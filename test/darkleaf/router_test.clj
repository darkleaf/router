(ns darkleaf.router-test
  (:require [clojure.test :refer :all]
            [darkleaf.router :refer [route scope build-handler build-matcher]]))

(def routes
  (flatten
   (list
    (route :main-page
           :pattern '{:segments [], :request-method :get}
           :handler identity)
    (route :legacy-page
           :vars '#{slug}
           :pattern '{:segments ["pages" (slug :guard #{"old-page"})], :request-method :get}
           :handler identity)
    (route :page
           :vars '#{slug}
           :pattern '{:segments ["pages" slug], :request-method :get}
           :handler identity)
    (scope :api
           {:vars '#{api-token}
            :pattern '{:segments ["api"], :headers {"token" api-token}}}
           (route :create-page
                  :pattern '{:segments ["pages"], :request-method :post}
                  :handler identity)
           (route :update-page
                  :vars '#{slug}
                  :pattern '{:segments ["pages" slug], :request-method :patch}
                  :handler identity))
    (route :not-found
           :handler identity))))

(deftest test-matcher
  (let [matcher (build-matcher routes)]
    (are [request r-name r-scope r-params] (= (let [[matched-route route-params] (matcher request)]
                                                [(:name matched-route) (:scope matched-route) route-params])
                                              [r-name r-scope r-params])
      {:segments [], :request-method :get}
      :main-page [] {}

      {:segments ["pages" "about"], :request-method :get}
      :page [] {:slug "about"}

      {:segments ["pages" "old-page"], :request-method :get}
      :legacy-page [] {:slug "old-page"}

      {:segments ["api" "pages"], :request-method :post, :headers {"token" "secret"}}
      :create-page [:api] {:api-token "secret"}

      {:segments ["api" "pages" "contacts"], :request-method :patch, :headers {"token" "secret"}}
      :update-page [:api] {:slug "contacts", :api-token "secret"})))

(deftest test-handler
  (let [matcher (build-matcher routes)
        handler (build-handler matcher)
        request {:uri "/pages/about", :request-method :get}
        response (handler request)]
    (is (= :page (get-in response [:matched-route :name])))
    (is (= ["pages" "about"] (:segments response)))
    (is (contains? response :matched-route))
    (is (= {:slug "about"} (:route-params response)))))

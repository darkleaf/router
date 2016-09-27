(ns darkleaf.router.low-level-test
  (:require [clojure.test :refer :all]
            [clojure.template :refer [do-template]]
            [darkleaf.router.low-level :refer :all]))

(def routes
  (combine-routes
   (route :main-page
          :pattern '{:segments [], :request-method :get}
          :template '{:segments [], :request-method :get}
          :handler identity)
   (route :legacy-page
          :vars '#{slug}
          :pattern '{:segments ["pages" (slug :guard #{"old-page"})], :request-method :get}
          :template '{:segments ["pages" ~slug], :request-method :get}
          :handler identity)
   (route :page
          :vars '#{slug}
          :pattern '{:segments ["pages" slug], :request-method :get}
          :template '{:segments ["pages" ~slug], :request-method :get}
          :handler identity)
   (scope :api
          {:vars '#{api-token}
           :pattern '{:segments ["api"], :headers {"token" api-token}}
           :template '{:segments ["api"], :headers {"token" ~api-token}}}
          (route :create-page
                 :pattern '{:segments ["pages"], :request-method :post}
                 :template '{:segments ["pages"], :request-method :post}
                 :handler identity)
          (route :update-page
                 :vars '#{slug}
                 :pattern '{:segments ["pages" slug], :request-method :patch}
                 :template '{:segments ["pages" ~slug], :request-method :patch}
                 :handler identity))
   (route :not-found
          :pattern {}
          :template {}
          :handler identity)))

(deftest bidirectional-matching
  (let [matcher (build-matcher routes)
        reverse-matcher (build-reverse-matcher routes)]
    (do-template [req-name req-scope req-params request]
                 (testing req-name
                   (is (= (reverse-matcher req-name req-scope req-params)
                          request))
                   (is (= (let [[matched-route route-params] (matcher request)]
                            [(:name matched-route) (:scope matched-route) route-params]
                            [req-name req-scope req-params]))))

                 :main-page [] {}
                 {:segments [], :request-method :get}

                 :page [] {:slug "about"}
                 {:segments ["pages" "about"], :request-method :get}

                 :legacy-page [] {:slug "old-page"}
                 {:segments ["pages" "old-page"], :request-method :get}

                 :create-page [:api] {:api-token "secret"}
                 {:segments ["api" "pages"], :request-method :post, :headers {"token" "secret"}}

                 :update-page [:api] {:slug "contacts", :api-token "secret"}
                 {:segments ["api" "pages" "contacts"], :request-method :patch, :headers {"token" "secret"}})))

(ns darkleaf.router.low-level-test
  (:require [clojure.test :refer :all]
            [clojure.template :refer [do-template]]
            [darkleaf.router.low-level :refer :all, :as ll]))

(def routes
  (combine-routes
   (route :main-page
          :pattern '{::ll/segments [], :request-method :get}
          :template '{::ll/segments [], :request-method :get}
          :handler identity)
   (route :legacy-page
          :vars '#{slug}
          :pattern '{::ll/segments ["pages" (slug :guard #{"old-page"})], :request-method :get}
          :template '{::ll/segments ["pages" ~slug], :request-method :get}
          :handler identity)
   (route :page
          :vars '#{slug}
          :pattern '{::ll/segments ["pages" slug], :request-method :get}
          :template '{::ll/segments ["pages" ~slug], :request-method :get}
          :handler identity)
   (scope :api
          {:vars '#{api-token}
           :pattern '{::ll/segments ["api"], :headers {"token" api-token}}
           :template '{::ll/segments ["api"], :headers {"token" ~api-token}}}
          (route :create-page
                 :pattern '{::ll/segments ["pages"], :request-method :post}
                 :template '{::ll/segments ["pages"], :request-method :post}
                 :handler identity)
          (route :update-page
                 :vars '#{slug}
                 :pattern '{::ll/segments ["pages" slug], :request-method :patch}
                 :template '{::ll/segments ["pages" ~slug], :request-method :patch}
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
                 {::ll/segments [], :request-method :get}

                 :page [] {:slug "about"}
                 {::ll/segments ["pages" "about"], :request-method :get}

                 :legacy-page [] {:slug "old-page"}
                 {::ll/segments ["pages" "old-page"], :request-method :get}

                 :create-page [:api] {:api-token "secret"}
                 {::ll/segments ["api" "pages"], :request-method :post, :headers {"token" "secret"}}

                 :update-page [:api] {:slug "contacts", :api-token "secret"}
                 {::ll/segments ["api" "pages" "contacts"], :request-method :patch, :headers {"token" "secret"}})))

(ns darkleaf.router-test
  (:require [clojure.test :refer :all]
            [clojure.template :refer [do-template]]
            [darkleaf.router :refer :all]))

#_(def routes
    (build-routes
     (root (:index pages-controller))
     (resources :pages pages-controller)
     (resource :account account-controller
               (resources :pages accoutn-pages-controller))
     (section :taxonomy
              (wildcard (build-fake-action :taxonomy)))
     (section :admin
              :middleware admin-middleware
              (root (:index admin-pages-controller))
              (resources :pages 'page-id admin-pages-controller
                         (get-action :foo  identity)
                         (post-action identity)
                         (resources :comments admin-page-comments-controller)))

     (not-found (build-fake-action :not-found))))

(def one-level-routes
  (build-routes
   (root identity)
   (action :get :about identity)
   (wildcard :get :taxonomy identity)
   #_(not-found identity)))

(deftest test-one-level-routes
  (let [handler (build-handler one-level-routes)
        request-for (build-request-for one-level-routes)]
    (do-template [req-name req-scope req-params request]
                 (testing req-name
                   (testing "direct"
                     (let [response (handler request)]
                       (is (= req-name (get-in response [:matched-route :name])))))
                   (testing "reverse"
                     (let [computed-request (request-for req-name req-scope req-params)]
                       (is (= request (dissoc computed-request :segments))))))
                 :root '[] {}
                 {:uri "/", :request-method :get}

                 :about '[] {}
                 {:uri "/about", :request-method :get}

                 :taxonomy '[] {:rest ["animal" "cat"]}
                 {:uri "/animal/cat", :request-method :get})))

#_(deftest test-handler
    (let [handler (build-handler matcher)
          request {:uri "/pages/about", :request-method :get}
          response (handler request)]
      (is (= :page (get-in response [:matched-route :name])))
      (is (= ["pages" "about"] (:segments response)))
      (is (contains? response :matched-route))
      (is (= {:slug "about"} (:route-params response)))))

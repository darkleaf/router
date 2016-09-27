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

(def routes
  (build-routes
   (root identity)
   (action :get :about identity)
   (section :taxonomy
            (action :get :foo identity)
            (wildcard :get identity))
   (resources :pages {:index identity
                      :show identity})
   (resource :account {:show identity
                       :edit identity
                       :update identity})
   (not-found identity)))

(deftest test-routes
  (let [handler (build-handler routes)
        request-for (build-request-for routes)]
    (do-template [req-name req-scope req-params request]
                 (testing req-name
                   (testing "direct"
                     (let [response (handler request)]
                       (is (= req-name (get-in response [:matched-route :name])))
                       (is (= req-params (:route-params response)))))
                   (testing "reverse"
                     (let [computed-request (request-for req-name req-scope req-params)]
                       (is (= request computed-request)))))
                 :root [] {}
                 {:uri "/", :request-method :get}

                 :about [] {}
                 {:uri "/about", :request-method :get}

                 :wildcard [:taxonomy] {:wildcard ["animal" "cat"]}
                 {:uri "/taxonomy/animal/cat", :request-method :get}

                 :index [:pages] {}
                 {:uri "/pages", :request-method :get}

                 :show [:pages] {:id "some-id"}
                 {:uri "/pages/some-id", :request-method :get}

                 :show [:account] {}
                 {:uri "/account", :request-method :get}

                 :edit [:account] {}
                 {:uri "/account/edit", :request-method :get}

                 :update [:account] {}
                 {:uri "/account", :request-method :patch})
    (testing :not-found
      (let [request {:uri "/not-found/page", :request-method :get}
            response (handler request)]
        (is (= :not-found (get-in response [:matched-route :name])))))))

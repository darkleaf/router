(ns darkleaf.router-test
  (:require [clojure.test :refer :all]
            [clojure.template :refer [do-template]]
            [darkleaf.router :refer :all]))

(def fake-handler identity)

(def routes
  (build-routes
   (root fake-handler)
   (action :get :about fake-handler)
   (section :taxonomy
            (wildcard :get fake-handler))
   (resources :pages 'page-id {:index fake-handler
                               :new fake-handler
                               :create fake-handler
                               :show fake-handler
                               :edit fake-handler
                               :update fake-handler
                               :destroy fake-handler}
              :collection
              [(action :archived fake-handler)]
              :member
              [(resources :comments 'comment-id {:index fake-handler})])
   (resource :account {:new fake-handler
                       :create fake-handler
                       :show fake-handler
                       :edit fake-handler
                       :update fake-handler
                       :destroy fake-handler}
             (resources :pages 'page-id {:index fake-handler}))
   (not-found fake-handler)))

#_{:middlewares []
   :member-widdlewares []
   :index identity
   :snow identity}

(deftest test-routes
  (let [handler (build-handler routes)
        request-for (build-request-for routes)]
    (do-template [req-name req-scope req-params request]
                 (testing (str req-name " " req-scope)
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

                 ;; pages routes
                 :index [:pages] {}
                 {:uri "/pages", :request-method :get}

                 :new [:pages] {}
                 {:uri "/pages/new", :request-method :get}

                 :create [:pages] {}
                 {:uri "/pages", :request-method :post}

                 :show [:pages] {:page-id "some-id"}
                 {:uri "/pages/some-id", :request-method :get}

                 :edit [:pages] {:page-id "about"}
                 {:uri "/pages/about/edit", :request-method :get}

                 :update [:pages] {:page-id "contacts"}
                 {:uri "/pages/contacts", :request-method :patch}

                 :destroy [:pages] {:page-id "wrong"}
                 {:uri "/pages/wrong", :request-method :delete}

                 ;; pages collection routes
                 :archived [:pages] {}
                 {:uri "/pages/archived", :request-method :get}

                 ;; pages member routes
                 :index [:pages :comments] {:page-id "some-id"}
                 {:uri "/pages/some-id/comments", :request-method :get}

                 ;; account routes
                 :new [:account] {}
                 {:uri "/account/new", :request-method :get}

                 :create [:account] {}
                 {:uri "/account", :request-method :post}

                 :show [:account] {}
                 {:uri "/account", :request-method :get}

                 :edit [:account] {}
                 {:uri "/account/edit", :request-method :get}

                 :update [:account] {}
                 {:uri "/account", :request-method :patch}

                 :destroy [:account] {}
                 {:uri "/account", :request-method :delete}

                 ;; inner account routes
                 :index [:account :pages] {}
                 {:uri "/account/pages", :request-method :get})
    (testing :not-found
      (let [request {:uri "/not-found/page", :request-method :get}
            response (handler request)]
        (is (= :not-found (get-in response [:matched-route :name])))))))

(ns darkleaf.router-test
  (:require [clojure.test :refer :all]
            [clojure.template :refer [do-template]]
            [darkleaf.router :refer :all]))

(def routes
  (build-routes
   (root identity)
   (action :get :about identity)
   (section :taxonomy
            (wildcard :get identity))
   (resources :pages 'page-id {:index identity
                               :new identity
                               :create identity
                               :show identity
                               :edit identity
                               :update identity
                               :destroy identity}
              :collection
              [(action :archived identity)]
              :member
              [(resources :comments 'comment-id {:index identity})])
   (resource :account {:new identity
                       :create identity
                       :show identity
                       :edit identity
                       :update identity
                       :destroy identity}
             (resources :pages 'page-id {:index identity}))
   (guard :locale #{"ru" "en"}
          (action :localized-page identity)
          (not-found identity))
   (not-found identity)))

(deftest test-routes
  (let [handler (build-handler routes)
        request-for (build-request-for routes)]
    (do-template [req-name req-scope req-params request]
                 (testing (:uri request)
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
                 {:uri "/account/pages", :request-method :get}

                 ;; guard locale
                 :localized-page [:locale] {:locale "en"}
                 {:uri "/en/localized-page", :request-method :get}

                 :localized-page [:locale] {:locale "ru"}
                 {:uri "/ru/localized-page", :request-method :get}

                 ;; not found
                 :not-found [] {:requested-segments ["not-found" "page"]}
                 {:uri "/not-found/page"}

                 :not-found [:locale] {:requested-segments ["wrong" "path"], :locale "en"}
                 {:uri "/en/wrong/path"})
    (testing :guard
      (let [request {:uri "/it/localized-page", :request-method :get}
            response (handler request)]
        (is (not= :localized-page (get-in response [:matched-route :name])))))))

;; ---------- wrap-handler testing ----------

(defn find-page [slug]
  (get
   {"about" {:id 1, :slug "about"}
    "contacts" {:id 2, :slug "contacts"}}
   slug))

(defn find-page-middleware [handler]
  (fn [req]
    (-> req
        (assoc-in [:models :page] (find-page (get-in req [:route-params :page-slug])))
        handler)))

(defn test-middleware [handler]
  (fn [req]
    (-> req
        (assoc :test-key :test-value)
        handler)))

(def routes-with-middleware
  (build-routes
   (wrap-handler test-middleware
                 (action :some-action identity))
   (resources :pages 'page-slug {:middleware test-middleware
                                 :member-middleware find-page-middleware
                                 :index identity
                                 :show identity}
              :member
              [(action :member-action identity)]
              :collection
              [(action :collection-action identity)])
   (resource :account {:middleware test-middleware
                       :show identity}
             (action :additional-action identity))))

(deftest test-routes-with-middleware
  (let [handler (build-handler routes-with-middleware)]
    (testing "wrap"
      (let [request {:uri "/some-action", :request-method :get}
            response (handler request)]
        (is (= :test-value (:test-key response)))))
    (testing "resoure(s) middleware"
      (do-template [request]
                   (testing request
                     (let [response (handler request)]
                       (is (= :test-value (:test-key response)))))
                   {:uri "/pages/about", :request-method :get}
                   {:uri "/pages/contacts/member-action", :request-method :get}
                   {:uri "/pages", :request-method :get}
                   {:uri "/pages/collection-action", :request-method :get}

                   {:uri "/account", :request-method :get}
                   {:uri "/account/additional-action", :request-method :get}))

    (testing "resources member middleware"
      (do-template [request model]
                   (testing request
                     (let [response (handler request)]
                       (is (= model (get-in response [:models :page])))))
                   {:uri "/pages/about", :request-method :get} {:id 1, :slug "about"}
                   {:uri "/pages/contacts/member-action", :request-method :get} {:id 2, :slug "contacts"}))))

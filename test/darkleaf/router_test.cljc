(ns darkleaf.router-test
  (:require [clojure.test :refer [deftest testing is]]
            [darkleaf.router :as r]))

(defn route-testing [routes action-id scope params request]
  (testing (str "action " action-id " "
                "in scope " scope " "
                "with params " params)
    (testing "direct matching"
      (let [handler (r/make-handler routes)
            response (handler request)]
        (is (= action-id (::r/action response)))
        (is (= scope     (::r/scope response)))
        (is (= params    (::r/params response)))))
    (testing "reverse matching"
      (let [request-for (r/make-request-for routes)
            calculated-request (request-for action-id scope params)]
        (is (= request calculated-request))))))

;; ~~~~~~~~~~ Resources ~~~~~~~~~~

(deftest resources
  (let [pages-controller {:index   (fn [req] req)
                          :show    (fn [req] req)
                          :new     (fn [req] req)
                          :create  (fn [req] req)
                          :edit    (fn [req] req)
                          :update  (fn [req] req)
                          :put     (fn [req] req)
                          :destroy (fn [req] req)}
        pages (r/resources :pages :page pages-controller)
        pages-testing (partial route-testing pages)]
    (pages-testing :index [:pages] {}
                   {:uri "/pages", :request-method :get})
    (pages-testing :new [:page] {}
                   {:uri "/pages/new", :request-method :get})
    (pages-testing :create [:page] {}
                   {:uri "/pages", :request-method :post})
    (pages-testing :show [:page] {:page-id "some-id"}
                   {:uri "/pages/some-id", :request-method :get})
    (pages-testing :edit [:page] {:page-id "some-id"}
                   {:uri "/pages/some-id/edit", :request-method :get})
    (pages-testing :update [:page] {:page-id "some-id"}
                   {:uri "/pages/some-id", :request-method :patch})
    (pages-testing :put [:page] {:page-id "some-id"}
                   {:uri "/pages/some-id", :request-method :put})
    (pages-testing :destroy [:page] {:page-id "some-id"}
                   {:uri "/pages/some-id", :request-method :delete})))

(deftest resources-without-segment
  (let [pages-controller {:index   (fn [req] req)
                          :show    (fn [req] req)
                          :new     (fn [req] req)
                          :create  (fn [req] req)
                          :edit    (fn [req] req)
                          :update  (fn [req] req)
                          :put     (fn [req] req)
                          :destroy (fn [req] req)}
        pages (r/resources :pages :page pages-controller
                           :segment false)
        pages-testing (partial route-testing pages)]
    (pages-testing :index [:pages] {}
                   {:uri "", :request-method :get})
    (pages-testing :new [:page] {}
                   {:uri "/new", :request-method :get})
    (pages-testing :create [:page] {}
                   {:uri "", :request-method :post})
    (pages-testing :show [:page] {:page-id "some-id"}
                   {:uri "/some-id", :request-method :get})
    (pages-testing :edit [:page] {:page-id "some-id"}
                   {:uri "/some-id/edit", :request-method :get})
    (pages-testing :update [:page] {:page-id "some-id"}
                   {:uri "/some-id", :request-method :patch})
    (pages-testing :put [:page] {:page-id "some-id"}
                   {:uri "/some-id", :request-method :put})
    (pages-testing :destroy [:page] {:page-id "some-id"}
                   {:uri "/some-id", :request-method :delete})))

;; ~~~~~~~~~~ Resource ~~~~~~~~~~

(deftest resource
  (let [star-controller {:show    (fn [req] req)
                         :new     (fn [req] req)
                         :create  (fn [req] req)
                         :edit    (fn [req] req)
                         :update  (fn [req] req)
                         :put     (fn [req] req)
                         :destroy (fn [req] req)}
        star (r/resource :star star-controller)
        star-testing (partial route-testing star)]
    (star-testing :new [:star] {}
                  {:uri "/star/new", :request-method :get})
    (star-testing :create [:star] {}
                  {:uri "/star", :request-method :post})
    (star-testing :show [:star] {}
                  {:uri "/star", :request-method :get})
    (star-testing :edit [:star] {}
                  {:uri "/star/edit", :request-method :get})
    (star-testing :update [:star] {}
                  {:uri "/star", :request-method :patch})
    (star-testing :put [:star] {}
                  {:uri "/star", :request-method :put})
    (star-testing :destroy [:star] {}
                  {:uri "/star", :request-method :delete})))

(deftest resource-wihout-segment
  (let [star-controller {:show    (fn [req] req)
                         :new     (fn [req] req)
                         :create  (fn [req] req)
                         :edit    (fn [req] req)
                         :update  (fn [req] req)
                         :put     (fn [req] req)
                         :destroy (fn [req] req)}
        star (r/resource :star star-controller
                         :segment false)
        star-testing (partial route-testing star)]
    (star-testing :new [:star] {}
                  {:uri "/new", :request-method :get})
    (star-testing :create [:star] {}
                  {:uri "", :request-method :post})
    (star-testing :show [:star] {}
                  {:uri "", :request-method :get})
    (star-testing :edit [:star] {}
                  {:uri "/edit", :request-method :get})
    (star-testing :update [:star] {}
                  {:uri "", :request-method :patch})
    (star-testing :put [:star] {}
                  {:uri "", :request-method :put})
    (star-testing :destroy [:star] {}
                  {:uri "", :request-method :delete})))

;; ~~~~~~~~~~ Nested ~~~~~~~~~~

(deftest resources-with-nested
  (let [comments-controller {:show (fn [req] req)}
        comments (r/resources :comments :comment comments-controller)

        pages-controller {}
        pages (r/resources :pages :page pages-controller
                           comments)
        pages-testing (partial route-testing pages)]
    (pages-testing :show [:page :comment] {:page-id "some-page-id"
                                           :comment-id "some-comment-id"}
                   {:uri "/pages/some-page-id/comments/some-comment-id"
                    :request-method :get})))

(deftest resource-with-nested
  (let [comments-controller {:show (fn [req] req)}
        comments (r/resources :comments :comment comments-controller)

        star-controller {}
        star (r/resource :star star-controller
                         comments)
        star-testing (partial route-testing star)]
    (star-testing :show [:star :comment] {:comment-id "some-comment-id"}
                  {:uri "/star/comments/some-comment-id"
                   :request-method :get})))

;; ~~~~~~~~~~ Composite ~~~~~~~~~~

(deftest composite
  (let [posts-controller {:show (fn [req] req)}
        posts (r/resources :posts :post posts-controller)

        news-controller {:show (fn [req] req)}
        news (r/resources :news :news news-controller)

        routes (r/composite posts news)

        routes-testing (partial route-testing routes)]
    (routes-testing :show [:post] {:post-id "some-post-id"}
                    {:uri "/posts/some-post-id"
                     :request-method :get})
    (routes-testing :show [:news] {:news-id "some-news-id"}
                    {:uri "/news/some-news-id"
                     :request-method :get})))

;; ~~~~~~~~~~ Scopes ~~~~~~~~~~

(deftest section
 (let [pages-controller {:index (fn [req] req)}
       pages (r/resources :pages :page pages-controller)
       admin (r/section :admin
                        pages)
       admin-testing (partial route-testing admin)]
   (admin-testing :index [:admin :pages] {}
                  {:uri "/admin/pages", :request-method :get})))

;; ~~~~~~~~~~ Middlewares ~~~~~~~~~~

(deftest resources-with-middleware
  (let [comments-controller {:middleware (fn [handler]
                                           (fn [req]
                                             (-> req
                                                 (assoc :test-key :overriden)
                                                 (handler))))
                             :show (fn [req] req)
                             :index (fn [req] req)}
        comments (r/resources :comments :comment comments-controller)

        pages-controller {:middleware (fn [handler]
                                        (fn [req]
                                          (-> req
                                              (assoc :test-key :original)
                                              (handler))))
                          :index (fn [req] req)
                          :show (fn [req] req)}
        pages (r/resources :pages :page pages-controller
                           comments)
        handler (r/make-handler pages)]
    (testing "head resources"
      (let [req {:uri "/pages", :request-method :get}
            resp (handler req)]
        (is (not (contains? resp :test-key))))
      (let [req {:uri "/pages/1", :request-method :get}
            resp (handler req)]
        (is (= :original (:test-key resp)))))
    (testing "nested"
      (let [req {:uri "/pages/1/comments", :request-method :get}
            resp (handler req)]
        (is (= :original (:test-key resp))))
      (let [req {:uri "/pages/1/comments/1", :request-method :get}
            resp (handler req)]
        (is (= :overriden (:test-key resp)))))))

(deftest section-with-midleware
  (let [pages-controller {:index (fn [req] req)}
        pages (r/resources :pages :page pages-controller)
        middleware (fn [handler]
                     (fn [req]
                       (-> req
                           (assoc :test-key true)
                           (handler))))
        routes (r/section :admin
                          :middleware middleware
                          pages)
        handler (r/make-handler routes)]
    (testing :index
      (let [req {:uri "/admin/pages", :request-method :get}
            resp (handler req)]
        (is (contains? resp :test-key))))))

(deftest wrapper
  (let [pages-controller {:index (fn [req] req)}
        pages (r/resources :pages :page pages-controller)
        middleware (fn [handler]
                     (fn [req]
                       (-> req
                           (assoc :test-key true)
                           (handler))))
        routes (r/wrapper middleware
                          pages)
        handler (r/make-handler routes)]
    (testing :index
      (let [req {:uri "/pages", :request-method :get}
            resp (handler req)]
        (is (contains? resp :test-key))))))

;; ~~~~~~~~~~ Utils ~~~~~~~~~~

(deftest internal-request-for
  (let [pages-controller {:index (fn [req] req)
                          :show (fn [req] true)}
        pages (r/resources :pages :page pages-controller)
        handler (r/make-handler pages)
        main-req {:uri "/pages", :request-method :get}
        main-resp (handler main-req)
        request-for (::r/request-for main-resp)]
    (testing "presence"
      (is (contains? main-resp ::r/request-for)))
    (testing "correctness"
      (let [req (request-for :show [:page] {:page-id 1})]
        (is (= (:uri req) "/pages/1"))))))

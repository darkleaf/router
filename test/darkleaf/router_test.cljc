(ns darkleaf.router-test
  (:require [clojure.test :refer [deftest testing is are]]
            [darkleaf.router :as r]))

(defn route-testing [routes action-id scope params request response]
  (testing (str "action " action-id " "
                "in scope " scope " "
                "with params " params)
    (testing "direct matching"
      (let [handler (r/make-handler routes)]
        (is (= response (handler request)))))
    (testing "reverse matching"
      (let [request-for (r/make-request-for routes)]
        (is (= request (request-for action-id scope params)))))))

(deftest resources
  (testing "ordinal"
    (let [pages-controller {:index   (fn [req] "index resp")
                            :show    (fn [req] "show resp")
                            :new     (fn [req] "new resp")
                            :create  (fn [req] "create resp")
                            :edit    (fn [req] "edit resp")
                            :update  (fn [req] "update resp")
                            :put     (fn [req] "put resp")
                            :destroy (fn [req] "destroy resp")}
          pages (r/resources :pages :page pages-controller)
          pages-testing (partial route-testing pages)]
      (pages-testing :index [:pages] {}
                     {:uri "/pages", :request-method :get}
                     "index resp")
      (pages-testing :new [:page] {}
                     {:uri "/pages/new", :request-method :get}
                     "new resp")
      (pages-testing :create [:page] {}
                     {:uri "/pages", :request-method :post}
                     "create resp")
      (pages-testing :show [:page] {:page-id "some-id"}
                     {:uri "/pages/some-id", :request-method :get}
                     "show resp")
      (pages-testing :edit [:page] {:page-id "some-id"}
                     {:uri "/pages/some-id/edit", :request-method :get}
                     "edit resp")
      (pages-testing :update [:page] {:page-id "some-id"}
                     {:uri "/pages/some-id", :request-method :patch}
                     "update resp")
      (pages-testing :put [:page] {:page-id "some-id"}
                     {:uri "/pages/some-id", :request-method :put}
                     "put resp")
      (pages-testing :destroy [:page] {:page-id "some-id"}
                     {:uri "/pages/some-id", :request-method :delete}
                     "destroy resp")))
  (testing "specific segment"
    (let [people-controller {:index   (fn [req] "index resp")
                             :show    (fn [req] "show resp")
                             :new     (fn [req] "new resp")
                             :create  (fn [req] "create resp")
                             :edit    (fn [req] "edit resp")
                             :update  (fn [req] "update resp")
                             :put     (fn [req] "put resp")
                             :destroy (fn [req] "destroy resp")}
          people (r/resources :people :person people-controller
                              :segment "menschen")
          people-testing (partial route-testing people)]
      (people-testing :index [:people] {}
                      {:uri "/menschen", :request-method :get}
                      "index resp")
      (people-testing :new [:person] {}
                      {:uri "/menschen/new", :request-method :get}
                      "new resp")
      (people-testing :create [:person] {}
                      {:uri "/menschen", :request-method :post}
                      "create resp")
      (people-testing :show [:person] {:person-id "some-id"}
                      {:uri "/menschen/some-id", :request-method :get}
                      "show resp")
      (people-testing :edit [:person] {:person-id "some-id"}
                      {:uri "/menschen/some-id/edit", :request-method :get}
                      "edit resp")
      (people-testing :update [:person] {:person-id "some-id"}
                      {:uri "/menschen/some-id", :request-method :patch}
                      "update resp")
      (people-testing :put [:person] {:person-id "some-id"}
                      {:uri "/menschen/some-id", :request-method :put}
                      "put resp")
      (people-testing :destroy [:person] {:person-id "some-id"}
                      {:uri "/menschen/some-id", :request-method :delete}
                      "destroy resp")))
  (testing "without segment"
    (let [pages-controller {:index   (fn [req] "index resp")
                            :show    (fn [req] "show resp")
                            :new     (fn [req] "new resp")
                            :create  (fn [req] "create resp")
                            :edit    (fn [req] "edit resp")
                            :update  (fn [req] "update resp")
                            :put     (fn [req] "put resp")
                            :destroy (fn [req] "destroy resp")}
          pages (r/resources :pages :page pages-controller
                             :segment false)
          pages-testing (partial route-testing pages)]
      (pages-testing :index [:pages] {}
                     {:uri "", :request-method :get}
                     "index resp")
      (pages-testing :new [:page] {}
                     {:uri "/new", :request-method :get}
                     "new resp")
      (pages-testing :create [:page] {}
                     {:uri "", :request-method :post}
                     "create resp")
      (pages-testing :show [:page] {:page-id "some-id"}
                     {:uri "/some-id", :request-method :get}
                     "show resp")
      (pages-testing :edit [:page] {:page-id "some-id"}
                     {:uri "/some-id/edit", :request-method :get}
                     "edit resp")
      (pages-testing :update [:page] {:page-id "some-id"}
                     {:uri "/some-id", :request-method :patch}
                     "update resp")
      (pages-testing :put [:page] {:page-id "some-id"}
                     {:uri "/some-id", :request-method :put}
                     "put resp")
      (pages-testing :destroy [:page] {:page-id "some-id"}
                     {:uri "/some-id", :request-method :delete}
                     "destroy resp"))))

(deftest resource
  (testing "ordinal"
    (let [star-controller {:show    (fn [req] "show resp")
                           :new     (fn [req] "new resp")
                           :create  (fn [req] "create resp")
                           :edit    (fn [req] "edit resp")
                           :update  (fn [req] "update resp")
                           :put     (fn [req] "put resp")
                           :destroy (fn [req] "destroy resp")}
          star (r/resource :star star-controller)
          star-testing (partial route-testing star)]
      (star-testing :new [:star] {}
                    {:uri "/star/new", :request-method :get}
                    "new resp")
      (star-testing :create [:star] {}
                    {:uri "/star", :request-method :post}
                    "create resp")
      (star-testing :show [:star] {}
                    {:uri "/star", :request-method :get}
                    "show resp")
      (star-testing :edit [:star] {}
                    {:uri "/star/edit", :request-method :get}
                    "edit resp")
      (star-testing :update [:star] {}
                    {:uri "/star", :request-method :patch}
                    "update resp")
      (star-testing :put [:star] {}
                    {:uri "/star", :request-method :put}
                    "put resp")
      (star-testing :destroy [:star] {}
                    {:uri "/star", :request-method :delete}
                    "destroy resp")))
  (testing "specific segment"
    (let [star-controller {:show    (fn [req] "show resp")
                           :new     (fn [req] "new resp")
                           :create  (fn [req] "create resp")
                           :edit    (fn [req] "edit resp")
                           :update  (fn [req] "update resp")
                           :put     (fn [req] "put resp")
                           :destroy (fn [req] "destroy resp")}
          star (r/resource :star star-controller
                           :segment "estrella")
          star-testing (partial route-testing star)]
      (star-testing :new [:star] {}
                    {:uri "/estrella/new", :request-method :get}
                    "new resp")
      (star-testing :create [:star] {}
                    {:uri "/estrella", :request-method :post}
                    "create resp")
      (star-testing :show [:star] {}
                    {:uri "/estrella", :request-method :get}
                    "show resp")
      (star-testing :edit [:star] {}
                    {:uri "/estrella/edit", :request-method :get}
                    "edit resp")
      (star-testing :update [:star] {}
                    {:uri "/estrella", :request-method :patch}
                    "update resp")
      (star-testing :put [:star] {}
                    {:uri "/estrella", :request-method :put}
                    "put resp")
      (star-testing :destroy [:star] {}
                    {:uri "/estrella", :request-method :delete}
                    "destroy resp")))
  (testing "wihout segment"
    (let [star-controller {:show    (fn [req] "show resp")
                           :new     (fn [req] "new resp")
                           :create  (fn [req] "create resp")
                           :edit    (fn [req] "edit resp")
                           :update  (fn [req] "update resp")
                           :put     (fn [req] "put resp")
                           :destroy (fn [req] "destroy resp")}
          star (r/resource :star star-controller
                           :segment false)
          star-testing (partial route-testing star)]
      (star-testing :new [:star] {}
                    {:uri "/new", :request-method :get}
                    "new resp")
      (star-testing :create [:star] {}
                    {:uri "", :request-method :post}
                    "create resp")
      (star-testing :show [:star] {}
                    {:uri "", :request-method :get}
                    "show resp")
      (star-testing :edit [:star] {}
                    {:uri "/edit", :request-method :get}
                    "edit resp")
      (star-testing :update [:star] {}
                    {:uri "", :request-method :patch}
                    "update resp")
      (star-testing :put [:star] {}
                    {:uri "", :request-method :put}
                    "put resp")
      (star-testing :destroy [:star] {}
                    {:uri "", :request-method :delete}
                    "destroy resp"))))

(deftest nesting
  (testing "resources"
    (let [pages-controller {:index (fn [req] "some")
                            :show (fn [req] "pages show resp")}
          comments-controller {:show (fn [req] "show resp")}
          star-controller {:show (fn [req] "star show resp")}
          routes (r/resources :pages :page pages-controller
                              (r/resources :comments :comment comments-controller)
                              (r/resource :star star-controller))
          routes-testing (partial route-testing routes)]
      (routes-testing :show [:page :comment] {:page-id "some-page-id"
                                              :comment-id "some-comment-id"}
                      {:uri "/pages/some-page-id/comments/some-comment-id"
                       :request-method :get}
                      "show resp")
      (routes-testing :show [:page :star] {:page-id 1}
                      {:uri "/pages/1/star", :request-method :get}
                      "star show resp")))

  (testing "resource"
    (let [star-controller {}
          comments-controller {:show (fn [req] "show resp")}
          routes (r/resource :star star-controller
                             (r/resources :comments :comment comments-controller))
          routes-testing (partial route-testing routes)]
      (routes-testing :show [:star :comment] {:comment-id "some-comment-id"}
                      {:uri "/star/comments/some-comment-id"
                       :request-method :get}
                      "show resp"))))

(deftest composite
  (let [posts-controller {:show (fn [req] "show post resp")}
        news-controller {:show (fn [req] "show news resp")}
        routes (r/composite
                (r/resources :posts :post posts-controller)
                (r/resources :news :news news-controller))
        routes-testing (partial route-testing routes)]
    (routes-testing :show [:post] {:post-id "some-post-id"}
                    {:uri "/posts/some-post-id"
                     :request-method :get}
                    "show post resp")
    (routes-testing :show [:news] {:news-id "some-news-id"}
                    {:uri "/news/some-news-id"
                     :request-method :get}
                    "show news resp")))

(deftest section
  (testing "ordinal"
    (let [pages-controller {:index (fn [req] "index resp")}
          admin (r/section :admin
                           (r/resources :pages :page pages-controller))
          admin-testing (partial route-testing admin)]
      (admin-testing :index [:admin :pages] {}
                     {:uri "/admin/pages", :request-method :get}
                     "index resp")))
  (testing "with segment"
    (let [pages-controller {:index (fn [req] "index resp")}
          admin (r/section :admin, :segment "private"
                           (r/resources :pages :page pages-controller))
          admin-testing (partial route-testing admin)]
      (admin-testing :index [:admin :pages] {}
                     {:uri "/private/pages", :request-method :get}
                     "index resp"))))

(deftest middleware
  (letfn [(make-middleware [name]
            (fn [handler]
              (fn [req]
                (str name " // " (handler req)))))]
    (testing "resources"
      (let [pages-controller {:middleware (make-middleware "pages")
                              :collection-middleware (make-middleware "collection")
                              :member-middleware (make-middleware "member")
                              :index   (fn [req] "index resp")
                              :show    (fn [req] "show resp")
                              :new     (fn [req] "new resp")
                              :create  (fn [req] "create resp")
                              :edit    (fn [req] "edit resp")
                              :update  (fn [req] "update resp")
                              :put     (fn [req] "put resp")
                              :destroy (fn [req] "destroy resp")}
            star-controller {:middleware (make-middleware "star")
                             :show (fn [req] "show star resp")}
            routes (r/resources :pages :page pages-controller
                                (r/resource :star star-controller))
            handler (r/make-handler routes)]

        (are [req resp] (= resp (handler req))
          {:uri "/pages", :request-method :get}
          "pages // collection // index resp"

          {:uri "/pages/1", :request-method :get}
          "pages // member // show resp"

          {:uri "/pages/new", :request-method :get}
          "pages // collection // new resp"

          {:uri "/pages", :request-method :post}
          "pages // collection // create resp"

          {:uri "/pages/1/edit", :request-method :get}
          "pages // member // edit resp"

          {:uri "/pages/1", :request-method :patch}
          "pages // member // update resp"

          {:uri "/pages/1", :request-method :put}
          "pages // member // put resp"

          {:uri "/pages/1", :request-method :delete}
          "pages // member // destroy resp"

          {:uri "/pages/1/star", :request-method :get}
          "pages // member // star // show star resp")))
    (testing "resource"
      (let [star-controller {:middleware (make-middleware "star")
                             :show    (fn [req] "show resp")
                             :new     (fn [req] "new resp")
                             :create  (fn [req] "create resp")
                             :edit    (fn [req] "edit resp")
                             :update  (fn [req] "update resp")
                             :put     (fn [req] "put resp")
                             :destroy (fn [req] "destroy resp")}
            comments-controllers {:middleware (make-middleware "comments")
                                  :index (fn [req] "comments index")}
            routes (r/resource :star star-controller
                               (r/resources :comments :comment comments-controllers))
            handler (r/make-handler routes)]
        (are [req resp] (= resp (handler req))
          {:uri "/star", :request-method :get}
          "star // show resp"

          {:uri "/star/new", :request-method :get}
          "star // new resp"

          {:uri "/star", :request-method :post}
          "star // create resp"

          {:uri "/star/edit", :request-method :get}
          "star // edit resp"

          {:uri "/star", :request-method :patch}
          "star // update resp"

          {:uri "/star", :request-method :put}
          "star // put resp"

          {:uri "/star", :request-method :delete}
          "star // destroy resp"

          {:uri "/star/comments", :request-method :get}
          "star // comments // comments index")))

    (testing "section"
      (let [pages-controller {:index (fn [req] "index resp")}
            routes (r/section :admin
                              :middleware (make-middleware "admin")
                              (r/resources :pages :page pages-controller))
            handler (r/make-handler routes)
            req {:uri "/admin/pages", :request-method :get}]
        (is (= "admin // index resp" (handler req)))))

    (testing "wrapper"
      (let [pages-controller {:index (fn [req] "index resp")}
            routes (r/wrapper (make-middleware "wrapper")
                              (r/resources :pages :page pages-controller))
            handler (r/make-handler routes)
            req {:uri "/pages", :request-method :get}]
        (is (= "wrapper // index resp" (handler req)))))))

(deftest request-keys
  (let [pages-controller {:index (fn [req] "index resp")
                          :show (fn [req] req)}
        pages (r/resources :pages :page pages-controller)
        handler (r/make-handler pages)
        returned-req (handler {:uri "/pages/1", :request-method :get})]
    (testing ::r/request-for
      (let [request-for (::r/request-for returned-req)
            req (request-for :index [:pages] {})]
        (is (= {:uri "/pages", :request-method :get} req))))
    (testing ::r/action
      (is (= :show (::r/action returned-req))))
    (testing ::r/scope
      (is (= [:page] (::r/scope returned-req))))
    (testing ::r/params
      (is (= {:page-id "1"} (::r/params returned-req))))))

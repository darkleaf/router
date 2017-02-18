(ns darkleaf.router-test
  (:require [clojure.test :refer [deftest testing is are]]
            [darkleaf.router :as r]))

(defn- make-middleware [name]
  (fn [handler]
    (fn [req]
      (str name " // " (handler req)))))

(defn- route-testing [routes action-id scope params request response]
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
                            :new     (fn [req] "new resp")
                            :create  (fn [req] "create resp")
                            :show    (fn [req] (str "show "
                                                    (-> req ::r/params :page)))
                            :edit    (fn [req] (str "edit "
                                                    (-> req ::r/params :page)))
                            :update  (fn [req] (str "update "
                                                    (-> req ::r/params :page)))
                            :put     (fn [req] (str "put "
                                                    (-> req ::r/params :page)))
                            :destroy (fn [req] (str "destroy "
                                                    (-> req ::r/params :page)))}
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
      (pages-testing :show [:page] {:page "about"}
                     {:uri "/pages/about", :request-method :get}
                     "show about")
      (pages-testing :edit [:page] {:page "about"}
                     {:uri "/pages/about/edit", :request-method :get}
                     "edit about")
      (pages-testing :update [:page] {:page "about"}
                     {:uri "/pages/about", :request-method :patch}
                     "update about")
      (pages-testing :put [:page] {:page "about"}
                     {:uri "/pages/about", :request-method :put}
                     "put about")
      (pages-testing :destroy [:page] {:page "about"}
                     {:uri "/pages/about", :request-method :delete}
                     "destroy about")))
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
      (people-testing :show [:person] {:person "some-id"}
                      {:uri "/menschen/some-id", :request-method :get}
                      "show resp")
      (people-testing :edit [:person] {:person "some-id"}
                      {:uri "/menschen/some-id/edit", :request-method :get}
                      "edit resp")
      (people-testing :update [:person] {:person "some-id"}
                      {:uri "/menschen/some-id", :request-method :patch}
                      "update resp")
      (people-testing :put [:person] {:person "some-id"}
                      {:uri "/menschen/some-id", :request-method :put}
                      "put resp")
      (people-testing :destroy [:person] {:person "some-id"}
                      {:uri "/menschen/some-id", :request-method :delete}
                      "destroy resp")))
  (testing "without segment"
    (let [pages-controller {:index   (fn [req] "index resp")
                            :new     (fn [req] "new resp")
                            :create  (fn [req] "create resp")
                            :show    (fn [req] (str "show "
                                                    (-> req ::r/params :page)))
                            :edit    (fn [req] (str "edit "
                                                    (-> req ::r/params :page)))
                            :update  (fn [req] (str "update "
                                                    (-> req ::r/params :page)))
                            :put     (fn [req] (str "put "
                                                    (-> req ::r/params :page)))
                            :destroy (fn [req] (str "destroy "
                                                    (-> req ::r/params :page)))}
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
      (pages-testing :show [:page] {:page "some"}
                     {:uri "/some", :request-method :get}
                     "show some")
      (pages-testing :edit [:page] {:page "some"}
                     {:uri "/some/edit", :request-method :get}
                     "edit some")
      (pages-testing :update [:page] {:page "some"}
                     {:uri "/some", :request-method :patch}
                     "update some")
      (pages-testing :put [:page] {:page "some"}
                     {:uri "/some", :request-method :put}
                     "put some")
      (pages-testing :destroy [:page] {:page "some"}
                     {:uri "/some", :request-method :delete}
                     "destroy some")))
  (testing "nested"
    (let [pages-controller {:index (fn [req] "some")
                            :show (fn [req] "pages show resp")}
          comments-controller {:show (fn [req] "show resp")}
          star-controller {:show (fn [req] (str "page "
                                                (-> req ::r/params :page)
                                                " star show resp"))}
          routes (r/resources :pages :page pages-controller
                              (r/resources :comments :comment comments-controller)
                              (r/resource :star star-controller))
          routes-testing (partial route-testing routes)]
      (routes-testing :show [:page :comment] {:page "some-page"
                                              :comment "some-comment"}
                      {:uri "/pages/some-page/comments/some-comment"
                       :request-method :get}
                      "show resp")
      (routes-testing :show [:page :star] {:page 1}
                      {:uri "/pages/1/star", :request-method :get}
                      "page 1 star show resp")))
  (testing "middleware"
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
        "pages // member // star // show star resp"))))

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
                    "destroy resp")))
  (testing "nested"
    (let [star-controller {}
          comments-controller {:show (fn [req] "show resp")}
          routes (r/resource :star star-controller
                             (r/resources :comments :comment comments-controller))
          routes-testing (partial route-testing routes)]
      (routes-testing :show [:star :comment] {:comment "some-comment"}
                      {:uri "/star/comments/some-comment"
                       :request-method :get}
                      "show resp")))
  (testing "middleware"
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
        "star // comments // comments index"))))

(deftest composite
  (let [posts-controller {:show (fn [req] "show post resp")}
        news-controller {:show (fn [req] "show news resp")}
        routes (r/composite
                (r/resources :posts :post posts-controller)
                (r/resources :news :news news-controller))
        routes-testing (partial route-testing routes)]
    (routes-testing :show [:post] {:post "some-post"}
                    {:uri "/posts/some-post"
                     :request-method :get}
                    "show post resp")
    (routes-testing :show [:news] {:news "some-news"}
                    {:uri "/news/some-news"
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
                     "index resp")))
  (testing "middleware"
    (let [pages-controller {:index (fn [req] "index resp")}
          routes (r/section :admin
                            :middleware (make-middleware "admin")
                            (r/resources :pages :page pages-controller))
          handler (r/make-handler routes)
          req {:uri "/admin/pages", :request-method :get}]
      (is (= "admin // index resp" (handler req))))))

(deftest wrapper
  (testing "middleware"
    (let [pages-controller {:index (fn [req] "index resp")}
          routes (r/wrapper (make-middleware "wrapper")
                            (r/resources :pages :page pages-controller))
          handler (r/make-handler routes)
          req {:uri "/pages", :request-method :get}]
      (is (= "wrapper // index resp" (handler req))))))

(deftest guard
  (testing "ordinal"
    (let [pages-controller {:index (fn [req] (str (-> req ::r/params :locale)
                                                  " index resp"))}
          routes (r/guard :locale #{"ru" "en"}
                          (r/resources :pages :page pages-controller))]
      (testing "correct"
        (let [routes-testing (partial route-testing routes)]
          (routes-testing :index [:locale :pages] {:locale "ru"}
                          {:uri "/ru/pages", :request-method :get}
                          "ru index resp")))
      (testing "wrong"
        (let [handler (r/make-handler routes)]
          (is (= 404
                 (-> {:uri "/wrong/pages", :request-method :get}
                     (handler)
                     :status)))))))
  (testing "middleware"
    (let [pages-controller {:index (fn [req] (str (-> req ::r/params :locale)
                                                  " index resp"))}
          routes (r/guard :locale #{"ru" "en"}
                          :middleware (make-middleware "guard")
                          (r/resources :pages :page pages-controller))
          handler (r/make-handler routes)]
      (is (= "guard // ru index resp"
             (-> {:uri "/ru/pages", :request-method :get}
                 (handler)))))))

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
      (is (= {:page "1"} (::r/params returned-req))))))

(deftest mount
  (let [forum-controller {:show (fn [req] "main")}
        forum-topics-controller {:index (fn [req] "")
                                 :show (fn [req] (-> req ::r/params :forum/topic))}
        forum (r/composite
               (r/resource :forum/main forum-controller
                           :segment false)
               (r/resources :forum/topics :forum/topic forum-topics-controller))
        sites-controller {}
        routes (r/resources :sites :site sites-controller
                            (r/mount forum :segment "forum"))
        routes-testing (partial route-testing routes)]
    (routes-testing :show [:site :forum/main] {:site "1"}
                    {:uri "/sites/1/forum", :request-method :get}
                    "main")
    (routes-testing :index [:site :forum/topics] {:site "1"}
                    {:uri "/sites/1/forum/topics", :request-method :get}
                    "")
    (routes-testing :show [:site :forum/topic] {:site "1", :forum/topic "2"}
                    {:uri "/sites/1/forum/topics/2", :request-method :get}
                    "2")))

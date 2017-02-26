(ns darkleaf.router.resources-test
  (:require [clojure.test :refer [deftest are]]
            [darkleaf.router :as r]
            [darkleaf.router.test-helpers :refer [route-testing make-middleware]]))

(deftest defaults
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
        pages (r/resources :pages :page pages-controller)]
    (route-testing pages
                   :description [:index [:pages] {}]
                   :request {:uri "/pages", :request-method :get}
                   :response "index resp")
    (route-testing pages
                   :description [:new [:page] {}]
                   :request {:uri "/pages/new", :request-method :get}
                   :response "new resp")
    (route-testing pages
                   :description [:create [:page] {}]
                   :request {:uri "/pages", :request-method :post}
                   :response "create resp")
    (route-testing pages
                   :description [:show [:page] {:page "about"}]
                   :request {:uri "/pages/about", :request-method :get}
                   :response "show about")
    (route-testing pages
                   :description [:edit [:page] {:page "about"}]
                   :request {:uri "/pages/about/edit", :request-method :get}
                   :response "edit about")
    (route-testing pages
                   :description [:update [:page] {:page "about"}]
                   :request {:uri "/pages/about", :request-method :patch}
                   :response "update about")
    (route-testing pages
                   :description [:put [:page] {:page "about"}]
                   :request {:uri "/pages/about", :request-method :put}
                   :response "put about")
    (route-testing pages
                   :description [:destroy [:page] {:page "about"}]
                   :request {:uri "/pages/about", :request-method :delete}
                   :response "destroy about")))

(deftest specific-segment
  (let [people-controller {:index   (fn [req] "index resp")
                           :show    (fn [req] "show resp")
                           :new     (fn [req] "new resp")
                           :create  (fn [req] "create resp")
                           :edit    (fn [req] "edit resp")
                           :update  (fn [req] "update resp")
                           :put     (fn [req] "put resp")
                           :destroy (fn [req] "destroy resp")}
        people (r/resources :people :person people-controller
                 :segment "menschen")]
    (route-testing people
                   :description [:index [:people] {}]
                   :request {:uri "/menschen", :request-method :get}
                   :response "index resp")
    (route-testing people
                   :description [:new [:person] {}]
                   :request {:uri "/menschen/new", :request-method :get}
                   :response "new resp")
    (route-testing people
                   :description [:create [:person] {}]
                   :request {:uri "/menschen", :request-method :post}
                   :response "create resp")
    (route-testing people
                   :description [:show [:person] {:person "some-id"}]
                   :request {:uri "/menschen/some-id", :request-method :get}
                   :response "show resp")
    (route-testing people
                   :description [:edit [:person] {:person "some-id"}]
                   :request {:uri "/menschen/some-id/edit", :request-method :get}
                   :response "edit resp")
    (route-testing people
                   :description [:update [:person] {:person "some-id"}]
                   :request {:uri "/menschen/some-id", :request-method :patch}
                   :response "update resp")
    (route-testing people
                   :description [:put [:person] {:person "some-id"}]
                   :request {:uri "/menschen/some-id", :request-method :put}
                   :response "put resp")
    (route-testing people
                   :description [:destroy [:person] {:person "some-id"}]
                   :request {:uri "/menschen/some-id", :request-method :delete}
                   :response "destroy resp")))

(deftest without-segment
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
        pages (r/resources :pages :page pages-controller, :segment false)]
    (route-testing pages
                   :description [:index [:pages] {}]
                   :request {:uri "", :request-method :get}
                   :response "index resp")
    (route-testing pages
                   :description [:new [:page] {}]
                   :request {:uri "/new", :request-method :get}
                   :response "new resp")
    (route-testing pages
                   :description [:create [:page] {}]
                   :request {:uri "", :request-method :post}
                   :response "create resp")
    (route-testing pages
                   :description [:show [:page] {:page "some"}]
                   :request {:uri "/some", :request-method :get}
                   :response "show some")
    (route-testing pages
                   :description [:edit [:page] {:page "some"}]
                   :request {:uri "/some/edit", :request-method :get}
                   :response "edit some")
    (route-testing pages
                   :description [:update [:page] {:page "some"}]
                   :request {:uri "/some", :request-method :patch}
                   :response "update some")
    (route-testing pages
                   :description [:put [:page] {:page "some"}]
                   :request {:uri "/some", :request-method :put}
                   :response "put some")
    (route-testing pages
                   :description [:destroy [:page] {:page "some"}]
                   :request {:uri "/some", :request-method :delete}
                   :response "destroy some")))

(deftest nested
  (let [pages-controller {:index (fn [req] "some")
                          :show (fn [req] "pages show resp")}
        comments-controller {:show (fn [req] "show resp")}
        star-controller {:show (fn [req] (str "page "
                                              (-> req ::r/params :page)
                                              " star show resp"))}
        routes (r/resources :pages :page pages-controller
                 (r/resources :comments :comment comments-controller)
                 (r/resource :star star-controller))]
    (route-testing routes
                   :description [:show
                                      [:page :comment]
                                      {:page "some-page", :comment "some-comment"}]
                   :request {:uri "/pages/some-page/comments/some-comment"
                             :request-method :get}
                   :response "show resp")
    (route-testing routes
                   :description [:show [:page :star] {:page 1}]
                   :request {:uri "/pages/1/star", :request-method :get}
                   :response "page 1 star show resp")))

(deftest middleware
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

(ns darkleaf.router.resource-test
  (:require [clojure.test :refer [deftest are]]
            [darkleaf.router :as r]
            [darkleaf.router.test-helpers :refer [route-testing make-middleware]]))

(deftest defaults
  (let [star-controller {:show    (fn [req] "show resp")
                         :new     (fn [req] "new resp")
                         :create  (fn [req] "create resp")
                         :edit    (fn [req] "edit resp")
                         :update  (fn [req] "update resp")
                         :put     (fn [req] "put resp")
                         :destroy (fn [req] "destroy resp")}
        star (r/resource :star star-controller)]
    (route-testing star
                   :description [:new [:star] {}]
                   :request {:uri "/star/new", :request-method :get}
                   :response "new resp")
    (route-testing star
                   :description [:create [:star] {}]
                   :request {:uri "/star", :request-method :post}
                   :response "create resp")
    (route-testing star
                   :description [:show [:star] {}]
                   :request {:uri "/star", :request-method :get}
                   :response "show resp")
    (route-testing star
                   :description [:edit [:star] {}]
                   :request {:uri "/star/edit", :request-method :get}
                   :response "edit resp")
    (route-testing star
                   :description [:update [:star] {}]
                   :request {:uri "/star", :request-method :patch}
                   :response "update resp")
    (route-testing star
                   :description [:put [:star] {}]
                   :request {:uri "/star", :request-method :put}
                   :response "put resp")
    (route-testing star
                   :description [:destroy [:star] {}]
                   :request {:uri "/star", :request-method :delete}
                   :response "destroy resp")))

(deftest specificsegment
  (let [star-controller {:show    (fn [req] "show resp")
                         :new     (fn [req] "new resp")
                         :create  (fn [req] "create resp")
                         :edit    (fn [req] "edit resp")
                         :update  (fn [req] "update resp")
                         :put     (fn [req] "put resp")
                         :destroy (fn [req] "destroy resp")}
        star (r/resource :star star-controller :segment "estrella")]
    (route-testing star
                   :description [:new [:star] {}]
                   :request {:uri "/estrella/new", :request-method :get}
                   :response "new resp")
    (route-testing star
                   :description [:create [:star] {}]
                   :request {:uri "/estrella", :request-method :post}
                   :response "create resp")
    (route-testing star
                   :description [:show [:star] {}]
                   :request {:uri "/estrella", :request-method :get}
                   :response "show resp")
    (route-testing star
                   :description [:edit [:star] {}]
                   :request {:uri "/estrella/edit", :request-method :get}
                   :response "edit resp")
    (route-testing star
                   :description [:update [:star] {}]
                   :request {:uri "/estrella", :request-method :patch}
                   :response "update resp")
    (route-testing star
                   :description [:put [:star] {}]
                   :request {:uri "/estrella", :request-method :put}
                   :response "put resp")
    (route-testing star
                   :description [:destroy [:star] {}]
                   :request {:uri "/estrella", :request-method :delete}
                   :response "destroy resp")))

(deftest wihout-segment
  (let [star-controller {:show    (fn [req] "show resp")
                         :new     (fn [req] "new resp")
                         :create  (fn [req] "create resp")
                         :edit    (fn [req] "edit resp")
                         :update  (fn [req] "update resp")
                         :put     (fn [req] "put resp")
                         :destroy (fn [req] "destroy resp")}
        star (r/resource :star star-controller :segment false)]
    (route-testing star
                   :description [:new [:star] {}]
                   :request {:uri "/new", :request-method :get}
                   :response "new resp")
    (route-testing star
                   :description [:create [:star] {}]
                   :request {:uri "", :request-method :post}
                   :response "create resp")
    (route-testing star
                   :description [:show [:star] {}]
                   :request {:uri "", :request-method :get}
                   :response "show resp")
    (route-testing star
                   :description [:edit [:star] {}]
                   :request {:uri "/edit", :request-method :get}
                   :response "edit resp")
    (route-testing star
                   :description [:update [:star] {}]
                   :request {:uri "", :request-method :patch}
                   :response "update resp")
    (route-testing star
                   :description [:put [:star] {}]
                   :request {:uri "", :request-method :put}
                   :response "put resp")
    (route-testing star
                   :description [:destroy [:star] {}]
                   :request {:uri "", :request-method :delete}
                   :response "destroy resp")))

(deftest nested
  (let [star-controller {}
        comments-controller {:show (fn [req] "show resp")}
        routes (r/resource :star star-controller
                 (r/resources :comments :comment comments-controller))]
    (route-testing routes
                   :description [:show [:star :comment] {:comment "some-comment"}]
                   :request {:uri "/star/comments/some-comment"
                             :request-method :get}
                   :response "show resp")))

(deftest middleware
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

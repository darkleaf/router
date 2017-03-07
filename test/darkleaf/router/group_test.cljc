(ns darkleaf.router.group-test
  (:require [clojure.test :refer [deftest is]]
            [darkleaf.router :as r]
            [darkleaf.router.test-helpers :refer [route-testing make-middleware]]))

(deftest defaults
  (let [posts-controller (r/controller
                           (show [req] "show post resp"))
        news-controller (r/controller
                          (show [req] "show news resp"))
        routes (r/group
                 (r/resources :posts :post posts-controller)
                 (r/resources :news :news news-controller))]
    (route-testing routes
                   :description [:show [:post] {:post "some-post"}]
                   :request {:uri "/posts/some-post", :request-method :get}
                   :response "show post resp")
    (route-testing routes
                   :description [:show [:news] {:news "some-news"}]
                   :request {:uri "/news/some-news", :request-method :get}
                   :response "show news resp")))

(deftest middleware
  (let [pages-controller (r/controller
                           (index [req] "index resp"))
        routes (r/group :middleware (make-middleware "wrapper")
                 (r/resources :pages :page pages-controller))
        handler (r/make-handler routes)
        req {:uri "/pages", :request-method :get}]
    (is (= "wrapper // index resp" (handler req)))))

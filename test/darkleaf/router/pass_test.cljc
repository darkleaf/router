(ns darkleaf.router.pass-test
  (:require [clojure.test :refer [deftest is]]
            [darkleaf.router :as r]
            [darkleaf.router.test-helpers :refer [route-testing make-middleware]]))

(deftest defaults
  (let [handler (fn [req] "dashboard")
        routes (r/section :admin
                 (r/pass :dashboard handler))]
    (route-testing routes
                   :description [:post [:admin :dashboard] {}]
                   :request {:uri "/admin/dashboard", :request-method :post}
                   :response "dashboard")
    (route-testing routes
                   :description [:get [:admin :dashboard] {:segments ["private" "users"]}]
                   :request {:uri "/admin/dashboard/private/users", :request-method :get}
                   :response "dashboard")))

(deftest with-segment
  (let [handler (fn [req] "dashboard")
        routes (r/section :admin
                 (r/pass :dashboard handler :segment "monitoring"))]
    (route-testing routes
                   :description [:post [:admin :dashboard] {}]
                   :request {:uri "/admin/monitoring", :request-method :post}
                   :response "dashboard")))

(deftest without-segment
  (let [main-controller {:show (fn [_] "main")}
        not-found-handler (fn [req] "custom 404 error")
        routes (r/group
                 (r/resource :main main-controller :segment false)
                 (r/pass :not-found not-found-handler :segment false))]
    (route-testing routes
                   :description [:show [:main] {}]
                   :request {:uri "", :request-method :get}
                   :response "main")
    (route-testing routes
                   :description [:get [:not-found] {:segments ["foo" "bar"]}]
                   :request {:uri "/foo/bar", :request-method :get}
                   :response "custom 404 error")))

(deftest middleware
  (let [handler (fn [req] "dashboard")
        middleware (make-middleware "m")
        routes (r/pass :dashboard handler :middleware middleware)
        handler (r/make-handler routes)]
    (is (= "m // dashboard")
        (handler {:uri "/dashboard", :request-method :get}))))

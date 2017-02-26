(ns darkleaf.router.section-test
  (:require [clojure.test :refer [deftest is]]
            [darkleaf.router :as r]
            [darkleaf.router.test-helpers :refer [route-testing make-middleware]]))

(deftest defaults
  (let [pages-controller {:index (fn [req] "index resp")}
        admin (r/section :admin
                (r/resources :pages :page pages-controller))]
    (route-testing admin
                   :description [:index [:admin :pages] {}]
                   :request {:uri "/admin/pages", :request-method :get}
                   :response "index resp")))

(deftest with-segment
  (let [pages-controller {:index (fn [req] "index resp")}
        admin (r/section :admin, :segment "private"
                (r/resources :pages :page pages-controller))]
    (route-testing admin
                   :description [:index [:admin :pages] {}]
                   :request {:uri "/private/pages", :request-method :get}
                   :response "index resp")))

(deftest middleware
  (let [pages-controller {:index (fn [req] "index resp")}
        routes (r/section :admin :middleware (make-middleware "admin")
                 (r/resources :pages :page pages-controller))
        handler (r/make-handler routes)
        req {:uri "/admin/pages", :request-method :get}]
    (is (= "admin // index resp" (handler req)))))

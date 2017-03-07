(ns darkleaf.router.guard-test
  (:require [clojure.test :refer [deftest testing is]]
            [darkleaf.router :as r]
            [darkleaf.router.test-helpers :refer [route-testing make-middleware]]))

(deftest defaults
  (let [pages-controller (r/controller
                           (index [req]
                             (str "locale: "
                                  (-> req ::r/params :locale))))
        routes (r/guard :locale #{"ru" "en"}
                 (r/resources :pages :page pages-controller))]
    (testing "correct"
      (let [routes-testing (partial route-testing routes)]
        (route-testing routes
                       :description [:index [:locale :pages] {:locale "ru"}]
                       :request {:uri "/ru/pages", :request-method :get}
                       :response "locale: ru")))
    (testing "wrong"
      (let [handler (r/make-handler routes)]
        (is (= 404
               (:status (handler {:uri "/wrong/pages", :request-method :get}))))))))

(deftest middleware
  (let [pages-controller (r/controller
                           (index [req]
                             (str "locale: "
                                  (-> req ::r/params :locale))))
        routes (r/guard :locale #{"ru" "en"} :middleware (make-middleware "guard")
                 (r/resources :pages :page pages-controller))
        handler (r/make-handler routes)]
    (is (= "guard // locale: ru"
           (handler {:uri "/ru/pages", :request-method :get})))))

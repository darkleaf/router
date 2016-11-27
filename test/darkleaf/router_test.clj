(ns darkleaf.router-test
  (:require [clojure.test :refer [deftest testing is]]
            [darkleaf.router :as r]))

(defn testing-route [routes action-id scope params request]
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
        testing-pages (partial testing-route pages)]
    (testing-pages :index [:pages] {}
                   {:uri "/pages", :request-method :get})
    (testing-pages :new [:pages] {}
                   {:uri "/pages/new", :request-method :get})
    (testing-pages :create [:pages] {}
                   {:uri "/pages", :request-method :post})
    (testing-pages :show [:page] {:page-id "some-id"}
                   {:uri "/pages/some-id", :request-method :get})
    (testing-pages :edit [:page] {:page-id "some-id"}
                   {:uri "/pages/some-id/edit", :request-method :get})
    (testing-pages :update [:page] {:page-id "some-id"}
                   {:uri "/pages/some-id", :request-method :patch})
    (testing-pages :put [:page] {:page-id "some-id"}
                   {:uri "/pages/some-id", :request-method :put})
    (testing-pages :destroy [:page] {:page-id "some-id"}
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
        testing-pages (partial testing-route pages)]
    (testing-pages :index [:pages] {}
                   {:uri "", :request-method :get})
    (testing-pages :new [:pages] {}
                   {:uri "/new", :request-method :get})
    (testing-pages :create [:pages] {}
                   {:uri "", :request-method :post})
    (testing-pages :show [:page] {:page-id "some-id"}
                   {:uri "/some-id", :request-method :get})
    (testing-pages :edit [:page] {:page-id "some-id"}
                   {:uri "/some-id/edit", :request-method :get})
    (testing-pages :update [:page] {:page-id "some-id"}
                   {:uri "/some-id", :request-method :patch})
    (testing-pages :put [:page] {:page-id "some-id"}
                   {:uri "/some-id", :request-method :put})
    (testing-pages :destroy [:page] {:page-id "some-id"}
                   {:uri "/some-id", :request-method :delete})))

;; (deftest scope
;;   (let [scope (ll/scope :foo
;;                         {:preprocessor
;;                          (fn [req]
;;                            (match req
;;                                   {:segments (["foo" & _] :seq)}
;;                                   (update req :segments pop)
;;                                   :else nil))}
;;                         (reify ll/Processable
;;                           (process [_ _] nil))
;;                         (reify ll/Processable
;;                           (process [_ _] "ok"))
;;                         (reify ll/Processable
;;                           (process [_ _] "last: not ok")))]
;;     (testing "scope not found"
;;       (let [req {:segments '("wrong" "smth")}
;;             resp (ll/process scope req)]
;;         (is (nil? resp))))
;;     (testing "matched"
;;       (let [req {:segments '("foo" "smth")}
;;             resp (ll/process scope req)]
;;         (is (= "ok" resp))))))

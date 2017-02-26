(ns darkleaf.router.additional-request-keys-test
  (:require [clojure.test :refer [deftest testing is]]
            [darkleaf.router :as r]))

(deftest request-keys
  (let [pages-controller {:index (fn [req] "index resp")
                          :show (fn [req] req)}
        pages (r/resources :pages :page pages-controller)
        handler (r/make-handler pages)
        returned-req (handler {:uri "/pages/1", :request-method :get})]
    (testing ::r/request-for
      (let [request-for (::r/request-for returned-req)]
        (is (= {:uri "/pages", :request-method :get}
               (request-for :index [:pages] {})))))
    (testing ::r/action
      (is (= :show (::r/action returned-req))))
    (testing ::r/scope
      (is (= [:page] (::r/scope returned-req))))
    (testing ::r/params
      (is (= {:page "1"} (::r/params returned-req))))))

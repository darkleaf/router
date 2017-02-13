(ns darkleaf.router-async-test
  (:require [darkleaf.router :as r]
            [clojure.test :refer [deftest is]]))

(deftest async-handler
  (let [pages-controller {:index (fn [req resp raise]
                                   (future
                                     (resp "index resp"))
                                   :something)}
        pages (r/resources :pages :page pages-controller)
        handler (r/make-handler pages)
        test-req {:uri "/pages", :request-method :get}
        done? (promise)
        check (fn [val]
                (is (= "index resp" val))
                (deliver done? true))]
    (handler test-req check check)
    (is (deref done? 100 false))))

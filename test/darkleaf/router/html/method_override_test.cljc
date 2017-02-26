(ns darkleaf.router.html.method-override-test
  (:require [clojure.test :refer [deftest is]]
            [darkleaf.router :as r]
            [darkleaf.router.html.method-override :as sut]))

(deftest wrap-method-override
  (let [controller {:put (fn [req] "success put")}
        routes (r/resource :star controller)
        handler (-> (r/make-handler routes)
                    (sut/wrap-method-override))]
    (is (= "success put"
           (handler {:request-method :post
                     :uri "/star"
                     :params {:_method "put"}})))))

#?(:clj
   (deftest wrap-method-override-async
     (let [controller {:put (fn [req respond raise] (respond "success put"))}
           routes (r/resource :star controller)
           handler (-> (r/make-handler routes)
                       (sut/wrap-method-override))
           respond (promise)
           exception (promise)]
       (handler {:request-method :post
                 :uri "/star"
                 :params {:_method "put"}}
                respond
                exception)
       (is (= "success put"
              (deref respond 100 nil)))
       (is (not (realized? exception))))))

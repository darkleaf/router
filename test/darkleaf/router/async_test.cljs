(ns darkleaf.router.async-test
  (:require [darkleaf.router :as r])
  (:import [goog.async nextTick])
  (:require-macros [cljs.test :refer [deftest is async]]))

(deftest async-handler
  (async done
         (let [pages-controller (r/controller
                                  (index [req resp raise]
                                    (nextTick #(resp "index resp"))
                                    :something))
               pages (r/resources :pages :page pages-controller)
               handler (r/make-handler pages)
               test-req {:uri "/pages", :request-method :get}
               check (fn [val]
                       (is (= "index resp" val))
                       (done))]
           (handler test-req check check))))

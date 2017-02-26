(ns darkleaf.router.async-test
  (:require [darkleaf.router :as r]
            [clojure.test :refer [deftest testing is]]))

(defn testing-hanlder [msg handler test-req test-resp]
  (testing msg
    (let [done? (promise)
          check (fn [val]
                  (is (= test-resp val))
                  (deliver done? true))]
      (handler test-req check check)
      (is (deref done? 100 false)))))

(deftest async-handler
  (let [response {:status 200
                  :headers {}
                  :body "index resp"}
        pages-controller {:index (fn [req resp raise]
                                   (future
                                     (resp response)))}
        pages (r/resources :pages :page pages-controller)
        handler (r/make-handler pages)]
    (testing-hanlder "found"
                     handler
                     {:uri "/pages", :request-method :get}
                     response)
    (testing-hanlder "not found"
                     handler
                     {:uri "/wrong/url", :request-method :get}
                     {:status 404, :headers {}, :body "404 error"})))

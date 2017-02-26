(ns darkleaf.router.async-test
  (:require [darkleaf.router :as r]
            [clojure.test :refer [deftest testing is]]))

(defn testing-handler [msg handler test-req test-resp]
  (testing msg
    (let [respond (promise)
          exception (promise)]
      (handler test-req respond exception)
      (is (= test-resp
             (deref respond 100 nil)))
      (is (not (realized? exception))))))

(deftest async-handler
  (let [response {:status 200
                  :headers {}
                  :body "index resp"}
        pages-controller {:index (fn [req resp raise]
                                   (future
                                     (resp response)))}
        pages (r/resources :pages :page pages-controller)
        handler (r/make-handler pages)]
    (testing-handler "found"
                     handler
                     {:uri "/pages", :request-method :get}
                     response)
    (testing-handler "not found"
                     handler
                     {:uri "/wrong/url", :request-method :get}
                     {:status 404, :headers {}, :body "404 error"})))

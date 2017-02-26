(ns darkleaf.router.html.method-override
  (:require [clojure.string :refer [lower-case]]))

(defn- method-override-request [request]
  (let [orig-method (:request-method request)
        new-method  (some-> request
                            :params
                            :_method
                            (lower-case)
                            (keyword))]
    (if (and (= :post orig-method)
             (some? new-method))
      (assoc request :request-method new-method)
      request)))

(defn wrap-method-override [handler]
  "Use it with ring.middleware.params/wrap-params
   and ring.middleware.keyword-params/wrap-keyword-params"
  (fn
    ([request]
     (handler (method-override-request request)))
    ([request respond raise]
     (handler (method-override-request request) respond raise))))

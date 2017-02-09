(ns darkleaf.router.action-impl
  (:require [darkleaf.router.keywords :as k]
            [darkleaf.router.protocols :as p]))

(defn- handle-with-middlewares [req handler]
  (if (empty? (k/middlewares req))
    (-> req
        (dissoc k/middlewares)
        (handler))
    (let [middleware (peek (k/middlewares req))
          new-req (update req k/middlewares pop)
          new-handler (middleware handler)]
      (recur new-req new-handler))))

(defrecord Action [id handle-impl fill-impl handler]
  p/Item
  (handle [_ req]
    (some-> req
            (handle-impl)
            (dissoc k/segments)
            (assoc k/action id)
            (handle-with-middlewares handler)))
  (fill [_ req]
    (when (and (= id (k/action req))
               (empty? (k/scope req)))
      (-> req
          (fill-impl)))))

(defn action
  ([id request-method handler]
   (let [handle-impl (fn [req]
                       (when (and (= request-method (:request-method req))
                                  (empty? (k/segments req)))
                          req))
         fill-impl (fn [req]
                     (-> req
                         (assoc :request-method request-method)))]
     (Action. id handle-impl fill-impl handler)))
  ([id request-method segment handler]
   (let [handle-impl (fn [req]
                       (when (and (= request-method (:request-method req))
                                  (= [segment] (k/segments req)))
                         req))
         fill-impl (fn [req]
                     (-> req
                         (assoc :request-method request-method)
                         (update k/segments conj segment)))]
     (Action. id handle-impl fill-impl handler))))

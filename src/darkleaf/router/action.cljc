(ns darkleaf.router.action
  (:require [clojure.set :as s]
            [darkleaf.router.keywords :as k]
            [darkleaf.router.protocols :as p]))

(deftype Action [id request-method segments handler]
  p/Item
  (process [_ req]
    (when (and (= request-method (:request-method req))
               (= segments (k/segments req)))
      (let [req (-> req
                    (assoc k/action id)
                    (dissoc k/segments))]
        [handler req])))
  (fill [_ req]
    (when (and (= id (k/action req))
               (empty? (k/scope req)))
      (-> req
          (assoc :request-method request-method)
          (update k/segments into segments)))))

(defn build [id request-method segments handler]
  (Action. id request-method segments handler))

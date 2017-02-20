(ns darkleaf.router.action
  (:require [clojure.string :refer [join]]
            [darkleaf.router.keywords :as k]
            [darkleaf.router.item :as i]))

;; todo: double from helpers ns
(defn- segments->uri [segments]
  (->> segments
       (map #(str "/" %))
       (join)))

(deftype Action [id request-method segments handler]
  i/Item
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
          (update k/segments into segments))))
  (explain [_ init]
    [(-> init
         (assoc :action id)
         (assoc-in [:req :request-method] request-method)
         (update-in [:req :uri] str (segments->uri segments)))]))

(defn build [id request-method segments handler]
  (Action. id request-method segments handler))

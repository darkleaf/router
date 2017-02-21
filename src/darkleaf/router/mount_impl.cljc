(ns darkleaf.router.mount-impl
  (:require [darkleaf.router.keywords :as k]
            [darkleaf.router.item :as i]
            [darkleaf.router.item-wrappers :as wrappers]))

(deftype App [item]
  i/Item
  (process [_ req]
    (as-> req <>
      (update <> k/request-for (fn [request-for]
                                (fn [action scope params]
                                  (request-for action
                                               (into (k/scope req) scope)
                                               (merge (k/params req) params)))))
      (update <> k/scope empty)
      (i/process item <>)))
  (fill [_ req]
    (i/fill item req))
  (explain [_ init]
    (i/explain item init)))

(defn mount [item & {:keys [segment middleware]}]
  (cond-> (App. item)
    middleware (wrappers/wrap-middleware middleware)
    segment (wrappers/wrap-segment segment)))

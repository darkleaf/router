(ns darkleaf.router.mount-impl
  (:require [darkleaf.router.keywords :as k]
            [darkleaf.router.protocols :as p]
            [darkleaf.router.wrapper-impl :refer [wrapper]]))

(deftype App [item segment]
  p/Item
  (process [_ req]
    (when (= segment (-> req k/segments peek))
      (as-> req r
        (update r k/request-for (fn [request-for]
                                  (fn [action scope params]
                                    (request-for action
                                                 (into (k/scope req) scope)
                                                 (merge (k/params req) params)))))
        (update r k/segments pop)
        (update r k/scope empty)
        (p/process item r))))
  (fill [_ req]
    (as-> req r
      (update r k/segments conj segment)
      (p/fill item r)))
  (explain [_ init]
    (as-> init i
      (update-in i [:req :uri] str "/" segment)
      (p/explain item i))))

(defn mount [item & {:keys [segment middleware]
                     :or {middleware identity}}]
  (wrapper middleware
           (App. item segment)))

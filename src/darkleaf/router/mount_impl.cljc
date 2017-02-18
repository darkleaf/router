(ns darkleaf.router.mount-impl
  (:require [darkleaf.router.keywords :as k]
            [darkleaf.router.protocols :as p]))

(deftype App [item segment]
  p/Item
  (process [_ req]
    (when (= segment (-> req k/segments peek))
      (as-> req r
        (update r k/segments pop)
        (update r k/scope empty)
        (p/process item r))))
  (fill [_ req]
    (as-> req r
      (update r k/segments conj segment)
      (p/fill item r))))

(defn mount [item & {:keys [segment middleware]
                     :or {middleware identity}}]
  (App. item segment))

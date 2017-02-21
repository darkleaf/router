(ns darkleaf.router.segment-impl
  (:require [darkleaf.router.keywords :as k]
            [darkleaf.router.item :as i]
            [darkleaf.router.url :as url]))

(deftype Segment [segment children]
  i/Item
  (process [_ req]
    (when (= segment (-> req k/segments peek))
      (-> req
          (update k/segments pop)
          (i/some-process children))))
  (fill [_ req]
    (-> req
        (update k/segments conj segment)
        (i/some-fill children)))
  (explain [_ init]
    (-> init
        (update-in [:req :uri] str "/" segment)
        (i/explain-all children))))

(defn ^{:style/indent :defn} segment [segment & children]
  (Segment. segment children))

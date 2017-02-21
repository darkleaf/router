(ns darkleaf.router.scope-impl
  (:require [darkleaf.router.keywords :as k]
            [darkleaf.router.item :as i]))

(deftype Scope [id children]
  i/Item
  (process [_ req]
    (-> req
        (update k/scope conj id)
        (i/some-process children)))
  (fill [_ req]
    (when (= id (-> req k/scope peek))
      (-> req
          (update k/scope pop)
          (i/some-fill children))))
  (explain [_ init]
    (-> init
        (update :scope conj id)
        (i/explain-all children))))

(defn ^{:style/indent :defn} scope [id & children]
  (Scope. id children))

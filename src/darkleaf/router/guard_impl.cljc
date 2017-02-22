(ns darkleaf.router.guard-impl
  (:require [darkleaf.router.item :as i]
            [darkleaf.router.keywords :as k]
            [darkleaf.router.args :as args]
            [darkleaf.router.url :as url]
            [darkleaf.router.item-wrappers :as wrappers]))

(deftype Guard [item id predicate]
  i/Item
  (process [_ req]
    (let [segment (-> req k/segments peek)]
      (when (predicate segment)
        (as-> req <>
          (update <> k/segments pop)
          (update <> k/params assoc id segment)
          (i/process item <>)))))
  (fill [_ req]
    (let [segment (-> req k/params id)]
      (when (predicate segment)
        (as-> req <>
          (update <> k/segments conj segment)
          (i/fill item <>)))))
  (explain [_ init]
    (let [encoded-id (url/encode id)]
      (as-> init <>
        (assoc-in <> [:params-kmap id] encoded-id)
        (update-in <> [:req :uri] str "{/" encoded-id "}")
        (i/explain item <>)))))

(defn ^{:style/indent :defn} guard [& args]
  (let [[id predicate
         {:keys [middleware]}
         children]
        (args/parse 2 args)]
    (cond-> (wrappers/composite children)
      middleware (wrappers/wrap-middleware middleware)
      :always (Guard. id predicate)
      :always (wrappers/wrap-scope id))))

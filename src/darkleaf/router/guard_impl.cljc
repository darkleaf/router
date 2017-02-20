(ns darkleaf.router.guard-impl
  (:require [darkleaf.router.item :as i]
            [darkleaf.router.keywords :as k]
            [darkleaf.router.args :as args]
            [darkleaf.router.url :as url]))

(deftype Guard [id predicate middleware children]
  i/Item
  (process [_ req]
    (let [segment (-> req k/segments peek)]
      (when (predicate segment)
        (-> req
            (update k/segments pop)
            (update k/params assoc id segment)
            (update k/scope conj id)
            (update k/middlewares conj middleware)
            (i/some-process children)))))
  (fill [_ req]
    (let [segment (-> req k/params id)]
      (when (and (= id (-> req k/scope peek))
                 (predicate segment))
        (-> req
            (update k/scope pop)
            (update k/segments conj segment)
            (i/some-fill children)))))
  (explain [_ init]
    (let [encoded-id (url/encode id)]
      (-> init
          (update :scope conj id)
          (assoc-in [:params-kmap id] encoded-id)
          (update-in [:req :uri] str "{/" encoded-id "}")
          (i/explain-all children)))))

(defn guard [& args]
  (let [[id predicate
         {:keys [middleware]
          :or {middleware identity}}
         children]
        (args/parse 2 args)]
    (Guard. id predicate middleware children)))

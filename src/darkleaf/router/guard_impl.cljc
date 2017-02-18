(ns darkleaf.router.guard-impl
  (:require [darkleaf.router.protocols :as p]
            [darkleaf.router.keywords :as k]
            [darkleaf.router.args :as args]))

(deftype Guard [id predicate middleware children]
  p/Item
  (process [_ req]
    (let [segment (-> req k/segments peek)]
      (when (predicate segment)
        (-> req
            (update k/segments pop)
            (update k/params assoc id segment)
            (update k/scope conj id)
            (update k/middlewares conj middleware)
            (p/some-process children)))))
  (fill [_ req]
    (let [segment (-> req k/params id)]
      (when (and (= id (-> req k/scope peek))
                 (predicate segment))
        (-> req
            (update k/scope pop)
            (update k/segments conj segment)
            (p/some-fill children))))))

(defn guard [& args]
  (let [[id predicate
         {:keys [middleware]
          :or {middleware identity}}
         children]
        (args/parse 2 args)]
    (Guard. id predicate middleware children)))

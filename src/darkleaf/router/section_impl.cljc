(ns darkleaf.router.section-impl
  (:require [darkleaf.router.protocols :as p]
            [darkleaf.router.keywords :as k]
            [darkleaf.router.args :as args]))

(deftype Section [id segment middleware children]
  p/Item
  (process [_ req]
    (when (= segment (-> req k/segments peek))
      (-> req
          (update k/segments pop)
          (update k/scope conj id)
          (update k/middlewares conj middleware)
          (p/some-process children))))
  (fill [_ req]
    (when (= id (peek (k/scope req)))
      (-> req
          (update k/scope pop)
          (update k/segments conj segment)
          (p/some-fill children)))))

(defn section [& args]
  (let [[id
         {:keys [middleware segment]
          :or {middleware identity
               segment (name id)}}
         children]
        (args/parse 1 args)]
    (Section. id segment middleware children)))

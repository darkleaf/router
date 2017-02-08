(ns darkleaf.router.scope
  (:require [darkleaf.router.keys :as k]
            [darkleaf.router.protocols :as p]))

(defrecord Scope [id handle-impl fill-impl middleware children]
  p/Item
  (handle [_ req]
    (some-> req
            (handle-impl)
            (update k/scope conj id)
            (update k/middlewares conj middleware)
            (p/some-handle children)))
  (fill [_ req]
    (when (= id (peek (k/scope req)))
      (-> req
          (update k/scope pop)
          (fill-impl)
          (p/some-fill children)))))

(defn scope
  ([id handle-impl fill-impl children]
   (scope id handle-impl fill-impl identity children))
  ([id handle-impl fill-impl middleware children]
   (let [children (remove nil? children)]
     (Scope. id handle-impl fill-impl middleware children))))

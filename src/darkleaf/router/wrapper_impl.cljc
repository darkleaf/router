(ns darkleaf.router.wrapper-impl
  (:require [darkleaf.router.keywords :as k]
            [darkleaf.router.protocols :as p]))

(defrecord Wrapper [middleware children]
  p/Item
  (handle [_ req]
    (-> req
        (update k/middlewares conj middleware)
        (p/some-handle children)))
  (fill [_ req]
    (p/some-fill req children)))

(defn wrapper [middleware & children]
  (Wrapper. middleware children))

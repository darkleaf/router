(ns darkleaf.router.wrapper-impl
  (:require [darkleaf.router.keywords :as k]
            [darkleaf.router.protocols :as p]))

(defrecord Wrapper [middleware children]
  p/Item
  (process [_ req]
    (-> req
        (update k/middlewares conj middleware)
        (p/some-process children)))
  (fill [_ req]
    (p/some-fill req children))
  (explain [_ init]
    (p/explain-all init children)))


(defn wrapper [middleware & children]
  (Wrapper. middleware children))

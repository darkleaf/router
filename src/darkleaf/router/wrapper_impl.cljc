(ns darkleaf.router.wrapper-impl
  (:require [darkleaf.router.keywords :as k]
            [darkleaf.router.item :as i]))

(defrecord Wrapper [middleware children]
  i/Item
  (process [_ req]
    (-> req
        (update k/middlewares conj middleware)
        (i/some-process children)))
  (fill [_ req]
    (i/some-fill req children))
  (explain [_ init]
    (i/explain-all init children)))

(defn wrapper [middleware & children]
  (Wrapper. middleware children))

(ns darkleaf.router.composite-impl
  (:require [darkleaf.router.item :as i]))

(defrecord Composite [children]
  i/Item
  (process [_ req]
    (i/some-process req children))
  (fill [_ req]
    (i/some-fill req children))
  (explain [_ init]
    (i/explain-all init children)))

(defn composite [& children]
  (Composite. children))

(ns darkleaf.router.composite-impl
  (:require [darkleaf.router.protocols :as p]))

(defrecord Composite [children]
  p/Item
  (process [_ req]
    (p/some-process req children))
  (fill [_ req]
    (p/some-fill req children)))

(defn composite [& children]
  (Composite. children))

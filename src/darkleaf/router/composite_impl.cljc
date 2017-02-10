(ns darkleaf.router.composite-impl
  (:require [darkleaf.router.protocols :as p]))

(defrecord Composite [children]
  p/Item
  (handle [_ req]
    (p/some-handle req children))
  (fill [_ req]
    (p/some-fill req children)))

(defn composite [& children]
  (Composite. children))

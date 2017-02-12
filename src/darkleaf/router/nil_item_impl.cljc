(ns darkleaf.router.nil-item-impl
  (:require [darkleaf.router.protocols :as p]))

(deftype NilItem []
  p/Item
  (process [_ _] nil)
  (fill [_ _] nil))

(defn nil-item []
  (NilItem.))

(ns darkleaf.router.nil-item-impl
  (:require [darkleaf.router.protocols :as p]))

(deftype NilItem []
  p/Item
  (process [_ _] nil)
  (fill [_ _] nil)
  (explain [_ _] []))

(defn nil-item []
  (NilItem.))

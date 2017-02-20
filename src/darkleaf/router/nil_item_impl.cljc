(ns darkleaf.router.nil-item-impl
  (:require [darkleaf.router.item :as i]))

(deftype NilItem []
  i/Item
  (process [_ _] nil)
  (fill [_ _] nil)
  (explain [_ _] []))

(defn nil-item []
  (NilItem.))

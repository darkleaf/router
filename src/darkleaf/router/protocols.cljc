(ns darkleaf.router.protocols
  (:require [darkleaf.router.keywords :as k]))

(defprotocol Item
  (handle [this req])
  (fill [this req-template]))

(defn some-handle [req xs]
  (some #(handle % req) xs))

(defn some-fill [req xs]
  (some #(fill % req) xs))

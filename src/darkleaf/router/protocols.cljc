(ns darkleaf.router.protocols
  (:require [darkleaf.router.keywords :as k]))

(defprotocol Item
  (process [this req]
    "return [req handler]")
  (fill [this req-template]
    "return req"))

(defn some-process [req xs]
  (some #(process % req) xs))

(defn some-fill [req xs]
  (some #(fill % req) xs))

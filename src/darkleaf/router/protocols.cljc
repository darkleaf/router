(ns darkleaf.router.protocols
  (:require [darkleaf.router.keywords :as k]))

(defprotocol Item
  (process [this req]
    "return [handler req]")
  (fill [this req-template]
    "return req")
  (explain [this init]
    "return [explanation]"))

(defn some-process [req xs]
  (some #(process % req) xs))

(defn some-fill [req xs]
  (some #(fill % req) xs))

(defn explain-all [init xs]
  (reduce (fn [acc item]
            (into acc (explain item init)))
          []
          xs))

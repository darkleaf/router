(ns darkleaf.router.item
  (:require [darkleaf.router.keywords :as k]))

(defprotocol Item
  (process [this req]
    "return [handler req]")
  (fill [this req-template]
    "return req")
  (explain [this init]
    "return [explanation]"))

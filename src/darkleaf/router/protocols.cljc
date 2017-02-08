(ns darkleaf.router.protocols
  (:require [darkleaf.router.keys :as k]))

(defprotocol Item
  (handle [this req])
  (fill [this req-template]))

(defn some-handle [req xs]
  (some #(handle % req) xs))

(defn some-fill [req xs]
  (some (fn [item]
          (let [req (fill item req)]
            (when (empty? (k/scope req))
               req)))
        xs))

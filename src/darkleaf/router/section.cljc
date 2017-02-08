(ns darkleaf.router.section
  (:require [darkleaf.router.keys :as k]
            [darkleaf.router.scope :refer [scope]]
            [darkleaf.router.util :as util]))

(defn section [& args]
  (let [[id
         {:keys [middleware]
          :or {middleware identity}}
         children]
        (util/parse-args 1 args)]
    (let [segment (name id)
          handle-impl (fn [req]
                        (when (= segment (peek (k/segments req)))
                          (update req k/segments pop)))
          fill-impl (fn [req]
                      (update req k/segments conj segment))]
      (scope id handle-impl fill-impl middleware children))))

(ns darkleaf.router.section-impl
  (:require [darkleaf.router.args :as args]
            [darkleaf.router.wrapper-impl :refer [wrapper]]
            [darkleaf.router.scope-impl :as scope-impl]
            [darkleaf.router.segment-impl :as segment-impl]))

(defn ^{:style/indent :defn} section [& args]
  (let [[id
         {:keys [middleware segment]
          :or {middleware identity
               segment (name id)}}
         children]
        (args/parse 1 args)]
    (segment-impl/segment segment
      (scope-impl/scope id
        (apply wrapper middleware children)))))

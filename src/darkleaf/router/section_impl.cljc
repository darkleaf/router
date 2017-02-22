(ns darkleaf.router.section-impl
  (:require [darkleaf.router.args :as args]
            [darkleaf.router.item-wrappers :as wrappers]))

(defn ^{:style/indent :defn} section [& args]
  (let [[id
         {:keys [middleware segment]
          :or {middleware identity
               segment (name id)}}
         children]
        (args/parse 1 args)]
    (cond-> (wrappers/composite children)
      middleware (wrappers/wrap-middleware middleware)
      segment (wrappers/wrap-segment segment)
      :always (wrappers/wrap-scope id))))

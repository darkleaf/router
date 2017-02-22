(ns darkleaf.router.group-impl
  (:require [darkleaf.router.args :as args]
            [darkleaf.router.item-wrappers :as wrappers]))

(defn ^{:style/indent :defn} group [& args]
  (let [[{:keys [middleware]}
         children]
        (args/parse 0 args)]
    (cond-> (wrappers/composite children)
      middleware (wrappers/wrap-middleware middleware))))

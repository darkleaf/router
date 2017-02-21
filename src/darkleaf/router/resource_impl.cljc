(ns darkleaf.router.resource-impl
  (:require [darkleaf.router.keywords :as k]
            [darkleaf.router.item :as i]
            [darkleaf.router.item-wrappers :as wrappers]
            [darkleaf.router.action :refer [action]]
            [darkleaf.router.args :as args]))

(defn ^{:style/indent :defn} resource [& args]
  (let [[singular-name controller
         {:keys [segment], :or {segment (name singular-name)}}
         nested]
        (args/parse 2 args)]
    (let [{:keys [middleware new create show edit update put destroy]} controller]
      (cond-> []
        new     (conj (action :new :get ["new"] new))
        create  (conj (action :create :post [] create))
        show    (conj (action :show :get [] show))
        edit    (conj (action :edit :get ["edit"] edit))
        update  (conj (action :update :patch [] update))
        put     (conj (action :put :put [] put))
        destroy (conj (action :destroy :delete [] destroy))
        :always (into nested)
        :always (wrappers/composite)
        middleware (wrappers/wrap-middleware middleware)
        segment (wrappers/wrap-segment segment)
        :always (wrappers/wrap-scope singular-name)))))

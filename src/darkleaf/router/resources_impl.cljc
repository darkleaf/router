(ns darkleaf.router.resources-impl
  (:require [darkleaf.router.keywords :as k]
            [darkleaf.router.item :as i]
            [darkleaf.router.item-wrappers :as wrappers]
            [darkleaf.router.action :refer [action]]
            [darkleaf.router.args :as args]
            [darkleaf.router.url :as url]))

(deftype MemberScope [item id]
  i/Item
  (process [_ req]
    (when-let [key-segment (-> req k/segments peek)]
      (as-> req <>
          (update <> k/segments pop)
          (assoc-in <> [k/params id] key-segment)
          (i/process item <>))))
  (fill [_ req]
    (when-let [key-segment (get-in req [k/params id])]
      (as-> req <>
          (update <> k/segments conj key-segment)
          (i/fill item <>))))
  (explain [_ init]
    (let [encoded-id (url/encode id)]
      (as-> init <>
          (assoc-in <> [:params-kmap id] encoded-id)
          (update-in <> [:req :uri] str "{/" encoded-id "}")
          (i/explain item <>)))))

(defn ^{:style/indent :defn} resources [& args]
  (let [[plural-name singular-name controller
         {:keys [segment nested]
          :or {segment (name plural-name)}}
         nested]
        (args/parse 3 args)]
    (let [{:keys [middleware member-middleware collection-middleware
                  index new create show edit update put destroy]} controller]
      (cond-> (wrappers/composite
               [(cond-> []
                  index (conj (action :index :get [] index))
                  :always (wrappers/composite)
                  collection-middleware (wrappers/wrap-middleware collection-middleware)
                  :always (wrappers/wrap-scope plural-name))
                (cond-> []
                  new (conj (action :new :get ["new"] new))
                  create (conj (action :create :post [] create))
                  :always (wrappers/composite)
                  collection-middleware (wrappers/wrap-middleware collection-middleware)
                  :always (wrappers/wrap-scope singular-name))
                (cond-> []
                  show    (conj (action :show :get [] show))
                  edit    (conj (action :edit :get ["edit"] edit))
                  update  (conj (action :update :patch [] update))
                  destroy (conj (action :destroy :delete [] destroy))
                  put     (conj (action :put :put [] put))
                  :always (into nested)
                  :always (wrappers/composite)
                  member-middleware (wrappers/wrap-middleware member-middleware)
                  :always (MemberScope. singular-name)
                  :always (wrappers/wrap-scope singular-name))])
        middleware (wrappers/wrap-middleware middleware)
        segment (wrappers/wrap-segment segment)))))

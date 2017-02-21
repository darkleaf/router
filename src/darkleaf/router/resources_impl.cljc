(ns darkleaf.router.resources-impl
  (:require [darkleaf.router.keywords :as k]
            [darkleaf.router.item :as i]
            [darkleaf.router.item-wrappers :as wrappers]
            [darkleaf.router.group-impl :refer [group]]
            [darkleaf.router.action :refer [action]]
            [darkleaf.router.args :as args]
            [darkleaf.router.url :as url]))

(defn- conj-action [acc id request-mehod segments controller]
  (if-let [handler (get controller id)]
    (conj acc (action/build id request-mehod segments handler))
    acc))

(deftype CollectionWithoutSegment [id children]
  i/Item
  (process [_ req]
    (-> req
        (update k/scope conj id)
        (i/some-process children)))
  (fill [_ req]
    (when (= id (-> req k/scope peek))
      (-> req
          (update k/scope pop)
          (i/some-fill children))))
  (explain [_ init]
    (-> init
        (update :scope conj id)
        (i/explain-all children))))

(deftype Collection [id segment children]
  i/Item
  (process [_ req]
    (when (= segment (-> req k/segments peek))
      (-> req
          (update k/segments pop)
          (update k/scope conj id)
          (i/some-process children))))
  (fill [_ req]
    (when (= id (peek (k/scope req)))
      (-> req
          (update k/scope pop)
          (update k/segments conj segment)
          (i/some-fill children))))
  (explain [_ init]
    (-> init
        (update :scope conj id)
        (update-in [:req :uri] str "/" segment)
        (i/explain-all children))))

(defn- collection-scope [id segment & children]
  (let [children (remove nil? children)]
    (cond
      (empty? children) (nil-item)
      segment (Collection. id segment children)
      :else (CollectionWithoutSegment. id children))))

(defn handle-segment [req segment]
  (when (= segment (-> req k/segments peek))
    (update req k/segments pop)))

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

(defn- controller-action [id request-mehod segments controller]
  (when-let [handler (get controller id)]
    (action/build id request-mehod segments handler)))

(defn ^{:style/indent :defn} resources [& args]
  (let [[plural-name singular-name controller
         {:keys [segment nested]
          :or {segment (name plural-name)}}
         nested]
        (args/parse 3 args)]
    (let [middleware (get controller :middleware identity)
          collection-middleware (get controller :collection-middleware identity)
          member-middleware (:member-middleware controller)
          {:keys [index new create show edit update put destroy]} controller]
      (group :middleware middleware
        (group :middleware collection-middleware
          (collection-scope plural-name segment
                            (controller-action :index :get [] controller))
          (collection-scope singular-name segment
                            (controller-action :new :get ["new"] controller)
                            (controller-action :create :post [] controller)))
        (cond-> []
          show    (conj (action :show :get [] show))
          edit    (conj (action :edit :get ["edit"] edit))
          update  (conj (action :update :patch [] update))
          destroy (conj (action :destroy :delete [] destroy))
          put     (conj (action :put :put [] put))
          :always (into nested)
          :always (wrappers/composite)
          member-middleware (wrapper-impl/wrapper member-middleware)
          :always (MemberScope. singular-name)
          :always (scope-impl/scope singular-name)
          segment (segment-impl/segment segment))))))

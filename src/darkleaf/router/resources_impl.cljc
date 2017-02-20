(ns darkleaf.router.resources-impl
  (:require [darkleaf.router.keywords :as k]
            [darkleaf.router.item :as i]
            [darkleaf.router.wrapper-impl :refer [wrapper]]
            [darkleaf.router.action :as action]
            [darkleaf.router.nil-item-impl :refer [nil-item]]
            [darkleaf.router.args :as args]
            [darkleaf.router.url :as url]))

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

(defn handle-key [req key]
  (when-let [key-segment (-> req k/segments peek)]
    (-> req
        (update k/segments pop)
        (assoc-in [k/params key] key-segment))))

(deftype MemberWithoutSegment [id children]
  i/Item
  (process [_ req]
    (some-> req
            (handle-key id)
            (update k/scope conj id)
            (i/some-process children)))
  (fill [_ req]
    (let [key-segment (get-in req [k/params id])]
      (when (some? key-segment)
        (-> req
            (update k/segments conj key-segment)
            (update k/scope pop)
            (i/some-fill children)))))
  (explain [_ init]
    (let [encoded-id (url/encode id)]
      (-> init
          (update :scope conj id)
          (assoc-in [:params-kmap id] encoded-id)
          (update-in [:req :uri] str "{/" encoded-id "}")
          (i/explain-all children)))))

(deftype Member [id segment children]
  i/Item
  (process [_ req]
    (some-> req
            (handle-segment segment)
            (handle-key id)
            (update k/scope conj id)
            (i/some-process children)))
  (fill [_ req]
    (let [key-segment (get-in req [k/params id])]
      (when (and (= id (-> req k/scope peek))
                 (some? key-segment))
        (-> req
            (update k/segments conj segment key-segment)
            (update k/scope pop)
            (i/some-fill children)))))
  (explain [_ init]
    (let [encoded-id (url/encode id)]
      (-> init
          (update :scope conj id)
          (assoc-in [:params-kmap id] encoded-id)
          (update-in [:req :uri] str "/" segment "{/" encoded-id "}")
          (i/explain-all children)))))

(defn- member-scope [singular-name segment & children]
  (let [children (remove nil? children)]
    (cond
      (empty? children) (nil-item)
      segment (Member. singular-name segment children)
      :else (MemberWithoutSegment. singular-name children))))

(defn- controller-action [id request-mehod segments controller]
  (when-let [handler (get controller id)]
    (action/build id request-mehod segments handler)))

(defn resources [& args]
  (let [[plural-name singular-name controller
         {:keys [segment nested]
          :or {segment (name plural-name)}}
         nested]
        (args/parse 3 args)]
    (let [middleware (get controller :middleware identity)
          collection-middleware (get controller :collection-middleware identity)
          member-middleware (get controller :member-middleware identity)]
      (wrapper middleware
               (wrapper collection-middleware
                        (collection-scope plural-name segment
                                          (controller-action :index :get [] controller))
                        (collection-scope singular-name segment
                                          (controller-action :new :get ["new"] controller)
                                          (controller-action :create :post [] controller)))
               (wrapper member-middleware
                        (apply member-scope singular-name segment
                               (controller-action :show :get [] controller)
                               (controller-action :edit :get ["edit"] controller)
                               (controller-action :update :patch [] controller)
                               (controller-action :destroy :delete [] controller)
                               (controller-action :put :put [] controller)
                               nested))))))

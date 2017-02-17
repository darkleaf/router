(ns darkleaf.router.resources-impl
  (:require [darkleaf.router.keywords :as k]
            [darkleaf.router.protocols :as p]
            [darkleaf.router.wrapper-impl :refer [wrapper]]
            [darkleaf.router.action :as action]
            [darkleaf.router.nil-item-impl :refer [nil-item]]
            [darkleaf.router.util :as util]))

(deftype CollectionWithoutSegment [id children]
  p/Item
  (process [_ req]
    (-> req
        (update k/scope conj id)
        (p/some-process children)))
  (fill [_ req]
    (when (= id (peek (k/scope req)))
      (-> req
          (update k/scope pop)
          (p/some-fill children)))))

(deftype Collection [id segment children]
  p/Item
  (process [_ req]
    (when (= segment (-> req k/segments peek))
      (-> req
          (update k/segments pop)
          (update k/scope conj id)
          (p/some-process children))))
  (fill [_ req]
    (when (= id (peek (k/scope req)))
      (-> req
          (update k/scope pop)
          (update k/segments conj segment)
          (p/some-fill children)))))

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
  (when-let [val (-> req k/segments peek)]
    (-> req
        (update k/segments pop)
        (assoc-in [k/params key] val))))

(deftype MemberWithoutSegment [id key children]
  p/Item
  (process [_ req]
    (some-> req
            (handle-key key)
            (update k/scope conj id)
            (p/some-process children)))
  (fill [_ req]
    (let [val (get-in req [k/params key])]
      (when (some? val)
        (-> req
            (update k/segments conj val)
            (update k/scope pop)
            (p/some-fill children))))))

(deftype Member [id key segment children]
  p/Item
  (process [_ req]
    (some-> req
            (handle-segment segment)
            (handle-key key)
            (update k/scope conj id)
            (p/some-process children)))
  (fill [_ req]
    (let [val (get-in req [k/params key])]
      (when (and (= id (-> req k/scope peek))
                 (some? val))
        (-> req
            (update k/segments conj segment val)
            (update k/scope pop)
            (p/some-fill children))))))

(defn- member-scope [singular-name segment & children]
  (let [children (remove nil? children)
        key (keyword (str (name singular-name) "-id"))]
    (cond
      (empty? children) (nil-item)
      segment (Member. singular-name key segment children)
      :else (MemberWithoutSegment. singular-name key children))))

(defn- controller-action [id request-mehod segments controller]
  (when-let [handler (get controller id)]
    (action/build id request-mehod segments handler)))

(defn resources [& args]
  (let [[plural-name singular-name controller
         {:keys [segment nested]
          :or {segment (name plural-name)}}
         nested]
        (util/parse-args 3 args)]
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

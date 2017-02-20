(ns darkleaf.router.resource-impl
  (:require [darkleaf.router.keywords :as k]
            [darkleaf.router.protocols :as p]
            [darkleaf.router.wrapper-impl :refer [wrapper]]
            [darkleaf.router.action :as action]
            [darkleaf.router.nil-item-impl :refer [nil-item]]
            [darkleaf.router.args :as args]))

(deftype ScopeWithoutSegment [id children]
  p/Item
  (process [_ req]
    (-> req
        (update k/scope conj id)
        (p/some-process children)))
  (fill [_ req]
    (when (= id (-> req k/scope peek))
      (-> req
          (update k/scope pop)
          (p/some-fill children))))
  (explain [_ init]
    (-> init
        (update :scope conj id)
        (p/explain-all children))))

(deftype Scope [id segment children]
  p/Item
  (process [_ req]
    (when (= segment (-> req k/segments peek))
      (-> req
          (update k/segments pop)
          (update k/scope conj id)
          (p/some-process children))))
  (fill [_ req]
    (when (= id (-> req k/scope peek))
      (-> req
          (update k/scope pop)
          (update k/segments conj segment)
          (p/some-fill children))))
  (explain [_ init]
    (-> init
        (update :scope conj id)
        (update-in [:req :uri] str "/" segment)
        (p/explain-all children))))

(defn- resource-scope [id segment & children]
  (let [children (remove nil? children)]
    (cond
      (empty? children) (nil-item)
      segment (Scope. id segment children)
      :else (ScopeWithoutSegment. id children))))

(defn- controller-action [id request-mehod segments controller]
  (when-let [handler (get controller id)]
    (action/build id request-mehod segments handler)))

(defn resource [& args]
  (let [[singular-name controller
         {:keys [segment], :or {segment (name singular-name)}}
         nested]
        (args/parse 2 args)]
    (let [middleware (get controller :middleware identity)]
      (wrapper
       middleware
       (apply resource-scope singular-name segment
              (controller-action :new :get ["new"] controller)
              (controller-action :create :post [] controller)
              (controller-action :show :get [] controller)
              (controller-action :edit :get ["edit"] controller)
              (controller-action :update :patch [] controller)
              (controller-action :put :put [] controller)
              (controller-action :destroy :delete [] controller)
              nested)))))

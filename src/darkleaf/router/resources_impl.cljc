(ns darkleaf.router.resources-impl
  (:require [darkleaf.router.keywords :as k]
            [darkleaf.router.scope-impl :refer [scope]]
            [darkleaf.router.action-impl :refer [action]]
            [darkleaf.router.composite-impl :refer [composite]]
            [darkleaf.router.util :as util]))

(defn- resources-collection-scope [scope-id segment middleware
                                   & children]
  (let [handle-impl (if segment
                      (fn [req]
                        (when (= segment (peek (k/segments req)))
                          (update req k/segments pop)))
                      identity)
        fill-impl (if segment
                    (fn [req]
                      (update req k/segments conj segment))
                    identity)]
    (scope scope-id handle-impl fill-impl middleware children)))

(defn- resources-member-scope [singular-name segment middleware & children]
  (let [id-key (keyword (str (name singular-name) "-id"))
        handle-impl (if segment
                      (fn [req]
                        (let [segments (k/segments req)
                              given-segment (peek segments)

                              segments (pop segments)
                              given-id (peek segments)

                              segments (pop segments)]
                          (when (and (= segment given-segment)
                                     (some? given-id))
                            (-> req
                                (assoc k/segments segments)
                                (assoc-in [k/params id-key] given-id)))))
                      (fn [req]
                        (let [segments (k/segments req)
                              given-id (peek segments)
                              segments (pop segments)]
                          (when (some? given-id)
                            (-> req
                                (assoc k/segments segments)
                                (assoc-in [k/params id-key] given-id))))))
        fill-impl (if segment
                    (fn [req]
                      (let [id (get-in req [k/params id-key])]
                        (update req k/segments conj segment id)))
                    (fn [req]
                      (let [id (get-in req [k/params id-key])]
                        (update req k/segments conj id))))]
    (scope singular-name handle-impl fill-impl middleware children)))

(defn resources [& args]
  (let [[plural-name singular-name controller
         {:keys [segment nested]
          :or {segment (name plural-name)}}
         nested]
        (util/parse-args 3 args)]
    (let [index-action   (when-let [handler (:index controller)]
                           (action :index :get handler))
          new-action     (when-let [handler (:new controller)]
                           (action :new :get "new" handler))
          create-action  (when-let [handler (:create controller)]
                           (action :create :post handler))
          show-action    (when-let [handler (:show controller)]
                           (action :show :get handler))
          edit-action    (when-let [handler (:edit controller)]
                           (action :edit :get "edit" handler))
          update-action  (when-let [handler (:update controller)]
                           (action :update :patch handler))
          put-action     (when-let [handler (:put controller)]
                           (action :put :put handler))
          destroy-action (when-let [handler (:destroy controller)]
                           (action :destroy :delete handler))
          middleware (get controller :middleware identity)
          collection-middleware (get controller :collection-middleware identity)
          member-middleware (get controller :member-middleware identity)
          middleware-for-collection (comp middleware collection-middleware)
          middleware-for-member (comp middleware member-middleware)]
      (composite
       (resources-collection-scope plural-name segment
                                   middleware-for-collection
                                   index-action)
       (resources-collection-scope singular-name segment
                                   middleware-for-collection
                                   new-action
                                   create-action)
       (apply resources-member-scope singular-name segment
              middleware-for-member
              (into nested
                    [edit-action
                     show-action
                     update-action
                     destroy-action
                     put-action])
              #_(into (vec nested)
                      [edit-action
                       show-action
                       update-action
                       put-action
                       destroy-action]))))))

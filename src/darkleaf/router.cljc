(ns darkleaf.router
  (:require [clojure.string :refer [split join]]))

;; ~~~~~~~~~~ Model ~~~~~~~~~~

(defprotocol Item
  (handle [this req])
  (fill [this req-template]))

(defn some-handle [req xs]
  (some #(handle % req) xs))

(defn some-fill [req xs]
  (some #(fill % req) xs))

(defrecord Composite [children]
  Item
  (handle [_ req]
    (some-handle req children))
  (fill [_ req]
    (some-fill req children)))

(defn- composite [& children]
  (Composite. children))

(defrecord Scope [id handle-impl fill-impl children]
  Item
  (handle [_ req]
    (some-> req
            (handle-impl)
            (update ::scope conj id)
            (some-handle children)))
  (fill [_ req]
    (when (= id (peek (::scope req)))
        (-> req
            (update ::scope pop)
            (fill-impl)
            (some-fill children)))))

(defn- scope [id handle-impl fill-impl children]
  (let [children (remove nil? children)]
    (Scope. id handle-impl fill-impl children)))

(defrecord Action [id handle-impl fill-impl handler]
  Item
  (handle [_ req]
    (some-> req
            (handle-impl)
            (dissoc ::segments)
            (assoc ::action id)
            (handler)))
  (fill [_ req]
    (when (= id (::action req))
      (-> req
          (fill-impl)))))

(defn- action
  ([id request-method handler]
   (let [handle-impl (fn [req]
                       (when (and (= request-method (:request-method req))
                                  (empty? (::segments req)))
                          req))
         fill-impl (fn [req]
                     (-> req
                         (assoc :request-method request-method)))]
     (Action. id handle-impl fill-impl handler)))
  ([id request-method segment handler]
   (let [handle-impl (fn [req]
                       (when (and (= request-method (:request-method req))
                                  (= [segment] (::segments req)))
                         req))
         fill-impl (fn [req]
                     (-> req
                         (assoc :request-method request-method)
                         (update ::segments conj segment)))]
     (Action. id handle-impl fill-impl handler))))

;; ~~~~~~~~~~~ Scopes ~~~~~~~~~~

(defn section [id & children]
 {:pre [(keyword? id)
        (every? #(or (nil? %) (satisfies? Item %)) children)]}
 (let [segment (name id)
       handle-impl (fn [req]
                     (when (= segment (peek (::segments req)))
                       (update req ::segments pop)))
       fill-impl (fn [req]
                   (update req ::segments conj segment))]
   (scope id handle-impl fill-impl children)))

;; ~~~~~~~~~~ Resources ~~~~~~~~~~

(defn- resources-collection-scope [scope-id segment & children]
  (let [handle-impl (if segment
                      (fn [req]
                        (when (= segment (peek (::segments req)))
                          (update req ::segments pop)))
                      identity)
        fill-impl (if segment
                    (fn [req]
                      (update req ::segments conj segment))
                    identity)]
    (scope scope-id handle-impl fill-impl children)))

(defn- resources-member-scope [plural-name singular-name segment & children]
  (let [id-key (keyword (str (name singular-name) "-id"))
        handle-impl (if segment
                      (fn [req]
                        (let [segments (::segments req)
                              given-segment (peek segments)

                              segments (pop segments)
                              given-id (peek segments)

                              segments (pop segments)]
                          (when (and (= segment given-segment)
                                     (some? given-id))
                            (-> req
                                (assoc ::segments segments)
                                (assoc-in [::params id-key] given-id)))))
                      (fn [req]
                        (let [segments (::segments req)
                              given-id (peek segments)
                              segments (pop segments)]
                          (when (some? given-id)
                            (-> req
                                (assoc ::segments segments)
                                (assoc-in [::params id-key] given-id))))))
        fill-impl (if segment
                    (fn [req]
                      (let [id (get-in req [::params id-key])]
                        (update req ::segments conj segment id)))
                    (fn [req]
                      (let [id (get-in req [::params id-key])]
                        (update req ::segments conj id))))]
    (scope singular-name handle-impl fill-impl children)))

(defn resources [plural-name singular-name controller
                 & {:keys [segment nested]
                    :or {segment (name plural-name)
                         nested []}}]
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
                         (action :destroy :delete handler))]
    (composite
     (resources-collection-scope plural-name segment
                                 index-action)
     (resources-collection-scope singular-name segment
                                 new-action
                                 create-action)
     (apply resources-member-scope plural-name singular-name segment
            (into nested
                  [show-action
                   edit-action
                   update-action
                   put-action
                   destroy-action])))))

;; ~~~~~~~~~~ Resource ~~~~~~~~~~

(defn- resource-scope [scope-id segment & children]
  (let [handle-impl (if segment
                      (fn [req]
                        (when (= segment (peek (::segments req)))
                          (update req ::segments pop)))
                      identity)
        fill-impl (if segment
                    (fn [req]
                      (update req ::segments conj segment))
                    identity)]
    (scope scope-id handle-impl fill-impl children)))

(defn resource [singular-name controller & {:keys [segment nested]
                                            :or {segment (name singular-name)
                                                 nested []}}]
  (let [new-action     (when-let [handler (:new controller)]
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
                         (action :destroy :delete handler))]
    (apply resource-scope singular-name segment
           new-action
           create-action
           show-action
           edit-action
           update-action
           put-action
           destroy-action
           nested)))

;; ~~~~~~~~~~ Helpers ~~~~~~~~~~

(def ^:private empty-segments #?(:clj clojure.lang.PersistentQueue/EMPTY
                                 :cljs cljs.core/PersistentQueue.EMPTY))
(def ^:private empty-scope    #?(:clj clojure.lang.PersistentQueue/EMPTY
                                 :cljs cljs.core/PersistentQueue.EMPTY))

(defn- uri->segments [uri]
  (into empty-segments
        (map second (re-seq #"/([^/]+)" uri))))

(defn- segments->uri [segments]
  (->> segments
       (map #(str "/" %))
       (join)))

(defn make-handler [item]
  (fn [req]
    (as-> req r
      (assoc r ::scope empty-scope)
      (assoc r ::params {})
      (assoc r ::segments (uri->segments (:uri r)))
      (handle item r))))

(defn make-request-for [item]
  (fn [action scope params]
    (let [scope (into empty-scope scope)]
        (as-> {::action action, ::scope scope, ::params params, ::segments empty-segments} r
          (fill item r)
          (assoc r :uri (segments->uri (::segments r)))
          (dissoc r ::action ::scope ::params ::segments)))))

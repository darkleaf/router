(ns darkleaf.router
  (:require [clojure.string :refer [split join]]


            [clojure.pprint :refer [pprint]]))

;; ~~~~~~~~~~ Utils ~~~~~~~~~~

(defn- parse-args [ordinal-args-count xs]
  "(=
     (parse-args 4 [1 2 3 4 :a 5 :b 6 7 8 9])
     [1 2 3 4 {:a 5, :b 6} [7 8 9]])"
  {:pre (< ordinal-args-count (count xs))}
  (let [ordinal-args (take ordinal-args-count xs)
        xs (drop ordinal-args-count xs)]
    (loop [opts {}
           xs xs]
      (if (keyword? (first xs))
        (do
          (assert (some? (second xs)))
          (recur (assoc opts (first xs) (second xs))
                 (drop 2 xs)))
        (-> ordinal-args
            (vec)
            (conj opts)
            (conj xs))))))

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

(defn composite [& children]
  (Composite. children))

(defn- add-middleware [req middleware]
  (if middleware
    (update req ::middlewares conj middleware)
    req))

(defrecord Scope [id handle-impl fill-impl middleware children]
  Item
  (handle [_ req]
    (some-> req
            (handle-impl)
            (update ::scope conj id)
            (add-middleware middleware)
            (some-handle children)))
  (fill [_ req]
    (when (= id (peek (::scope req)))
      (-> req
          (update ::scope pop)
          (fill-impl)
          (some-fill children)))))

(defn- scope
  ([id handle-impl fill-impl children]
   (scope id handle-impl fill-impl nil children))
  ([id handle-impl fill-impl middleware children]
   (let [children (remove nil? children)]
     (Scope. id handle-impl fill-impl middleware children))))

(defn- handle-with-middlewares [req handler]
  (if (empty? (::middlewares req))
    (-> req
        (dissoc ::middlewares)
        (handler))
    (let [middleware (peek (::middlewares req))
          new-req (update req ::middlewares pop)
          new-handler (middleware handler)]
      (recur new-req new-handler))))

(defrecord Action [id handle-impl fill-impl handler]
  Item
  (handle [_ req]
    (some-> req
            (handle-impl)
            (dissoc ::segments)
            (assoc ::action id)
            (handle-with-middlewares handler)))
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

(defrecord Wrapper [middleware children]
  Item
  (handle [_ req]
    (-> req
        (add-middleware middleware)
        (some-handle children)))
  (fill [_ req]
    (some-fill req children)))

(defn wrapper [middleware & children]
  (Wrapper. middleware children))

;; ~~~~~~~~~~~ Scopes ~~~~~~~~~~

(defn section [& args]
  (let [[id
         {:keys [middleware]}
         children]
        (parse-args 1 args)]
    (let [segment (name id)
          handle-impl (fn [req]
                        (when (= segment (peek (::segments req)))
                          (update req ::segments pop)))
          fill-impl (fn [req]
                      (update req ::segments conj segment))]
      (scope id handle-impl fill-impl middleware children))))

;; ~~~~~~~~~~ Resources ~~~~~~~~~~

(defn- resources-collection-scope [scope-id segment middleware
                                   & children]
  (let [handle-impl (if segment
                      (fn [req]
                        (when (= segment (peek (::segments req)))
                          (update req ::segments pop)))
                      identity)
        fill-impl (if segment
                    (fn [req]
                      (update req ::segments conj segment))
                    identity)]
    (scope scope-id handle-impl fill-impl middleware children)))

(defn- resources-member-scope [plural-name singular-name segment middleware & children]
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
    (scope singular-name handle-impl fill-impl middleware children)))

(defn resources [& args]
  (let [[plural-name singular-name controller
         {:keys [segment nested]
          :or {segment (name plural-name)}}
         nested]
        (parse-args 3 args)]
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
      (apply resources-member-scope plural-name singular-name segment
             middleware-for-member
             (into (vec nested)
                   [edit-action
                    show-action
                    update-action
                    put-action
                    destroy-action]))))))

;; ~~~~~~~~~~ Resource ~~~~~~~~~~

(defn- resource-scope [scope-id segment middleware & children]
  (let [handle-impl (if segment
                      (fn [req]
                        (when (= segment (peek (::segments req)))
                          (update req ::segments pop)))
                      identity)
        fill-impl (if segment
                    (fn [req]
                      (update req ::segments conj segment))
                    identity)]
    (scope scope-id handle-impl fill-impl middleware children)))

(defn resource [& args]
  (let [[singular-name controller
         {:keys [segment], :or {segment (name singular-name)}}
         nested]
        (parse-args 2 args)]
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
                           (action :destroy :delete handler))
          middleware (get controller :middleware)]
      (apply resource-scope singular-name segment middleware
             new-action
             create-action
             show-action
             edit-action
             update-action
             put-action
             destroy-action
             nested))))

;; ~~~~~~~~~~ Helpers ~~~~~~~~~~

(def ^:private empty-segments #?(:clj clojure.lang.PersistentQueue/EMPTY
                                 :cljs cljs.core/PersistentQueue.EMPTY))
(def ^:private empty-scope    #?(:clj clojure.lang.PersistentQueue/EMPTY
                                 :cljs cljs.core/PersistentQueue.EMPTY))
(def ^:private empty-middlewares [])

(defn- uri->segments [uri]
  (into empty-segments
        (map second (re-seq #"/([^/]+)" uri))))

(defn- segments->uri [segments]
  (->> segments
       (map #(str "/" %))
       (join)))

(defn make-request-for [item]
  (fn [action scope params]
    (let [scope (into empty-scope scope)]
        (as-> {::action action, ::scope scope, ::params params, ::segments empty-segments} r
          (fill item r)
          (assoc r :uri (segments->uri (::segments r)))
          (dissoc r ::action ::scope ::params ::segments)))))

(defn make-handler [item]
  (let [request-for (make-request-for item)]
    (fn [req]
      (as-> req r
        (assoc r ::request-for request-for)
        (assoc r ::scope empty-scope)
        (assoc r ::params {})
        (assoc r ::segments (uri->segments (:uri r)))
        (assoc r ::middlewares empty-middlewares)
        (handle item r)))))

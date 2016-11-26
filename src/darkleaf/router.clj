(ns darkleaf.router
  (:require [clojure.core.match :refer [match]]
            [clojure.string :refer [split join]]))

(defprotocol Item
  (handle [this req]
    "return response or nil")
  (fill [this req-template]
    "return new request or nil"))

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

(defrecord Scope [s-name handle-impl fill-impl children]
  Item
  (handle [_ req]
    (some-> req
            (handle-impl)
            (update ::scope conj s-name)
            (some-handle children)))
  (fill [_ req]
    (match req
           {::scope ([s-name & r] :seq)}
           (-> req
               (assoc ::scope r)
               (fill-impl)
               (some-fill children))
           :else nil)))

(defrecord Action [a-name handle-impl fill-impl handler]
  Item
  (handle [_ req]
    (some-> req
            (handle-impl)
            (assoc ::action a-name)
            (handler)))
  (fill [_ req]
    (match req
           {::action a-name}
           (-> req
               (fill-impl))
           :else nil)))

(defn static-action
  ([a-name request-method handler]
   (let [handle-impl (fn [req]
                       (match req
                              {:request-method request-method
                               ::segments (_ :guard empty?)}
                              req
                              :else nil))
         fill-impl (fn [req]
                     (-> req
                         (assoc :request-method request-method)))]
     (Action. a-name handle-impl fill-impl handler)))
  ([a-name request-method segment handler]
   (let [handle-impl (fn [req]
                      (match req
                             {:request-method request-method
                              ::segments ([segment] :seq)}
                             req
                             :else nil))
         fill-impl (fn [req]
                     (-> req
                         (assoc :request-method request-method)
                         (update ::segments conj segment)))]
     (Action. a-name handle-impl fill-impl handler))))

(defn- parse-uri [uri]
  (into clojure.lang.PersistentQueue/EMPTY
        (rest (split uri #"/"))))

(defn- add-segments [req]
  (assoc req ::segments (parse-uri (:uri req))))

(defn- add-defaults [req]
  (-> req
      (assoc ::scope [])
      (assoc ::params {})))

(defn make-handler [item]
  (fn [req]
    (as-> req r
      (add-segments r)
      (add-defaults r)
      (handle item r))))

(defn make-request-for [item]
  (fn [action scope params]
    (as-> {::action action, ::scope scope, ::params params, ::segments []} r
      (fill item r)
      (assoc r :uri (str "/" (join "/" (::segments r))))
      (dissoc r ::action ::scope ::params ::segments))))

(defn section [section-name & children]
  {:pre [(keyword? section-name)
         (some #(satisfies? Item %) children)]}
  (let [segment-name (name section-name)
        handle-impl (fn [req]
                       (match req
                              {::segments ([segment-name & _] :seq)}
                              (update req ::segments pop)
                              :else nil))
        fill-impl (fn [req]
                    (-> req
                        (update ::segments conj segment-name)))]
    (Scope. section-name handle-impl fill-impl children)))

(defn- resources-collection-scope [plural-name & children]
  (let [children (remove nil? children)]
    (apply section plural-name children)))

(defn- resources-member-scope [plural-name singular-name & children]
  (let [children (remove nil? children)
        segment-name (name plural-name)
        id-key (keyword (str (name singular-name) "-id"))
        handle-impl (fn [req]
                       (match req
                              {::segments ([segment-name id & _] :seq)}
                              (-> req
                                  (update ::segments pop)
                                  (update ::segments pop)
                                  (assoc-in [::params id-key] id))
                              :else nil))
        fill-impl (fn [req]
                    (-> req
                        (update ::segments
                                conj
                                segment-name
                                (get-in req [::params id-key]))))]

    (Scope. singular-name handle-impl fill-impl children)))

(defn resources [plural-name singular-name controller]
  (let [index-action   (when-let [index-handler (:index controller)]
                         (static-action :index :get index-handler))
        new-action     (when-let [new-handler (:new controller)]
                         (static-action :new :get "new" new-handler))
        create-action  (when-let [create-handler (:create controller)]
                         (static-action :create :post create-handler))
        show-action    (when-let [show-handler (:show controller)]
                         (static-action :show :get show-handler))
        edit-action    (when-let [edit-handler (:edit controller)]
                         (static-action :edit :get "edit" edit-handler))
        update-action  (when-let [update-handler (:update controller)]
                         (static-action :update :patch update-handler))
        destroy-action (when-let [destroy-handler (:destroy controller)]
                         (static-action :destroy :delete destroy-handler))]
    (composite
     (resources-collection-scope plural-name
                                 index-action
                                 new-action
                                 create-action)
     (resources-member-scope plural-name singular-name
                             show-action
                             edit-action
                             update-action
                             destroy-action))))

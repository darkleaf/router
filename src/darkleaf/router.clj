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

(defn static-segment-scope [s-name & children]
  (let [segment-name (name s-name)
        handle-impl (fn [req]
                       (match req
                              {::segments ([segment-name & _] :seq)}
                              (update req ::segments pop)
                              :else nil))
        fill-impl (fn [req]
                    (-> req
                        (update ::segments conj segment-name)))]
    (Scope. s-name handle-impl fill-impl children)))

(defrecord Wrapper [handle-impl fill-impl children]
  Item
  (handle [_ req]
    (some-> req
            (handle-impl)
            (some-handle children)))
  (fill [_ req]
    (some-> req
            (fill-impl)
            (some-fill children))))

(defn dynamic-segment-wrapper [segment-key & children]
 (let [handle-impl (fn [req]
                     (match req
                            {::segments ([segment & _] :seq)}
                            (-> req
                                (update ::segments pop)
                                (assoc-in [::params segment-key] segment))
                            :else nil))
       fill-impl (fn [req]
                   (when-let [segment-val (get-in req [::params segment-key])]
                     (update req ::segments conj segment-val)))]
   (Wrapper. handle-impl fill-impl children)))

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

(defn resources [r-name key controller]
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
    (apply static-segment-scope
           r-name
           (remove nil?
                   [index-action
                    new-action
                    create-action
                    (apply dynamic-segment-wrapper
                           key
                           (remove nil?
                                   [show-action
                                    edit-action
                                    update-action
                                    destroy-action]))]))))

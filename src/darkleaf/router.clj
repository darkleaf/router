(ns darkleaf.router
  (:require [clojure.core.match :refer [match]]
            [clojure.string :refer [split join]]))

(defprotocol Processable
  (process [this req]
    "return response or nil"))

#_(defrecord Composite [children]
    Processable
    (process [_ req]
      (some #(process % req) children)))

#_(defn composite [& children]
    (Composite. children))

(defrecord Scope [name preprocessor children]
  Processable
  (process [_ req]
    (when-let [preprocessed (preprocessor req)]
      (some #(process % preprocessed) children))))

(defn static-segment-scope [s-name & children]
  (let [segement-name (name s-name)
        preprocessor (fn [req]
                       (match req
                              {::segments ([segment-name & _] :seq)}
                              (update req ::segments pop)
                              :else nil))]
    (Scope. name preprocessor children)))

(defrecord Wrapper [preprocessor children]
  Processable
  (process [_ req]
    (when-let [preprocessed (preprocessor req)]
      (some #(process % preprocessed) children))))

(defn dynamic-segment-wrapper [segment-key & children]
  (let [preprocessor (fn [req]
                       (match req
                              {::segments ([segment & _] :seq)}
                              (-> req
                                  (update ::segments pop)
                                  (assoc-in [::params segment-key] segment))
                              :else nil))]
    (Wrapper. preprocessor children)))

(defrecord Endpoint [name preprocessor handler]
  Processable
  (process [_ req]
    (when-let [preprocessed (preprocessor req)]
     (handler preprocessed))))

(defn static-endpoint
  ([e-name request-method handler]
   (let [preprocessor (fn [req]
                        (match req
                               {:request-method request-method
                                ::segments (_ :guard empty?)}
                               req
                               :else nil))]
     (Endpoint. e-name preprocessor handler)))
  ([e-name method segment handler]
   (let [preprocessor (fn [req]
                        (match req
                               {:request-method request-method
                                ::segments ([segment] :seq)}
                               req
                               :else nil))]
     (Endpoint. e-name preprocessor handler))))


(defn- add-segments [req]
  ;; TODO: write own parser with queue
  (assoc req ::segments (apply list (rest (split (:uri req) #"/")))))

(defn make-handler [processable]
  (fn [req]
    (as-> req r
      (add-segments r)
      (process processable r))))

(defn resources [r-name key controller]
  (let [index-endpoint   (when-let [index-handler (:index controller)]
                           (static-endpoint :index :get index-handler))
        new-endpoint     (when-let [new-handler (:new controller)]
                           (static-endpoint :index :get "new" new-handler))
        create-endpoint  (when-let [create-handler (:create controller)]
                           (static-endpoint :create :post create-handler))
        show-endpoint    (when-let [show-handler (:show controller)]
                           (static-endpoint :show :get show-handler))
        edit-endpoint    (when-let [edit-handler (:edit controller)]
                           (static-endpoint :edit :get "edit" edit-handler))
        update-endpoint  (when-let [update-handler (:update controller)]
                           (static-endpoint :update :patch update-handler))
        destroy-endpoint (when-let [destroy-handler (:destroy controller)]
                           (static-endpoint :destroy :delete destroy-handler))]
    (apply static-segment-scope
           r-name
           (remove nil?
                   [index-endpoint
                    new-endpoint
                    create-endpoint
                    (apply dynamic-segment-wrapper
                           key
                           (remove nil?
                                   [show-endpoint
                                    edit-endpoint
                                    update-endpoint
                                    destroy-endpoint]))]))))

(ns darkleaf.router.helpers
  (:require [clojure.string :refer [split join]]
            [darkleaf.router.keywords :as k]
            [darkleaf.router.protocols :as p]))

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
    (let [scope (into empty-scope scope)
          initial-req {k/action action
                       k/scope scope
                       k/params params
                       k/segments empty-segments}]
      (when-let [req (p/fill item initial-req)]
        (assert (-> req k/scope empty?))
        (as-> req r
          (assoc r :uri (segments->uri (k/segments r)))
          (dissoc r k/action k/scope k/params k/segments))))))

(defn- process [item req]
  (let [req (assoc req
                   k/scope empty-scope
                   k/params {}
                   k/segments (uri->segments (:uri req))
                   k/middlewares empty-middlewares)]
    (when-let [[handler req] (p/process item req)]
      (assert (-> req k/segments empty?))
      (assert (-> req k/action keyword?))
      (let [middleware (apply comp (k/middlewares req))
            handler (middleware handler)
            req (dissoc req
                        k/middlewares
                        k/segments)]
        [handler req]))))

(def not-found
  {:status 404
   :headers {}
   :body "404 error"})

(defn make-handler [item]
  (let [request-for (make-request-for item)
        pre-process (fn [req]
                      (assoc req k/request-for request-for))]
    (fn
      ([req]
       (let [req (pre-process req)]
         (if-let [[handler req] (process item req)]
           (handler req)
           not-found)))
      ([req resp raise]
       (let [req (pre-process req)]
         (if-let [[handler req] (process item req)]
           (handler req resp raise)
           (resp not-found)))))))

(defn explain [item]
  (let [init {:action nil
              :scope []
              :params-keys #{}
              :req {:uri ""
                    :request-method nil}}
        explanations (p/explain item init)]
    explanations))

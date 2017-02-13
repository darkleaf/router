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
        (as-> req r
          (assoc r :uri (segments->uri (k/segments r)))
          (dissoc r k/action k/scope k/params k/segments))))))


(defn make-handler [item]
  (let [request-for (make-request-for item)
        set-init (fn [req]
                   (assoc req
                          k/request-for request-for
                          k/scope empty-scope
                          k/params {}
                          k/segments (uri->segments (:uri req))
                          k/middlewares empty-middlewares))]
    (fn
      ([req]
       (let [req (set-init req)
             [req handler] (p/process item req)]
         (handler req)))
      ([req resp raise]
       (let [req (set-init req)
             [req handler] (p/process item req)]
         (handler req resp raise))))))

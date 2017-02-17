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
                   k/middlewares empty-middlewares)
        [handler req] (p/process item req)
        ;; может вернуться nil, если нет подходящего маршрута
        ;; надо это как-то обрабатывать
        _ (assert (-> req k/segments empty?))
        _ (assert (-> req k/action keyword?))
        middleware (apply comp (k/middlewares req))
        handler (middleware handler)
        req (dissoc req
                    k/middlewares
                    k/segments)]
    [handler req]))

(defn make-handler [item]
  (let [request-for (make-request-for item)
        post-process (fn [req]
                       (assoc req k/request-for request-for))]
    (fn
      ([req]
       (let [[handler req] (process item req)
             req (post-process req)]
         (handler req)))
      ([req resp raise]
       (let [[handler req] (process item req)
             req (post-process req)]
         (handler req resp raise))))))

(ns darkleaf.router.helpers
  (:require [clojure.string :refer [split join]]
            [darkleaf.router.keys :as k]
            [darkleaf.router.protocols :as protocols]))

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
      (as-> {k/action action
             k/scope scope
             k/params params
             k/segments empty-segments} r
        (protocols/fill item r)
        (assoc r :uri (segments->uri (k/segments r)))
        (dissoc r k/action k/scope k/params k/segments)))))

(defn make-handler [item]
  (let [request-for (make-request-for item)]
    (fn [req]
      (as-> req r
        (assoc r k/request-for request-for)
        (assoc r k/scope empty-scope)
        (assoc r k/params {})
        (assoc r k/segments (uri->segments (:uri r)))
        (assoc r k/middlewares empty-middlewares)
        (protocols/handle item r)))))

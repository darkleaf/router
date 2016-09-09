(ns darkleaf.router
  (:require [clojure.core.match :refer [match]])
  (:require [clojure.string :refer [split]]))

(defrecord Route [name
                  vars pattern
                  handler scope])

(defn route
  [name & {:keys [vars pattern
                  handler]
           :or {vars #{}
                pattern {}}}]
  {:pre [(and
          (keyword? name)
          (set? vars)
          (map? pattern)
          (fn? handler))]}
  (Route. name
          vars pattern
          handler '()))

(defn- merge-segments-shapes [parent-segments child-segments]
  (into parent-segments child-segments))

(defn- merge-map-shapes [parent-map-pattern child-map-pattern]
  (reduce-kv (fn [result k v]
               (assoc result k
                      (cond
                        (and (map? (k result)) (map? v)) (merge-map-shapes (k result) v)
                        (not (contains? result k)) v
                        :else (throw (java.lang.IllegalArgumentException. "can't merge patterns: parent conrain child key")))))
             parent-map-pattern
             child-map-pattern))

(defn- merge-request-shapes [parent child]
  (let [parent-segments (get parent :segments [])
        child-segments (get child :segments [])

        parent-map (dissoc parent :segments)
        child-map (dissoc child :segments)

        result-segments (merge-segments-shapes parent-segments child-segments)
        result-map-pattern (merge-map-shapes parent-map child-map)]
    (assoc result-map-pattern :segments result-segments)))

(defn scope [s-name
             {:keys [vars pattern]
              :or {vars #{}
                   pattern {}}}
             & routes]
  {:pre [(and
          (keyword? s-name)
          (set? vars)
          (map? pattern))]}
  (map
   (fn [route]
     (-> route
         (update :scope conj s-name)
         (update :vars into vars)
         (update :pattern #(merge-request-shapes pattern %))))
   (flatten routes)))

(defn- build-params-map [route]
  (let [symbols (:vars route)
        m-keys (vec (map keyword symbols))
        m-vals (vec symbols)]
    `(zipmap ~m-keys ~m-vals)))

(defn- build-pattern-row [route]
  [(:pattern route)])

(defn- route->match-clause [route]
  (let [pattern-row (build-pattern-row route)
        action [route (build-params-map route)]]
    (list pattern-row action)))

(defmacro build-matcher [routes-var-name]
  `(fn [req#]
     (match
      [req#]
      ~@(let [routes (var-get (resolve routes-var-name))]
          (mapcat route->match-clause routes)))))

(defn- assoc-our-keys [req]
  (assoc req :segments (vec (rest (split (:uri req) #"/")))))

(defn build-handler [matcher]
  (fn [-req]
    (let [req (assoc-our-keys -req)
          [route params] (matcher req)
          route-handler (:handler route)
          req-with-info (assoc req
                               :route-params params
                               :matched-route route)]
      (route-handler req-with-info))))

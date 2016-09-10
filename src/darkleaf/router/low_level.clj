(ns darkleaf.router.low-level
  (:require [clojure.core.match :refer [match]]))

(defrecord Route [name
                  vars pattern template
                  handler scope])

(defn route
  [name & {:keys [vars pattern template handler]
           :or {vars #{}
                pattern {}
                template pattern}}]
  {:pre [(and
          (keyword? name)
          (set? vars)
          (map? pattern)
          (map? template)
          (fn? handler))]}
  (Route. name
          vars pattern template
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
             {:keys [vars pattern template]
              :or {vars #{}
                   pattern {}
                   template pattern}}
             & routes]
  {:pre [(and
          (keyword? s-name)
          (set? vars)
          (map? pattern)
          (map? template))]}
  (map
   (fn [route]
     (-> route
         (update :scope conj s-name)
         (update :vars into vars)
         (update :pattern #(merge-request-shapes pattern %))
         (update :template #(merge-request-shapes template %))))
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

(defmacro build-reverse-matcher [routes-var-name]
  (let [r-params-symbol (gensym 'r-params)]
    `(fn [r-name# r-scope# ~r-params-symbol]
       (case [r-name# r-scope#]
         ~@(let [routes (var-get (resolve routes-var-name))]
             (mapcat
              (fn [route]
                (list
                 [(:name route) (:scope route)]
                 `(let [{:keys [~@(:vars route)]} ~r-params-symbol]
                       ~(:template route))))
              routes))))))

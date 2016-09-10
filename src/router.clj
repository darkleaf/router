(ns darkleaf.router
  (:require [darkleaf.router.low-level :refer :all])
  (:require [clojure.string :refer [split]]))

(defn prefix [p-name & routes]
  (scope p-name
         {:pattern {:segments [(name p-name)]}}
         routes))

(defn resources [rs-name controller]
  (prefix rs-name
          (cond-> ()
            (:index controller) (conj (route :index
                                             :pattern {:request-method :get}
                                             :handler (:index controller)))
            (:create controller) (conj (route :create
                                              :pattern {:request-method :post}
                                              :handler (:create controller)))
            (:show controller)  (conj (route :show
                                             :vars '#{id}
                                             :pattern '{:segments [id] :request-method :get}
                                             :handler (:show controller))))))

(defn- assoc-our-keys [req]
  (assoc req :segments (vec (rest (split (:uri req) #"/")))))

(defmacro build-handler [routes-var-name]
  `(let [matcher# (build-matcher ~routes-var-name)]
     (fn [-req#]
       (let [req# (assoc-our-keys -req#)
             [route# params#] (matcher# req#)
             route-handler# (:handler route#)
             req-with-info# (assoc req#
                                   :route-params params#
                                   :matched-route route#)]
         (route-handler# req-with-info#)))))

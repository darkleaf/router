(ns darkleaf.router
  (:require [darkleaf.router.low-level :refer :all])
  (:require [clojure.string :refer [split join]]))

(defn build-routes [& routes]
  (combine-routes routes))

(defn action [request-method action-name handler]
  (route action-name
         :pattern {:request-method request-method
                   :segments [(name action-name)]}
         :handler handler))

(defn wildcard [request-method wildcard-name handler]
  (route wildcard-name
         :vars '#{rest}
         :pattern {:request-method request-method
                   :segments '[& rest]}
         :template {:request-method request-method
                    :segments 'rest}
         :handler handler))

(defn root [handler]
  (route :root
         :pattern {:request-method :get
                   :segments []}
         :handler handler))

#_(defn not-found [handler]
    (route :not-found
           :handler handler))

#_(defn prefix [p-name & routes]
    (scope p-name
           {:pattern {:segments [(name p-name)]}}
           routes))

#_(defn resources [rs-name controller]
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


(defn- prepare-request [req]
  (assoc req :segments (vec (rest (split (:uri req) #"/")))))

(defn matcher->handler [matcher]
  (fn [original-req]
    (let [req (prepare-request original-req)
          [route params] (matcher req)
          route-handler (:handler route)
          req-with-info (assoc req
                               :route-params params
                               :matched-route route)]
      (route-handler req-with-info))))

(defmacro build-handler [routes-var-name]
  `(let [matcher# (build-matcher ~routes-var-name)]
     (matcher->handler matcher#)))

(defn- prepare-reverse-request [req]
  (assoc req :uri (str "/" (join "/" (:segments req)))))

;;TODO check with matcher
(defn matchers->request-for [matcher reverse-matcher]
  (letfn [(request-for
            ([r-name r-scope] (request-for r-name r-scope {}))
            ([r-name r-scope r-params]
             (-> (reverse-matcher r-name r-scope r-params)
                 prepare-reverse-request)))]
    request-for))

(defmacro build-request-for [routes-var-name]
  `(let [matcher# (build-matcher ~routes-var-name)
         reverse-matcher# (build-reverse-matcher ~routes-var-name)]
     (matchers->request-for matcher# reverse-matcher#)))

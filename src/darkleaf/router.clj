(ns darkleaf.router
  (:require [darkleaf.router.low-level :refer :all])
  (:require [clojure.string :refer [split join]]))

(defn build-routes [& routes]
  (combine-routes routes))

(defn action [request-method action-name handler]
  (route action-name
         :pattern {:request-method request-method, :segments [(name action-name)]}
         :template {:request-method request-method, :segments [(name action-name)]}
         :handler handler))

(defn wildcard [request-method handler]
  (route :wildcard
         :vars '#{wildcard}
         :pattern {:request-method request-method, :segments '[& wildcard]}
         :template {:request-method request-method, :segments '[~@wildcard]}
         :handler handler))

(defn root [handler]
  (route :root
         :pattern {:request-method :get, :segments []}
         :template {:request-method :get, :segments []}
         :handler handler))

(defn not-found [handler]
  (route :not-found
         :pattern {}
         :template {}
         :handler handler))

(defn section [s-name & routes]
  (scope s-name
         {:pattern {:segments [(name s-name)]}
          :template {:segments [(name s-name)]}}
         routes))

(defn resources [rs-name controller]
  (section rs-name
           (cond-> '()
             (contains? controller :index)
             (conj (route :index
                          :pattern {:request-method :get}
                          :template {:request-method :get}
                          :handler (:index controller)))

             (contains? controller :new)
             (conj (route :new
                          :pattern {:request-method :get, :segments ["new"]}
                          :template {:request-method :get, :segments ["new"]}
                          :handler (:new controller)))

             (contains? controller :create)
             (conj (route :create
                          :pattern {:request-method :post}
                          :template {:request-method :post}
                          :handler (:create controller)))

             (contains? controller :show)
             (conj (route :show
                          :vars '#{id}
                          :pattern '{:request-method :get, :segments [id]}
                          :template '{:request-method :get, :segments [~id]}
                          :handler (:show controller)))

             (contains? controller :edit)
             (conj (route :edit
                          :vars '#{id}
                          :pattern '{:request-method :get, :segments [id "edit"]}
                          :template '{:request-method :get, :segments [~id "edit"]}
                          :handler (:edit controller)))

             (contains? controller :update)
             (conj (route :update
                          :vars '#{id}
                          :pattern '{:request-method :patch, :segments [id]}
                          :template '{:request-method :patch, :segments [~id]}
                          :handler (:update controller)))

             (contains? controller :destroy)
             (conj (route :destroy
                          :vars '#{id}
                          :pattern '{:request-method :delete, :segments [id]}
                          :template '{:request-method :delete, :segments [~id]}
                          :handler (:destroy controller))))))

(defn resource [r-name controller]
  (section r-name
           (cond-> '()
             (contains? controller :new)
             (conj (route :new
                          :pattern {:request-method :get, :segments ["new"]}
                          :template {:request-method :get, :segments ["new"]}
                          :handler (:new controller)))

             (contains? controller :create)
             (conj (route :create
                          :pattern {:request-method :post}
                          :template {:request-method :post}
                          :handler (:create controller)))

             (contains? controller :show)
             (conj (route :show
                          :pattern '{:request-method :get}
                          :template '{:request-method :get}
                          :handler (:show controller)))

             (contains? controller :edit)
             (conj (route :edit
                          :pattern '{:request-method :get, :segments ["edit"]}
                          :template '{:request-method :get, :segments ["edit"]}
                          :handler (:edit controller)))

             (contains? controller :update)
             (conj (route :update
                          :pattern '{:request-method :patch}
                          :template '{:request-method :patch}
                          :handler (:update controller)))

             (contains? controller :destroy)
             (conj (route :destroy
                          :pattern '{:request-method :delete}
                          :template '{:request-method :delete}
                          :handler (:destroy controller))))))

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
  (-> req
      (assoc :uri (str "/" (join "/" (:segments req))))
      (dissoc :segments)))

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

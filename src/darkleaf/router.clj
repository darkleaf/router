(ns darkleaf.router
  (:require [darkleaf.router.low-level :refer :all])
  (:require [clojure.string :refer [split join]]))

(defn build-routes [& routes]
  (combine-routes routes))

(defn action
  ([action-name handler]
   (action :get action-name handler))
  ([request-method action-name handler]
   (route action-name
          :pattern {:request-method request-method, :segments [(name action-name)]}
          :template {:request-method request-method, :segments [(name action-name)]}
          :handler handler)))

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

(defn guard [g-name predicate & routes]
  (let [name-symbol (-> g-name name symbol)]
    (scope g-name
           {:vars #{name-symbol}
            :pattern {:segments [(list name-symbol :guard predicate)]}
            :template {:segments [(list 'clojure.core/unquote name-symbol)]}}
           routes)))

(defn wrap-handler [middleware & routes]
  (map
   #(update % :handler middleware)
   (flatten routes)))

(defn- collection-routes [rs-name controller additional-routes]
  (wrap-handler
   (get controller :middleware identity)
   (scope rs-name
          {:pattern {:segments [(name rs-name)]}
           :template {:segments [(name rs-name)]}}
          (cond-> []
            (contains? controller :index)
            (conj (route :index
                         :pattern {:request-method :get, :segments []}
                         :template {:request-method :get, :segments []}
                         :handler (:index controller)))

            (contains? controller :new)
            (conj (route :new
                         :pattern {:request-method :get, :segments ["new"]}
                         :template {:request-method :get, :segments ["new"]}
                         :handler (:new controller)))

            (contains? controller :create)
            (conj (route :create
                         :pattern {:request-method :post, :segments []}
                         :template {:request-method :post, :segments []}
                         :handler (:create controller))))
          additional-routes)))

(defn- member-routes [rs-name id-symbol controller additional-routes]
  (wrap-handler
   (comp (get controller :member-middleware identity)
         (get controller :middleware identity))
   (scope rs-name
          {:vars #{id-symbol}
           :pattern {:segments [(name rs-name) id-symbol]}
           :template {:segments [(name rs-name) (list 'clojure.core/unquote id-symbol)]}}
          ;;#=> '{:segments ["page" ~page-id]} for rs-name - :page, id-symbol - 'page-id
          (cond-> []
            (contains? controller :show)
            (conj (route :show
                         :pattern {:request-method :get, :segments []}
                         :template {:request-method :get, :segments []}
                         :handler (:show controller)))

            (contains? controller :edit)
            (conj (route :edit
                         :pattern {:request-method :get, :segments ["edit"]}
                         :template {:request-method :get, :segments ["edit"]}
                         :handler (:edit controller)))

            (contains? controller :update)
            (conj (route :update
                         :pattern {:request-method :patch, :segments []}
                         :template {:request-method :patch, :segments []}
                         :handler (:update controller)))

            (contains? controller :destroy)
            (conj (route :destroy
                         :pattern {:request-method :delete, :segments []}
                         :template {:request-method :delete, :segments []}
                         :handler (:destroy controller))))
          additional-routes)))

(defn resources [rs-name id-symbol controller
                 & {collection-rs :collection, member-rs :member
                    :or {collection-rs [], member-rs []}}]
  [(collection-routes rs-name controller collection-rs)
   (member-routes rs-name id-symbol controller member-rs)])

(defn resource [r-name controller & inner-routes]
  (wrap-handler
   (get controller :middleware identity)
   (scope r-name
          {:pattern {:segments [(name r-name)]}
           :template {:segments [(name r-name)]}}
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
                         :handler (:destroy controller))))
          inner-routes)))

;; ---------- builders ----------

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

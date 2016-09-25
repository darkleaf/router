(ns darkleaf.router-test
  (:require [clojure.test :refer :all]
            [darkleaf.router :refer :all]))

(defn- build-fake-action [action-name]
  (fn [req] {:action action-name, :req req}))

(def pages-controller {:index (build-fake-action :index)
                       :show (build-fake-action :show)})

(def account-controller {:show (build-fake-action :show)
                         :update (build-fake-action :update)
                         :edit (build-fake-action :edit)})

(def admin-pages-controller {:index (build-fake-action :index)
                             :create (build-fake-action :create)
                             :new (build-fake-action :new)
                             :update (build-fake-action :update)
                             :edit (build-fake-action :edit)
                             :destroy (build-fake-action :destroy)})

(def admin-page-commments-controller {:index (build-fake-action :index)
                                      :create (build-fake-action :create)
                                      :new (build-fake-action :new)
                                      :update (build-fake-action :update)
                                      :edit (build-fake-action :edit)
                                      :destroy (build-fake-action :destroy)})

(defn admin-middleware [handler]
  (fn [req]
    (-> req
        (assoc :admin true)
        handler)))

(def routes
  (build-routes
   (root (:index pages-controller))
   (resources :pages pages-controller)
   (resource :account account-controller)
   (section :taxonomy
            (wildcard (build-fake-action :taxonomy)))
   (section :admin
            :middleware admin-middleware
            (root (:index admin-pages-controller))
            (resources :pages admin-pages-controller)
            (nested-for :pages 'page-id
                        (resources :comments admin-page-comments-controller)))
   (not-found (build-fake-action :not-found))))


#_(deftest test-handler
    (let [handler (build-handler matcher)
          request {:uri "/pages/about", :request-method :get}
          response (handler request)]
      (is (= :page (get-in response [:matched-route :name])))
      (is (= ["pages" "about"] (:segments response)))
      (is (contains? response :matched-route))
      (is (= {:slug "about"} (:route-params response)))))

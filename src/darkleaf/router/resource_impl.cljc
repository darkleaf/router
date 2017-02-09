(ns darkleaf.router.resource-impl
  (:require [darkleaf.router.keywords :as k]
            [darkleaf.router.scope-impl :refer [scope]]
            [darkleaf.router.action-impl :refer [action]]
            [darkleaf.router.util :as util]))

(defn- resource-scope [scope-id segment middleware & children]
  (let [handle-impl (if segment
                      (fn [req]
                        (when (= segment (peek (k/segments req)))
                          (update req k/segments pop)))
                      identity)
        fill-impl (if segment
                    (fn [req]
                      (update req k/segments conj segment))
                    identity)]
    (scope scope-id handle-impl fill-impl middleware children)))

(defn resource [& args]
  (let [[singular-name controller
         {:keys [segment], :or {segment (name singular-name)}}
         nested]
        (util/parse-args 2 args)]
    (let [new-action     (when-let [handler (:new controller)]
                           (action :new :get "new" handler))
          create-action  (when-let [handler (:create controller)]
                           (action :create :post handler))
          show-action    (when-let [handler (:show controller)]
                           (action :show :get handler))
          edit-action    (when-let [handler (:edit controller)]
                           (action :edit :get "edit" handler))
          update-action  (when-let [handler (:update controller)]
                           (action :update :patch handler))
          put-action     (when-let [handler (:put controller)]
                           (action :put :put handler))
          destroy-action (when-let [handler (:destroy controller)]
                           (action :destroy :delete handler))
          middleware (get controller :middleware identity)]
      (apply resource-scope singular-name segment middleware
             new-action
             create-action
             show-action
             edit-action
             update-action
             put-action
             destroy-action
             nested))))

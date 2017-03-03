(ns darkleaf.router.use-cases.resource-composition-test
  (:require [clojure.test :refer [deftest testing is]]
            [darkleaf.router :as r]))

;; There is a project resource and it needs to be completed.
;; Supposed project controller should have the complete action.
;; Some time later a new requirement is obtained:
;; there must be the form for data specifying while project completes.
;; In this case it is necessary to add actions for completed form.
;; Controller grows and becomes complicated fast with this approach.

;; In cases like that it is recommended to use nested resources
;; instead of adding extra actions to controller.

;; In this library there is only the one way to implement this requirement:
;; project resource must contains nested completion resource.

(deftest usage
  (let [projects-controller {:index (fn [req] "projects list")
                             :show (fn [req] "project page")}
        project-completion-controller {:new (fn [req] "completion form")
                                       :create (fn [req] "successfully completed")}
        routes (r/resources :projects :project projects-controller
                 (r/resource :completion project-completion-controller))]
    (testing "handler"
      (let [handler (r/make-handler routes)]
        (is (= "completion form"
               (handler {:request-method :get
                         :uri "/projects/1/completion/new"})))))
    (testing "request-for"
      (let [request-for (r/make-request-for routes)]
        (is (= {:request-method :post
                :uri "/projects/1/completion"}
               (request-for :create [:project :completion] {:project 1})))))))

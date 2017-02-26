(ns darkleaf.router.use-cases.resource-composition-test
  (:require [clojure.test :refer [deftest testing is]]
            [darkleaf.router :as r]))

;; Есть ресурс Проект и его требуется завершать.
;; Можно предположить, что проект должнен иметь экшен "завершить".
;; Спустя время, поступает новое требование:
;; должна быть форма для указания данных при завершении проекта.
;; В этом случае придется добавлять экшен "показать форму завершения проекта".
;; При таком подходе контроллер быстро разрастается и усложняется,
;; фактически начинает контроллировать несколько ресурсов.
;;
;; Hельзя добавлять дополнительные экшены к контроллеру,
;; вместо этого предлагается использовать вложенные ресурсы.
;;
;; В данном примере это можно реализовать только единственным способом:
;; ресурс Проект содержит вложенный ресурс Завершение,
;; для завершения проекта вызывается экшен create ресурса Завершение.

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

(ns darkleaf.router.use-cases.member-middleware-test
  (:require [clojure.test :refer [deftest testing is are]]
            [darkleaf.router :as r]))

(deftest usage
  (let [db {:users {"1" {:id "1", :name "Alice"
                         :projects {"1" {:id "1", :name "e-shop"}
                                    "2" {:id "2", :name "web-site"}}}}}
        find-user (fn [id] (get-in db [:users id]))
        find-project (fn [user-id id] (get-in db [:users user-id :projects id]))
        users-controller {:member-middleware
                          (fn [h]
                            (fn [req]
                              (-> req
                                  (assoc-in [:models :user]
                                            (find-user (-> req ::r/params :user)))
                                  (h))))
                          :show (fn [req] (-> req :models :user :name))}
        projects-controller {:member-middleware
                             (fn [h]
                               (fn [req]
                                 (-> req
                                     (assoc-in [:models :project]
                                               (find-project (-> req :models :user :id)
                                                             (-> req ::r/params :project)))
                                     (h))))
                             :index (fn [req]
                                      (str "user name: "
                                           (-> req :models :user :name)
                                           "; "
                                           "projects list"))
                             :show (fn [req]
                                     (str "user name: "
                                          (-> req :models :user :name)
                                          "; project: "
                                          (-> req :models :project :name)))}
        routes (r/resources :users :user users-controller
                 (r/resources :projects :project projects-controller))]
    (testing "response"
      (let [handler (r/make-handler routes)]
        (are [req resp] (= resp (handler req))
          {:uri "/users/1", :request-method :get}
          "Alice"

          {:uri "/users/1/projects", :request-method :get}
          "user name: Alice; projects list"

          {:uri "/users/1/projects/2", :request-method :get}
          "user name: Alice; project: web-site")))))

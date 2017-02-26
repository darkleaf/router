(ns darkleaf.router.mount-test
  (:require [clojure.test :refer [deftest is]]
            [darkleaf.router :as r]
            [darkleaf.router.test-helpers :refer [route-testing make-middleware]]))

(deftest with-segment
  (let [dashboard-controller {:show
                              (fn [req]
                                (let [request-for (::r/request-for req)]
                                  (str "dashboard url: "
                                       (:uri (request-for :show [:dashboard/main] {})))))}
        dashboard (r/resource :dashboard/main dashboard-controller :segment false)
        routes (r/group
                 (r/section :admin
                   (r/mount dashboard :segment "dashboard"))
                 (r/guard :locale #{"en" "ru"}
                   (r/mount dashboard :segment "dashboard")))]
    (route-testing routes
                   :description [:show [:admin :dashboard/main] {}]
                   :request {:uri "/admin/dashboard", :request-method :get}
                   :response "dashboard url: /admin/dashboard")
    (route-testing routes
                   :description [:show [:locale :dashboard/main] {:locale "en"}]
                   :request {:uri "/en/dashboard", :request-method :get}
                   :response "dashboard url: /en/dashboard")))

(deftest without-segment
  (let [dashboard-controller {:show (fn [req] "dashboard")}
        dashboard (r/resource :dashboard/main dashboard-controller :segment false)]
    (for [routes [(r/mount dashboard)
                  (r/mount dashboard :segment false)]]
      (route-testing routes
                     :description [:show [:dashboard/main] {}]
                     :request {:uri "", :request-method :get}
                     :response "dashboard"))))

(deftest middleware
  (let [forum-topics-controller {:show
                                 (fn [req]
                                   (str "topic "
                                        (-> req ::r/params :forum/topic)
                                        " inside "
                                        (-> req :forum/scope)))}
        forum (r/resources :forum/topics :forum/topic forum-topics-controller)

        sites-controller {}
        site-forum-adapter (fn [handler]
                             (fn [req]
                               (-> req
                                   (assoc :forum/scope (str "site "
                                                            (-> req ::r/params :site)))
                                   (handler))))
        community-forum-adapter (fn [handler]
                                  (fn [req]
                                    (-> req
                                        (assoc :forum/scope "community")
                                        (handler))))
        routes (r/group
                 (r/mount forum :segment "community", :middleware community-forum-adapter)
                 (r/resources :sites :site sites-controller
                   (r/mount forum :segment "forum", :middleware site-forum-adapter)))]
    (route-testing routes
                   :description [:show [:forum/topic] {:forum/topic "1"}]
                   :request {:uri "/community/topics/1", :request-method :get}
                   :response "topic 1 inside community")
    (route-testing routes
                   :description [:show [:site :forum/topic] {:site "1", :forum/topic "2"}]
                   :request {:uri "/sites/1/forum/topics/2", :request-method :get}
                   :response "topic 2 inside site 1")))

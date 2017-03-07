(ns darkleaf.router.controller-test
  (:require [clojure.test :refer [deftest are]]
            [darkleaf.router :as r]))

(r/defcontroller pages-controller
  (middleware [handler]
    #(str "pages // " (handler %)))
  (collection-middleware [handler]
    #(str "collection // " (handler %)))
  (member-middleware [handler]
    #(str "member // " (handler %)))
  (index [req]
    "index resp")
  (new [req]
    "new resp")
  (create [req]
    "create resp")
  (show [req]
    (str "show resp"))
  (edit [req]
    (str "edit resp"))
  (update [req]
    (str "update resp"))
  (put [req]
    (str "put resp"))
  (destroy [req]
    (str "destroy resp")))

(deftest default
  (let [routes (r/resources :pages :page pages-controller)
        handler (r/make-handler routes)]
    (are [req resp] (= resp (handler req))
      {:uri "/pages", :request-method :get}
      "pages // collection // index resp"

      {:uri "/pages/1", :request-method :get}
      "pages // member // show resp"

      {:uri "/pages/new", :request-method :get}
      "pages // collection // new resp"

      {:uri "/pages", :request-method :post}
      "pages // collection // create resp"

      {:uri "/pages/1/edit", :request-method :get}
      "pages // member // edit resp"

      {:uri "/pages/1", :request-method :patch}
      "pages // member // update resp"

      {:uri "/pages/1", :request-method :put}
      "pages // member // put resp"

      {:uri "/pages/1", :request-method :delete}
      "pages // member // destroy resp")))

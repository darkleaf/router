(ns darkleaf.router-test
  (:require [clojure.test :refer :all]
            [darkleaf.router :refer :all]))

(def routes
  (flatten
   (list
    (resources :pages (:index identity)))))


(deftest test-handler
  (let [handler (build-handler matcher)
        request {:uri "/pages/about", :request-method :get}
        response (handler request)]
    (is (= :page (get-in response [:matched-route :name])))
    (is (= ["pages" "about"] (:segments response)))
    (is (contains? response :matched-route))
    (is (= {:slug "about"} (:route-params response)))))

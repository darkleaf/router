(ns darkleaf.router.test-helpers
  (:require [clojure.test :refer [testing is]]
            [clojure.set :as set]
            [darkleaf.router :as r]
            #?(:clj [uritemplate-clj.core :as templ])
            #?(:clj [clojure.walk :as walk])))

(defn make-middleware [name]
  (fn [handler]
    (fn [req]
      (str name " // " (handler req)))))

#?(:clj
   (defn- transform-kv [m t-key t-val]
     (let [f (fn [[k v]] [(t-key k) (t-val v)])]
       (walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m))))

(defn route-testing [routes & {[action scope params] :description
                               :keys [request response]}]
  (testing (str "action " action " "
                "in scope " scope " "
                "with params " params)
    (testing "direct matching"
      (let [handler (r/make-handler routes)]
        (is (= response (handler request)))))
    (testing "reverse matching"
      (let [request-for (r/make-request-for routes)]
        (is (= request (request-for action scope params)))))
    #?(:clj
       (testing "explanation"
         (let [explanations (r/explain routes)
               explanation (first (filter (fn [i]
                                            (and (= action (:action i))
                                                 (= scope (:scope i))))
                                          explanations))
               _ (is (some? explanation))
               params (set/rename-keys params (:params-kmap explanation))
               request-template (:req explanation)
               calculated-request (transform-kv request-template
                                                identity
                                                (fn [val]
                                                  (if (string? val)
                                                    (templ/uritemplate val params)
                                                    val)))]
           (is (= request calculated-request)))))))

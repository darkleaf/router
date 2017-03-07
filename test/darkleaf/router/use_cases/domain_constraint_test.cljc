(ns darkleaf.router.use-cases.domain-constraint-test
  (:require [clojure.test :refer [deftest testing is]]
            [darkleaf.router :as r]
            [darkleaf.router.item :as i]
            [darkleaf.router.item-wrappers :as wrappers]))

(deftype DomainConstraint [item name]
  i/Item
  (process [_ req]
    (when (= name (:server-name req))
      (i/process item req)))
  (fill [_ req]
    (i/fill item (assoc req :server-name name)))
  (explain [_ init]
    (i/explain item (assoc-in init [:req :server-name] name))))

(defn ^{:style/indent :defn} domain [id name & children]
  (-> (wrappers/composite children)
      (wrappers/wrap-scope id)
      (DomainConstraint. name)))

(deftest usage
  (let [main-pages-controller (r/controller
                                (index [req] "main pages"))
        shop-pages-controller (r/controller
                                (index [req] "shop pages"))
        routes (r/group
                 (domain :main "cool-site.com"
                   (r/resources :pages :page main-pages-controller))
                 (domain :shop "shop.cool-site.com"
                   (r/resources :pages :page shop-pages-controller)))]
    (testing "handler"
      (let [handler (r/make-handler routes)]
        (is (= "main pages"
               (handler {:request-method :get
                         :uri "/pages"
                         :server-name "cool-site.com"})))
        (is (= "shop pages"
               (handler {:request-method :get
                         :uri "/pages"
                         :server-name "shop.cool-site.com"})))))
    (testing "request-for"
      (let [request-for (r/make-request-for routes)]
        (is (= {:request-method :get
                :uri "/pages"
                :server-name "cool-site.com"}
               (request-for :index [:main :pages] {})))
        (is (= {:request-method :get
                :uri "/pages"
                :server-name "shop.cool-site.com"}
               (request-for :index [:shop :pages] {})))))
    (testing "explain"
      (is (= [{:action :index,
               :scope [:main :pages],
               :params-kmap {},
               :req {:uri "/pages",
                     :server-name "cool-site.com",
                     :request-method :get}}
              {:action :index,
               :scope [:shop :pages],
               :params-kmap {},
               :req {:uri "/pages",
                     :server-name "shop.cool-site.com",
                     :request-method :get}}]
             (r/explain routes))))))

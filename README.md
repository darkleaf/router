# Router

[![Build Status](https://travis-ci.org/darkleaf/router.svg?branch=master)](https://travis-ci.org/darkleaf/router)
[![Clojars Project](https://img.shields.io/clojars/v/darkleaf/router.svg)](https://clojars.org/darkleaf/router)

Bidirectional RESTfull Ring router for clojure and clojurescript.
Routing description is data structure that builds by functions.
No macros, no foreign libs.
Routing can be described in cljc files for code sharing.

## Usage

Please see [tests](test/darkleaf/router_test.clj) for exhaustive examples.

## Concept

Библиотека подразумевает определенный подход к проектированию роутинга.

Например, есть ресурс Проект и его требуется завершать.
Можно предположить, что проект должнен иметь экшен "завершить".
Спустя время, поступает новое требование: должна быть форма для указания данных при завершении проекта.
В этом случае придется добавлять экшен "показать форму завершения проекта".
При таком подходе контроллер быстро разрастается и усложняется, фактически начинает контроллировать несколько ресурсов.

В этой библиотеке нельзя добавлять дополнительные экшены к контроллеру,
вместо этого предлагается использовать вложенные ресурсы.

В данном примере это можно реализовать только единственным способом:
ресурс Проект содержит вложенный ресурс Завершение,
для завершения проекта вызывается экшен create ресурса Завершение.

``` clojure
(def projects-controller
  {:index (fn [req] (response "projects list"))
   :show (fn [req] (response "project page"))})
(def project-completion-controller
  {:new (fn [req] (response "completion form"))
   :create (fn [req] (response "successfully created"))})

(r/resources :projects :project projects-controller
             (r/resource :completition project-completion-controller)
```

## Resources

| Action name | Scope | Params | Http method | Url | Type | Used for |
| --- | --- | --- | --- | --- | --- | --- |
| index   | [:pages] | {}           | Get    | /pages        | collection | display a list of pages  |
| show    | [:page]  | {:page-id 1} | Get    | /pages/1      | member     | display a specific page |
| new     | [:page]  | {}           | Get    | /pages/new    | collection | display a form for creating new page |
| create  | [:page]  | {}           | Post   | /pages        | collection | create a new page |
| edit    | [:page]  | {:page-id 1} | Get    | /pages/1/edit | member     | display a form for updating page |
| update  | [:page]  | {:page-id 1} | Patch  | /pages/1      | member     | update a specific page |
| put     | [:page]  | {:page-id 1} | Put    | /pages/1      | member     | upsert a specific page, may be combined with edit action |
| destroy | [:page]  | {:page-id 1} | Delete | /pages/1      | member     | delete a specific page |

``` clojure
;; all keys are optional
(ns app.some-ns
  (:require [darkleaf.router :as r]
            [ring.util.response :refer [response]]))

(def pages-controller
  {:middleware            (fn [h] (fn [req] (h req))) ;; will be applied to nested routes too
   :collection-middleware (fn [h] (fn [req] (h req)))
   :member-middleware     (fn [h] (fn [req] (h req))) ;; will be applied to nested routes too
   :index   (fn [req] (response "index resp"))
   :show    (fn [req] (response "show resp"))
   :new     (fn [req] (response "new resp"))
   :create  (fn [req] (response "create resp"))
   :edit    (fn [req] (response "edit resp"))
   :update  (fn [req] (response "update resp"))
   :put     (fn [req] (response "put resp"))
   :destroy (fn [req] (response "destroy resp"))})

;; :index [:pages] {} -> /pages
;; :show [:page] {:page-id 1} -> /pages/1
(r/resources :pages :page pages-controller)

;; :index [:people] {} -> /menschen
;; :show [:person] {:person-id 1} -> /menschen/1
(r/resources :people :person people-controller
             :segment "menschen")

;; :index [:people] {} -> /
;; :show [:person] {:person-id 1} -> /1
(r/resources :people :person people-controller
             :segment false)

;; :put [:page :star] {:page-id 1} -> PUT /pages/1/star
(r/resources :pages :page pages-controller
             (r/resource :star star-controller)
```

## Resource

| Action name | Scope | Params | Http method | Url | Used for
| --- | --- | --- | --- | --- | --- |
| show    | [:star] | {} | Get    | /star/:star-id      | display a specific star |
| new     | [:star] | {} | Get    | /star/new           | display a form for creating new star |
| create  | [:star] | {} | Post   | /star               | create a new star |
| edit    | [:star] | {} | Get    | /star/:star-id/edit | display a form for updating star |
| update  | [:star] | {} | Patch  | /star/:star-id      | update a specific star |
| put     | [:star] | {} | Put    | /star/:star-id      | upsert a specific star, may be combined with edit action |
| destroy | [:star] | {} | Delete | /star/:star-id      | delete a specific star |

``` clojure
;; all keys are optional
(def star-controller
  {:middleware (fn [h] (fn [req] (h req))) ;; will be applied to nested routes too
   :show    (fn [req] (response "show resp"))
   :new     (fn [req] (response "new resp"))
   :create  (fn [req] (response "create resp"))
   :edit    (fn [req] (response "edit resp"))
   :update  (fn [req] (response "update resp"))
   :put     (fn [req] (response "put resp"))
   :destroy (fn [req] (response "destroy resp"))})
```

``` clojure
;; :show [:star] {} -> /star
(r/resource :star star-controller)

;; :show [:star] {} -> /estrella
(r/resource :star star-controller
            :segment "estrella")

;; :show [:star] {} -> /
(r/resource :star star-controller
            :segment false)

;; :index [:star :comments] {} -> /star/comments
(r/resource :star star-controller
            (r/resources :comments :comment comments-controller)
```

## Composite

Объединяет несколько роутов в один.

``` clojure
(def posts-controller {:show (fn [req] (response "show post resp"))})
(def news-controller {:show (fn [req] (response "show news resp"))})
(def routes
  (r/composite
   (r/resources :posts :post posts-controller)
   (r/resources :news :news news-controller)))
```

## Section

``` clojure
;; :index [:admin :pages] {} -> /admin/pages
(r/section :admin
           (r/resources :pages :page pages-controller))

;; :index [:admin :pages] {} -> /private/pages
(r/section :admin, :segment "private"
           (r/resources :pages :page pages-controller))

(r/section :admin, :middleware (fn [h] (fn [req] (h req)))
           (r/resources :pages :page pages-controller))
```

## Wrapper

``` clojure
(r/wrapper (fn [h] (fn [req] (h req)))
           (r/resources :pages :page pages-controller))
```

## Guard

``` clojure
;; :index [:locale :pages] {:locale "ru"} -> /ru/pages
;; :index [:locale :pages] {:locale "wrong"} -> not found
(r/guard :locale #{"ru" "en"}
         (r/resources :pages :page pages-controller))

(r/guard :locale #{"ru" "en"}
         :middleware (fn [h] (fn [req] (h req)))
         (r/resources :pages :page pages-controller))
```

## Helpers

``` clojure
(def controller {:index (fn [_] "ok")})
(def pages (resources :pages :page controller))

(def handler (make-handler pages))
(def request-for (make-request-for pages))

(handler {:uri "/pages", :request-method :get}) ;; call index action from controller
(request-for :index [:pages] {}) ;; returns {:uri "/pages", :request-method :get}
```

Handler adds keys for request map:
* :darkleaf.router/action
* :darkleaf.router/scope
* :darkleaf.router/params
* :darkleaf.router/request-for for preventing circular dependency

## Questions

You can create github issue with your question.

## TODO

* wildcard, not-found
* docs, pre, assert
* clojure.spec

## License

Copyright © 2016 Mikhail Kuzmin

Distributed under the Eclipse Public License version 1.0.

# Router

[![Build Status](https://travis-ci.org/darkleaf/router.svg?branch=master)](https://travis-ci.org/darkleaf/router)
[![Clojars Project](https://img.shields.io/clojars/v/darkleaf/router.svg)](https://clojars.org/darkleaf/router)

Bidirectional RESTfull Ring router for clojure and clojurescript.

## Comparation

| library | clj | cljs | dsl | named routes | mountable apps | abstraction | export format | extensibility |
| --- | --- | --- | ---  | --- | --- | --- | --- | --- |
| [compojure](https://github.com/weavejester/compojure) | ✓ |   | macros         |   |   | url      |                          |           |
| [secretary](https://github.com/gf3/secretary)         |   | ✓ | macros         | ✓ |   | url      |                          | protocols |
| [bidi](https://github.com/juxt/bidi)                  | ✓ | ✓ | data/functions | ✓ |   | url      | route description data   | protocols |
| [darkleaf/router](https://github.com/darkleaf/router) | ✓ | ✓ | functions      | ✓ | ✓ | resource | [explain data](#explain) | protocols |

## Usage

``` clojure
(ns app.some-ns
  (:require [darkleaf.router :as r]
            [ring.util.response :refer [response]]))

(r/defcontroller controller
  (index [req]
    (let [request-for (::r/request-for req)]
      (response (str (request-for :index [:pages] {}))))))

(def routing (r/resources :pages :page controller))

(def handler (r/make-handler routing))
(def request-for (r/make-request-for routing))

(handler {:uri "/pages", :request-method :get}) ;; call index action from controller
(request-for :index [:pages] {}) ;; returns {:uri "/pages", :request-method :get}
```

Single routing namespace:
``` clojure
(ns app.routing
  (:require
   [darkleaf.router :as r]
   [app.controllers.main :as main]
   [app.controllers.session :as session]
   [app.controllers.account.invites :as account.invites]
   [app.controllers.users :as users]
   [app.controllers.users.statistics :as users.statistics]
   [app.controllers.users.pm-bonus :as users.pm-bonus]
   [app.controllers.projects :as projects]
   [app.controllers.projects.status :as projects.status]
   [app.controllers.projects.completion :as projects.completion]
   [app.controllers.tasks :as tasks]
   [app.controllers.tasks.status :as tasks.status]
   [app.controllers.tasks.comments :as tasks.comments]))
   
(def routes
  (r/group
    (r/resource :main main/controller :segment false)
    (r/resource :session session/controller)
    (r/section :account
      (r/resources :invites :invite account.invites/controller)) 
    (r/resources :users :user users/controller
      (r/resource :statistics users.statistics/controller)
      (r/resource :pm-bonus users.pm-bonus/controller))
    (r/resources :projects :project projects/controller
      (r/resource :status projects.status/controller)
      (r/resource :completion projects.completion/controller))
    (r/resources :tasks :task tasks/controller
      (r/resource :status tasks.status/controller)
      (r/resources :comments tasks.comments/controller))))
```

Multiple routing namespaces:
``` clojure
(ns app.routes.main
  (:require
   [darkleaf.router :as r]))

(r/defcontroller controller
  (show [req] ...))

(def routes (r/resource :main main-controller :segment false))

(ns app.routes
  (:require
   [darkleaf.router :as r]
   [app.routes.main :as main]
   [app.routes.session :as session]
   [app.routes.account :as account]
   [app.routes.users :as users]
   [app.routes.projects :as projects]
   [app.routes.tasks :as tasks]))

(def routes
  (r/group
    main/routes
    session/routes
    account/routes
    users/routes
    projects/routes
    tasks/routes))
```

## Use cases

* [resource composition / additional controller actions](test/darkleaf/router/use_cases/resource_composition_test.cljc)
* [member middleware](test/darkleaf/router/use_cases/member_middleware_test.cljc)
* [extending / domain constraint](test/darkleaf/router/use_cases/domain_constraint_test.cljc)

## Rationale

Routing libraries work similarly on all programming languages: they only map uri with a handler using templates.
For example compojure, sinatra, express.js, cowboy.

There are some downsides of this approach.

1. No reverse or named routing. Url is set in templates as a string.
2. Absence of structure. Libraries do not offer any ways of code structure, that results of chaos in url and unclean code.
3. Inability to mount an external application. Inability to create html links related with mount point.
4. Inability to serialize routing and use it in other external applications for request forming.

Most of these problems are solved in [Ruby on Rails](http://guides.rubyonrails.org/routing.html).

1. If you know the action, controller name and parameters, you can get url, for example: edit_admin_post_path(@post.id).
2. You can use rest resources to describe routing.
   Actions of controllers match to handlers.
   However, framework allows to add non-standart actions into controller,
   that makes your code unlean later.
3. There is an engine support. For example, you can mount a forum engine into your project or
   decompose your application into several engines.
4. There is an API for routes traversing, which uses `rake routes` command. The library
   [js-routes](https://github.com/railsware/js-routes) brings url helpers in js.

Solution my library suggests.

1. Knowing action, scope and params, we can get the request, which invokes the handler of this route:
   `(request-for :edit [:admin :post] {:post "1"})`.
2. The main abstraction is the rest resource. Controller contains only standard actions.
   You can see [resource composition](test/darkleaf/router/use_cases/resource_composition_test.cljc) how to deal with it.
3. Ability to mount an external application. See [example](#mount) for details.
4. The library interface is identical in clojure and clojurecript, that allows to share the code between server and
   client using .cljc files. You can also export routing description with cross-platform templates as a simple data
   structure. See [example](#explain) for details.

## Resources

| Action name | Scope | Params | Http method | Url | Type | Used for |
| --- | --- | --- | --- | --- | --- | --- |
| index   | [:pages] | {}        | Get    | /pages        | collection | display a list of pages  |
| show    | [:page]  | {:page&nbsp;1} | Get    | /pages/1      | member     | display a specific page |
| new     | [:page]  | {}        | Get    | /pages/new    | collection | display a form for creating new page |
| create  | [:page]  | {}        | Post   | /pages        | collection | create a new page |
| edit    | [:page]  | {:page 1} | Get    | /pages/1/edit | member     | display a form for updating page |
| update  | [:page]  | {:page 1} | Patch  | /pages/1      | member     | update a specific page |
| put     | [:page]  | {:page 1} | Put    | /pages/1      | member     | upsert a specific page, may be combined with edit action |
| destroy | [:page]  | {:page 1} | Delete | /pages/1      | member     | delete a specific page |

``` clojure
(ns app.some-ns
  (:require [darkleaf.router :as r]
            [ring.util.response :refer [response]]))

;; all items are optional
(r/defcontroller pages-controller
  (middleware [h]
    (fn [req] (h req)))
  (collection-middleware [h]
    (fn [req] (h req)))
  (member-middleware [h]
    (fn [req] (h req)))
  (index [req]
    (response "index resp"))
  (show [req]
    (response "show resp"))
  (new [req]
    (response "new resp"))
  (create [req]
    (response "create resp"))
  (edit [req]
    (response "edit resp"))
  (update [req]
    (response "update resp"))
  (put [req]
    (response "put resp"))
  (destroy [req]
    (response "destroy resp")))

;; :index [:pages] {} -> /pages
;; :show [:page] {:page 1} -> /pages/1
(r/resources :pages :page pages-controller)

;; :index [:people] {} -> /menschen
;; :show [:person] {:person 1} -> /menschen/1
(r/resources :people :person people-controller :segment "menschen")

;; :index [:people] {} -> /
;; :show [:person] {:person 1} -> /1
(r/resources :people :person people-controller :segment false)

;; :put [:page :star] {:page 1} -> PUT /pages/1/star
(r/resources :pages :page pages-controller
  (r/resource :star star-controller)
```

There are 3 types of middlewares:

* `middleware` applied to all action handlers including nested.
* `collection-middleware` applied only to index, new and create actions.
* `member-middleware` applied to show, edit, update, put, delete and all nested handlers, look
  [here](test/darkleaf/router/use_cases/member_middleware_test.cljc) for details.

Please see [test](test/darkleaf/router/resources_test.cljc) for all examples.

## Resource

| Action name | Scope | Params | Http method | Url | Used for
| --- | --- | --- | --- | --- | --- |
| show    | [:star] | {} | Get    | /star      | display a specific star |
| new     | [:star] | {} | Get    | /star/new  | display a form for creating new star |
| create  | [:star] | {} | Post   | /star      | create a new star |
| edit    | [:star] | {} | Get    | /star/edit | display a form for updating star |
| update  | [:star] | {} | Patch  | /star      | update a specific star |
| put     | [:star] | {} | Put    | /star      | upsert a specific star, may be combined with edit action |
| destroy | [:star] | {} | Delete | /star      | delete a specific star |

``` clojure
;; all items are optional
(r/defcontroller star-controller
  ;; will be applied to nested routes too
  (middleware [h]
    (fn [req] (h req)))
  (show [req]
    (response "show resp"))
  (new [req]
    (response "new resp"))
  (create [req]
    (response "create resp"))
  (edit [req]
    (response "edit resp"))
  (update [req]
    (response "update resp"))
  (put [req]
    (response "put resp"))
  (destroy [req]
    (response "destroy resp")))

;; :show [:star] {} -> /star
(r/resource :star star-controller)

;; :show [:star] {} -> /estrella
(r/resource :star star-controller :segment "estrella")

;; :show [:star] {} -> /
(r/resource :star star-controller :segment false)

;; :index [:star :comments] {} -> /star/comments
(r/resource :star star-controller
  (r/resources :comments :comment comments-controller)
```

Please see [test](test/darkleaf/router/resource_test.cljc) for exhaustive examples.

## Group

This function combines multiple routes into one and applies optional middleware.

``` clojure
(r/defcontroller posts-controller
  (show [req] (response "show post resp")))
(r/defcontroller news-controller
  (show [req] (response "show news resp")))

;; :show [:post] {:post 1} -> /posts/1
;; :show [:news] {:news 1} -> /news/1
(r/group
  (r/resources :posts :post posts-controller)
  (r/resources :news :news news-controller)))

(r/group :middleware (fn [h] (fn [req] (h req)))
  (r/resources :posts :post posts-controller)
  (r/resources :news :news news-controller))
```

Please see [test](test/darkleaf/router/group_test.cljc) for exhaustive examples.

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

Please see [test](test/darkleaf/router/section_test.cljc) for exhaustive examples.

## Guard

``` clojure
;; :index [:locale :pages] {:locale "ru"} -> /ru/pages
;; :index [:locale :pages] {:locale "wrong"} -> not found
(r/guard :locale #{"ru" "en"}
  (r/resources :pages :page pages-controller))

(r/guard :locale #(= "en" %)
  (r/resources :pages :page pages-controller))

(r/guard :locale #{"ru" "en"} :middleware (fn [h] (fn [req] (h req)))
  (r/resources :pages :page pages-controller))
```

Please see [test](test/darkleaf/router/guard_test.cljc) for exhaustive examples.

## Mount

This function allows to mount isolated applications. `request-for` inside `request` map works regarding the mount point.

```clojure
(def dashboard-app (r/resource :dashboard/main dashboard-controller :segment false))

;; show [:admin :dashboard/main] {} -> /admin/dashboard
(r/section :admin
  (r/mount dashboard-app :segment "dashboard"))

;; show [:admin :dashboard/main] {} -> /admin
(r/section :admin
  (r/mount dashboard-app :segment false))

;; show [:admin :dashboard/main] {} -> /admin
(r/section :admin
  (r/mount dashboard-app))

(r/section :admin
  (r/mount dashboard-app :segment "dashboard", :middleware (fn [h] (fn [req] (h req)))))
```

Please see [test](test/darkleaf/router/mount_test.cljc) for exhaustive examples.

## Pass

Passes any request in the current scope to a specified handler.
Inner segments are available as `(-> req ::r/params :segments)`.
Action name is provided by request-method.
It can be used for creating custom 404 page for current scope.

```clojure
(defn handler (fn [req] (response "dashboard")))

;; :get [:admin :dashboard] {} -> /admin/dashboard
;; :post [:admin :dashboard] {:segments ["private" "users"]} -> POST /admin/dashboard/private/users
(r/section :admin
  (r/pass :dashboard handler))

;; :get [:admin :dashboard] {} -> /admin/monitoring
;; :post [:admin :dashboard] {:segments ["private" "users"]} -> POST /admin/monitoring/private/users
(r/section :admin
  (r/pass :dashboard handler :segment "monitoring"))

;; :get [:not-found] {} -> /
;; :post [:not-found] {:segments ["foo" "bar"]} -> POST /foo/bar
(r/pass :not-found handler :segment false)
```

Please see [test](test/darkleaf/router/pass_test.cljc) for exhaustive examples.

## Additional request keys

Handler adds keys for request map:
* :darkleaf.router/action
* :darkleaf.router/scope
* :darkleaf.router/params
* :darkleaf.router/request-for

Please see [test](test/darkleaf/router/additional_request_keys_test.cljc) for exhaustive examples.

## Async

[Asynchronous ring](https://www.booleanknot.com/blog/2016/07/15/asynchronous-ring.html) handlers support.
It also can be used in [macchiato-framework](https://github.com/macchiato-framework/examples/tree/master/auth-example-router).

``` clojure
(r/defcontroller pages-controller
  (index [req resp raise]
    (future (resp response))))

(def pages (r/resources :pages :page pages-controller))
(def handler (r/make-handler pages))

(defn respond [val]) ;; from web server
(defn error [e]) ;; from web server

(handler {:request-method :get, :uri "/pages"} respond error)
```

Please see [clj test](test/darkleaf/router/async_test.clj)
and [cljs test](test/darkleaf/router/async_test.cljs)
for exhaustive examples.

## Explain

```clojure
(r/defcontroller people-controller
  (index [req] (response "index"))
  (show [req] (response "show")))

(def routes (r/resources :people :person people-controller))
(pprint (r/explain routes))
```

```clojure
[{:action :index,
  :scope [:people],
  :params-kmap {},
  :req {:uri "/people", :request-method :get}}
 {:action :show,
  :scope [:person],
  :params-kmap {:person "%3Aperson"},
  :req {:uri "/people{/%3Aperson}", :request-method :get}}]
```

It useful for:

+ inspection routing structure
+ mistakes detection
+ cross-platform routes serialization
+ documentation generation

[URI Template](https://tools.ietf.org/html/rfc6570) uses for templating.
Url encode is applied for ability to use keywords as a template variable
because of the fact that clojure keywords contains forbidden symbols.
Template parameters and :params mapping is set with :params-kmap.

## HTML

HTML doesn’t support HTTP methods except GET и POST.
You need to add the hidden field _method with put, patch or delete value to send PUT, PATCH or DELETE request.
It is also necessary to wrap a handler with `darkleaf.router.html.method-override/wrap-method-override`.
Use it with `ring.middleware.params/wrap-params` and `ring.middleware.keyword-params/wrap-keyword-params`.

Please see [examples](test/darkleaf/router/html/method_override_test.cljc).

In future releases I'm going to add js code for arbitrary request sending using html links.

## Questions

You can create github issue with your question.

## TODO

* docs
* pre, assert

## License

Copyright © 2016 Mikhail Kuzmin

Distributed under the Eclipse Public License version 1.0.

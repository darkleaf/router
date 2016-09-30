# Router

[![Build Status](https://travis-ci.org/darkleaf/router.svg?branch=master)](https://travis-ci.org/darkleaf/router)

Bidirectional Ring router. REST oriented. It uses the core.match internally.

Routing description is data structure that builds by functions.
`handler` and `request-for` are functions builing by macros.

## Project status

Ready for hobby projects.
May be API will be corrected but not fundamentally.
I plan to gather feedback to make adjustments and make release.

## Basic usage

```clojure
(ns hello-world.core
  (:require [darkleaf.router :refer :all]))

;; routes must store into a var for macro magick
(def routes
  (build-routes
   (action :about (fn [req] some-ring-response))
   (section :products
    (action :get :mug (fn [req] some-ring-response)))))

(def handler (build-handler routes))
(def request-for (build-request-for routes))

(handler {:uri "/about", :request-method :get}) ;; call about action
(request-for :contacts []) ;; returns {:uri "/about", :request-method :get}

(handler {:uri "/products/mug", :request-method :get}) ;; call mug action in product's scope
(request-for :mug [:products]) ;; returns {:uri "/products/mug", :request-method :get}
```

This library also contains some useful functions including `root`, `wildcard`, `not-found`, `guard`, `resource`, `resources`.
Please see [tests](test/darkleaf/router_test.clj) for more examples.

## Resourceful routing

```clojure
(def pages-controller
  {:middleware (fn [handler] (fn [req] req))
   :member-middleware some-middleware
   :index (fn [req] some-ring-response)
   :show (fn [req] some-ring-response)})

(def routes
  (build-routes
   (resources :pages 'page-id pages-controller)))

(def handler (build-handler routes))
(def request-for (build-request-for routes))

(handler {:uri "/pages", :request-method :get}) ;; call index action from pages-controller
(request-for :index [:pages]) ;; returns {:uri "/pages", :request-method :get}

(handler {:uri "/pages/1", :request-method :get}) ;; call show action from pages-controller
(request-for :show [:pages] {:page-id "1"}) ;; returns {:uri "/pages/1", :request-method :get}
```

Router adds two keys for request map: `:matched-route` and `route-params`.
For show action `route-params` are `{:page-id "1"}`.

You can also use nested resource(s). You can see examples in [tests](test/darkleaf/router_test.clj).

Controller for resources function can contain certain keys:
* `:middleware` - middleware that wrap all controller actions including nested routes handlers.
* `:member-middleware` - wrap only member actions and member nested routes
* collection actions: `:index`, `:new`, `:create`
* member actions: `:show`, `:edit`, `:update`, `:destroy`

## Low level

If you support legacy routing or need some functions for building custom routing
you can build own fucntions using `darkleaf.router.low-lewel` namespace.
See examples in `darkleaf.router` [namespace](src/darkleaf/router/low_level.clj) and [tests](test/darkleaf/router/low_level_test.clj).

## Questions

You can create github issue with your question.

## TODO

* refactoring
 * vars & locals naming
 * error messages
 * pre/post specs
* documentation/wiki
 * best practices
* helpers ??
 * link-to
  * query params /pages?filter=active
* clojure script
* clojure.spec

## License

Copyright © 2016 Mikhail Kuzmin

Distributed under the Eclipse Public License version 1.0.

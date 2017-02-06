# Router

[![Build Status](https://travis-ci.org/darkleaf/router.svg?branch=master)](https://travis-ci.org/darkleaf/router)
[![Clojars Project](https://img.shields.io/clojars/v/darkleaf/router.svg)](https://clojars.org/darkleaf/router)

Bidirectional RESTfull Ring router.

Routing description is data structure that builds by functions.

Эта библиотека рассчитана на новые проекты.
Навязывает структурирование роутинга только с помощью ресурсов.
Ресурсом может быть страница, сессия, завершение проекта.

Имеется возможность расширить dsl используя протоколы.


## Controllers

Контроллер представляет собой map, где ключ - это название экшена, а значение - обработчик запроса.

``` clojure
(def pages-controller
  {:index   (fn [req] "index resp")
   :show    (fn [req] "show resp")
   :new     (fn [req] "new resp")
   :create  (fn [req] "create resp")
   :edit    (fn [req] "edit resp")
   :update  (fn [req] "update resp")
   :put     (fn [req] "put resp")
   :destroy (fn [req] "destroy resp")})
```

## Resources

Ресусы соззаются функцией `resources`:

``` clojure
(resources :pages :page pages-controller)
```


Эта функция принимает следующие праметры:
названия ресурса в множественном и единственном числах,
контроллер, и опциональные параметры, которые рассмотрем ниже.

Зная action name, scope и params можно получить http...

| Action name | Scope | Params | Http method | Url |
| --- | --- | --- | --- | --- |
| index   | [:pages] | {}           | Get    | /pages        |
| show    | [:page]  | {:page-id 1} | Get    | /pages/1      |
| new     | [:page]  | {}           | Get    | /pages/new    |
| create  | [:page]  | {}           | Post   | /pages        |
| edit    | [:page]  | {:page-id 1} | Get    | /pages/1/edit |
| update  | [:page]  | {:page-id 1} | Patch  | /pages/1      |
| put     | [:page]  | {:page-id 1} | Put    | /pages/1      |
| destroy | [:page]  | {:page-id 1} | Delete | /pages/1      |


## Resource

| Action name | Scope | Http method | Url |
| --- | --- | --- | --- |
| show    | [:star]  | Get    | /star/:star-id      |
| new     | [:star]  | Get    | /star/new           |
| create  | [:star]  | Post   | /star               |
| edit    | [:star]  | Get    | /star/:star-id/edit |
| update  | [:star]  | Patch  | /star/:star-id      |
| put     | [:star]  | Put    | /star/:star-id      |
| destroy | [:star]  | Delete | /star/:star-id      |




##


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

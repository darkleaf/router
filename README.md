# Router

[![Build Status](https://travis-ci.org/darkleaf/router.svg?branch=master)](https://travis-ci.org/darkleaf/router)
[![Clojars Project](https://img.shields.io/clojars/v/darkleaf/router.svg)](https://clojars.org/darkleaf/router)

Bidirectional RESTfull Ring router for clojure and clojurescript.
Routing description is data structure that builds by functions.
No macros, no foreign libs.
Routing can be described in cljc files for code sharing.

## Comparation

| library | clj | cljs | syntax | reverse match | isolated apps | main abstraction | exporting | extensibility |
| --- | --- | --- | ---  | --- | --- | --- | --- | --- |
| [compojure](https://github.com/weavejester/compojure) | ✓ |   | macros         |   |   | url      |                                           |           |
| [secretary](https://github.com/gf3/secretary)         |   | ✓ | macros         | ✓ |   | url      |                                           | protocols |
| [bidi](https://github.com/juxt/bidi)                  | ✓ | ✓ | data/functions | ✓ |   | url      | route description data                    | protocols |
| [darkleaf/router](https://github.com/darkleaf/router) | ✓ | ✓ | functions      | ✓ | ✓ | resource | explain api with cross-platform templates | protocols |

## Usage

``` clojure
(ns app.some-ns
  (:require [darkleaf.router :as r]
            [ring.util.response :refer [response]]))

(def pages-controller
  {:index (fn [req]
            (let [request-for (::r/request-for req)]
              (response
               (str "best page uri: "
                    (:uri (request-for :show [:page] {:page "best-page"}))))))
   :show  (fn [req] (response "show resp"))})

(def routes (r/resources :pages :page pages-controller))
(def handler (r/make-hanlder routes))

(handler {:request-method :get, :uri "/pages"}) ;; #=> {:status 200, :headers {}, :body "best page uri: /pages/best-page"}
(handler {:request-method :get, :uri "/pages/1"}) ;; #=> response from show action
```

Please see [tests](test/darkleaf/router_test.cljc) for exhaustive examples.

## Rationale

Библиотеки роутинга на всех языках имеют схожий функционал: они сопоставляют uri с обработчиком с помощью шаблонов.
Так устроены: compojure, sinatra, express.js, cowboy.

Я вижу следующие недостатки такого подхода:

1. Отсутствие обратного роутинга. Url задается в шаблонах с помощью строк.
2. Отсутствует структура.
   Библиотеки не предлагают из коробки решения для структурирования кода,
   что ведет к хаосу в структуре url и спагетти-коду.
3. Отсутствуют подключаемые изолированные приложения,
   т.к. нет возможности создать внутреннюю ссылку относительно точки монтирования.
4. Невозможно сериализовать роутинг и использовать его в других системах для формирования запросов.

Большинство этих проблем решены в [Ruby on Rails](http://guides.rubyonrails.org/routing.html):

1. Зная экшен, название контроллера и параметры можно получить url,
   с помощью url helpers вида `edit_admin_post_path(@post.id)`.
2. Предполагается построение роутинга с помощью rest ресурсов.
   Обработчики задаются экшенами в контроллерах.
   Однако, фреймворк позволяет добавлять нестандартные экшены в контроллеры, что со временем ведет к спагетти-коду.
3. Существует поддержка engine.
   Например, в свой проект можно примонтировать форум
   или разбить приложение на несколько независимых.
4. Существует апи обхода роутов, например, его использует `rake routes`.
   Cуществует [библиотека](https://github.com/railsware/js-routes), пробрасывающая url helpers в js.

Решение с помощью этой библиотеки:

1. Зная action, scope и params можно получить запрос,
   который вызовет обработчик этого роута: `(request-for :edit [:admin :post] {:post "1"})`.
2. Главной абстракцией является rest ресурс.
   Контроллер ресурса может содержать только определенные экшены, см. [resource composition](#resouce-composition).
3. Существует возможность примонтировать стороннее приложение, см. [пример](#mount).
4. Библиотека имеет одинаковый интерфейс в clojure и clojurescript,
   что позволяет разделять код между сервером и клиентом с помощью сljc.
   Также имеется возможность экспортировать описание роутинга
   в виде простых структур данных с использованием кроссплатформенных шаблонов, см. [пример](#explain).

## Resouce composition

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
  (r/resource :completion project-completion-controller)
```

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

;; all keys are optional
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

## Group

Объединяет несколько роутов в один.
Опционально добавляет middleware.

``` clojure
(def posts-controller {:show (fn [req] (response "show post resp"))})
(def news-controller {:show (fn [req] (response "show news resp"))})

;; :show [:post] {:post 1} -> /posts/1
;; :show [:news] {:news 1} -> /news/1
(r/group
  (r/resources :posts :post posts-controller)
  (r/resources :news :news news-controller)))

(r/group :middleware (fn [h] (fn [req] (h req)))
  (r/resources :posts :post posts-controller)
  (r/resources :news :news news-controller))
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

## Guard

``` clojure
;; :index [:locale :pages] {:locale "ru"} -> /ru/pages
;; :index [:locale :pages] {:locale "wrong"} -> not found
(r/guard :locale #{"ru" "en"}
  (r/resources :pages :page pages-controller))

(r/guard :locale #{"ru" "en"} :middleware (fn [h] (fn [req] (h req)))
  (r/resources :pages :page pages-controller))
```

## Mount

Позволяет примонтировать изолированное приложение.
Внутренний request-for работает относительно точки монтирования.
Смотри подробные примеры в тестах.

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

## Pass

Передает любой запрос в текущей области в обработчик.
Внутренние сегменты доступны как `(-> req ::r/params :segments)`.
Экшен задается request-method.
Может использоваться для задания специальной страницы 404 для текущей области.

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

## Helpers

``` clojure
(def controller {:index (fn [req]
                          (let [request-for (::r/request-for req)]
                            (response (str (request-for :index [:pages] {})))))})
(def pages (r/resources :pages :page controller))

(def handler (r/make-handler pages))
(def request-for (r/make-request-for pages))

(handler {:uri "/pages", :request-method :get}) ;; call index action from controller
(request-for :index [:pages] {}) ;; returns {:uri "/pages", :request-method :get}
```

Handler adds keys for request map:
* :darkleaf.router/action
* :darkleaf.router/scope
* :darkleaf.router/params
* :darkleaf.router/request-for for preventing circular dependency

## Async

Имеется поддержка [ассинхронных обработчиков ring](https://www.booleanknot.com/blog/2016/07/15/asynchronous-ring.html).

``` clojure
(def pages-controller {:index (fn [req resp raise]
                                (future (resp response)))})

(def pages (r/resources :pages :page pages-controller))
(def handler (r/make-handler pages))

(defn respond [val]) ;; from web server
(defn error [e]) ;; from web server

(handler {:request-method :get, :uri "/pages"} respond error)
```

## Explain

```clojure
(def people-controller {:index (fn [req] (response "index"))
                        :show (fn [req] (response "show"))})
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

Удобно использовать для:
 + наглядного отображения структуры роутинга
 + поиска ошибок
 + кроссплатформенной сериализации роутинга
 + построения документации

Для шаблонизации используется [URI Template](https://tools.ietf.org/html/rfc6570).
Т.к. clojure keywords содержат запрещенные символы,
поэтому, что бы использовать keyword в качестве переменной шаблона, применятеся url encode.
Соответствие параметров шаблона и :params задается через :params-kmap.

## Questions

You can create github issue with your question.

## TODO

* refactoring
* domains support?
* docs
* pre, assert

## License

Copyright © 2016 Mikhail Kuzmin

Distributed under the Eclipse Public License version 1.0.

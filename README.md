# Router

[![Build Status](https://travis-ci.org/darkleaf/router.svg?branch=master)](https://travis-ci.org/darkleaf/router)

*UNSTABLE*

Ring роутинг поверх core.match. Работает в обе стороны. Вдохновлен Ruby on rails.

Роуты хранятся в виде структуры данных. handler и request-for гененрируются на основе роутов с помощью макросов.

## Usage

See tests.

## Questions

You can create github issue with your question.

## TODO

* :segments поместить в неймспейс и не пускать его в handler
* request-for проверять, что полученный request будет обработан этим же роутом
* необратимые роуты, вроде not-found
* helpers ??
 * link-to
  * query params /pages?filter=active
* Документация кода и readme на английском
* ClojureScript
* clojure.spec

## License

Copyright © 2016 Mikhail Kuzmin

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

# Router

*UNSTABLE*

Ring роутинг поверх core.match. Работает в обе стороны. Вдохновлен Ruby on rails.

Роуты хранятся в виде структуры данных. handler и request-for гененрируются на основе роутов с помощью макросов.

## Usage

See tests.

## Questions

You can create github issue with your question.

## TODO

* Вложенные ресурсы
  ```
  /pages/1/comments/2
  ```
* Дополнительные экшены (в качестве исключения)
  ```
  /pages/1/clone
  ```
* Дополнительные экшены для коллекций (в качестве исключения)
  ```
  /pages/archived
  ```
* Секции/области
  ```
  /ru/pages/about
  /en/pages/about
  /pages/about #with default locale
  ```
* query и body? params
  ```
  /pages?filter=active
  ```
* Документация на английском

## License

Copyright © 2016 Mikhail Kuzmin

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

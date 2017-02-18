(ns darkleaf.router.args-test
  (:require [darkleaf.router.args :as sut]
            [clojure.test :refer [deftest is]]))

(deftest parse
  (is (=
       [1 2 3 4 {:a 5, :b 6} [7 8 9]]
       (sut/parse 4 [1 2 3 4 :a 5 :b 6 7 8 9])))
  (is (=
       [1 2 3 4 {} [7 8 9]]
       (sut/parse 4 [1 2 3 4 7 8 9])))
  (is (=
       [1 2 3 4 {:a 5} []]
       (sut/parse 4 [1 2 3 4 :a 5]))))

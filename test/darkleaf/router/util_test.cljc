(ns darkleaf.router.util-test
  (:require [darkleaf.router.util :as sut]
            [clojure.test :refer [deftest is]]))

(deftest parse-args
  (is (=
       [1 2 3 4 {:a 5, :b 6} [7 8 9]]
       (sut/parse-args 4 [1 2 3 4 :a 5 :b 6 7 8 9])))
  (is (=
       [1 2 3 4 {} [7 8 9]]
       (sut/parse-args 4 [1 2 3 4 7 8 9])))
  (is (=
       [1 2 3 4 {:a 5} []]
       (sut/parse-args 4 [1 2 3 4 :a 5]))))

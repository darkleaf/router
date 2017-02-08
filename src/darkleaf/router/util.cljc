(ns darkleaf.router.util
  (:require [clojure.test :as test]))

(test/with-test
  (defn parse-args [ordinal-args-count xs]
    {:pre (< ordinal-args-count (count xs))}
    (let [ordinal-args (take ordinal-args-count xs)
          xs (drop ordinal-args-count xs)]
      (loop [opts {}
             xs xs]
        (if (keyword? (first xs))
          (do
            (assert (some? (second xs)))
            (recur (assoc opts (first xs) (second xs))
                   (drop 2 xs)))
          (-> ordinal-args
              (vec)
              (conj opts)
              (conj xs))))))
  (test/is
   (=
    [1 2 3 4 {:a 5, :b 6} [7 8 9]]
    (parse-args 4 [1 2 3 4 :a 5 :b 6 7 8 9])))
  (test/is
   (=
    [1 2 3 4 {} [7 8 9]]
    (parse-args 4 [1 2 3 4 7 8 9])))
  (test/is
   (=
    [1 2 3 4 {:a 5} []]
    (parse-args 4 [1 2 3 4 :a 5]))))

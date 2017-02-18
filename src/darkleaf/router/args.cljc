(ns darkleaf.router.args)

(defn parse [ordinal-args-count xs]
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

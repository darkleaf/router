(ns darkleaf.router.item-wrappers
  (:require [darkleaf.router.keywords :as k]
            [darkleaf.router.item :as i]))

(deftype Segment [item segment]
  i/Item
  (process [_ req]
    (when (= segment (-> req k/segments peek))
      (i/process item
                 (update req k/segments pop))))
  (fill [_ req]
    (i/fill item
            (update req k/segments conj segment)))
  (explain [_ init]
    (i/explain item
               (update-in init [:req :uri] str "/" segment))))

(defn wrap-segment [item segment]
  (Segment. item segment))

(deftype Scope [item id]
  i/Item
  (process [_ req]
    (i/process item
               (update req k/scope conj id)))
  (fill [_ req]
    (when (= id (-> req k/scope peek))
      (i/fill item
              (update req k/scope pop))))
  (explain [_ init]
    (i/explain item
               (update init :scope conj id))))

(defn wrap-scope [item id]
  (Scope. item id))

(deftype Middleware [item middleware]
  i/Item
  (process [_ req]
    (i/process item
               (update req k/middlewares conj middleware)))
  (fill [_ req]
    (i/fill item req))
  (explain [_ init]
    (i/explain item init)))

(defn wrap-middleware [item middleware]
  (Middleware. item middleware))

(deftype Composite [items]
  i/Item
  (process [_ req]
    (some #(i/process % req) items))
  (fill [_ req]
    (some #(i/fill % req) items))
  (explain [_ init]
    (reduce (fn [acc item]
              (into acc (i/explain item init)))
            []
            items)))

(defn composite [items]
  (Composite. items))

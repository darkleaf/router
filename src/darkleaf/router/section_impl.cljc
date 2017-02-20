(ns darkleaf.router.section-impl
  (:require [darkleaf.router.item :as i]
            [darkleaf.router.keywords :as k]
            [darkleaf.router.args :as args]
            [darkleaf.router.wrapper-impl :refer [wrapper]]))

(deftype Section [id segment children]
  i/Item
  (process [_ req]
    (when (= segment (-> req k/segments peek))
      (-> req
          (update k/segments pop)
          (update k/scope conj id)
          (i/some-process children))))
  (fill [_ req]
    (when (= id (-> req k/scope peek))
      (-> req
          (update k/scope pop)
          (update k/segments conj segment)
          (i/some-fill children))))
  (explain [_ init]
    (-> init
        (update :scope conj id)
        (update-in [:req :uri] str "/" segment)
        (i/explain-all children))))

(defn section [& args]
  (let [[id
         {:keys [middleware segment]
          :or {middleware identity
               segment (name id)}}
         children]
        (args/parse 1 args)]
    (wrapper middleware
             (Section. id segment children))))

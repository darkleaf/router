(ns darkleaf.router
  (:require [darkleaf.router.group-impl :as group-impl]
            [darkleaf.router.section-impl :as section-impl]
            [darkleaf.router.guard-impl :as guard-impl]
            [darkleaf.router.resource-impl :as resource-impl]
            [darkleaf.router.resources-impl :as resources-impl]
            [darkleaf.router.mount-impl :as mount-impl]
            [darkleaf.router.pass-impl :as pass-impl]
            [darkleaf.router.helpers :as helpers])
  #?(:cljs (:require-macros [darkleaf.router :refer [defalias]])))

#?(:clj
   (defmacro ^:private defalias [name orig]
     `(doto (def ~name ~orig)
        (alter-meta! merge (meta (var ~orig))))))

(defalias group group-impl/group)
(defalias section section-impl/section)
(defalias guard guard-impl/guard)
(defalias resource resource-impl/resource)
(defalias resources resources-impl/resources)
(defalias mount mount-impl/mount)
(defalias pass pass-impl/pass)

(defalias make-request-for helpers/make-request-for)
(defalias make-handler helpers/make-handler)
(defalias explain helpers/explain)

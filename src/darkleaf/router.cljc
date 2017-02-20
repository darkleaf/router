(ns darkleaf.router
  (:require [darkleaf.router.composite-impl :as composite-impl]
            [darkleaf.router.wrapper-impl :as wrapper-impl]
            [darkleaf.router.section-impl :as section-impl]
            [darkleaf.router.guard-impl :as guard-impl]
            [darkleaf.router.resource-impl :as resource-impl]
            [darkleaf.router.resources-impl :as resources-impl]
            [darkleaf.router.mount-impl :as mount-impl]
            [darkleaf.router.helpers :as helpers]))

(def composite composite-impl/composite)
(def wrapper wrapper-impl/wrapper)
(def section section-impl/section)
(def guard guard-impl/guard)
(def resource resource-impl/resource)
(def resources resources-impl/resources)
(def mount mount-impl/mount)

(def make-request-for helpers/make-request-for)
(def make-handler helpers/make-handler)
(def explain helpers/explain)

(ns darkleaf.router
  (:require [darkleaf.router.composite :as ns-composite]
            [darkleaf.router.wrapper :as ns-wrapper]
            [darkleaf.router.section :as ns-section]
            [darkleaf.router.resource :as ns-resource]
            [darkleaf.router.resources :as ns-resources]
            [darkleaf.router.helpers :as helpers]))

(def composite ns-composite/composite)
(def wrapper ns-wrapper/wrapper)
(def section ns-section/section)
(def resource ns-resource/resource)
(def resources ns-resources/resources)
(def make-request-for helpers/make-request-for)
(def make-handler helpers/make-handler)

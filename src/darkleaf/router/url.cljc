(ns darkleaf.router.url
  (:require [clojure.string :refer [replace]])
  #?(:clj (:import (java.net URLEncoder))))

(defn- encode-impl [string]
  #?(:clj (URLEncoder/encode string  "UTF-8")
     :cljc (js/encodeURIComponent string)))

(defn encode [string]
  (some-> string
          (str)
          (encode-impl)
          (replace "+" "%20")))

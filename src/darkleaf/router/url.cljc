(ns darkleaf.router.url
  (:require [clojure.string :as str])
  #?(:clj (:import (java.net URLEncoder))))

(defn- encode-impl [string]
  #?(:clj (URLEncoder/encode string  "UTF-8")
     :cljc (js/encodeURIComponent string)))

(defn encode [string]
  (some-> string
          (str)
          (encode-impl)
          (str/replace "+" "%20")))

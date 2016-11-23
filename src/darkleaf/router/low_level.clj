(ns darkleaf.router.low-level)

(defprotocol Processable
  (process [this req]
    "return response or :unprocessable"))

(defrecord App [children]
  Processable
  (process [_ req]
    (some #(process % req) children)))

(defn app [& children]
  (App. children))

(defrecord Scope [name preprocessor children]
  Processable
  (process [_ req]))

(defn scope [name {:keys [preprocessor]} & children]
  (Scope. name preprocessor children))

#_(defrecord Wrapper [preprocessor children]
    Processable
    (process [_ req]))

(defrecord Action [name handler preprocessor]
  Processable
  (process [_ req]
    (some-> req
            preprocessor
            handler)))

(defn action
  [name & {:keys [handler preprocessor]}]
  {:pre [(keyword? name)
         (ifn? handler)
         (ifn? preprocessor)]}
  (Action. name handler preprocessor))


;; (defn scope [s-name
;;              {:keys [vars pattern template]
;;               :or {vars #{}}}
;;              & routes]
;;   {:pre [(keyword? s-name)
;;          (set? vars)
;;          (map? pattern)
;;          (map? template)]}
;;   (map
;;    (fn [route]
;;      (-> route
;;          (update :scope conj s-name)
;;          (update :vars into vars)
;;          (update :pattern #(merge-request-shapes pattern %))
;;          (update :template #(merge-request-shapes template %))))
;;    (flatten routes)))

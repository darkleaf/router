(ns darkleaf.router.pass-impl
  (:require [darkleaf.router.keywords :as k]
            [darkleaf.router.item :as i]
            [darkleaf.router.url :as url]
            [darkleaf.router.item-wrappers :as wrappers]))

(def ^:private actions #{:get :post :patch :put :delete :head :options})

(deftype Pass [id handler]
  i/Item
  (process [_ req]
    (when (actions (-> req :request-method))
      (let [segments (-> req k/segments)
            req (-> req
                    (assoc k/action (:request-method req))
                    (assoc-in [k/params :segments] (vec segments))
                    (dissoc k/segments))]
        [handler req])))
  (fill [_ req]
    (let [segments (get-in req [k/params :segments] [])]
      (when (and (empty? (k/scope req))
                 (actions (k/action req)))
        (-> req
            (assoc :request-method (k/action req))
            (update k/segments into segments)
            (dissoc k/scope)))))
  (explain [_ init]
    (let [encoded-segments (url/encode :segments)]
      (for [action actions]
        (-> init
            (assoc :action action)
            (assoc-in [:params-kmap :segments] encoded-segments)
            (assoc-in [:req :request-method] action)
            (update-in [:req :uri] str "{/" encoded-segments "*}"))))))

(defn pass [id handler & {:keys [segment middleware]
                          :or {segment (name id)}}]
  (cond-> (Pass. id handler)
    middleware (wrappers/wrap-middleware middleware)
    segment    (wrappers/wrap-segment segment)
    :always    (wrappers/wrap-scope id)))

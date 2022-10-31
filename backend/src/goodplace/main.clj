(ns goodplace.main
  (:require [aero.core :as aero]
            [buddy.auth.backends :as backends]
            [buddy.auth.middleware :as bam]
            [easy.system :as es]
            [goodplace.handlers :as handlers]
            [goodplace.middleware :as mw]
            [goodplace.models.users :as users]
            [goodplace.shared.routes :as routes]
            [goodplace.templates.app :refer [template]]
            [inertia.middleware :as inertia]
            [integrant.core :as ig]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [reitit.coercion.schema :as schema-coercion]
            [reitit.dev.pretty :as pretty]
            [reitit.ring :as rr]
            [reitit.ring.coercion :as rrc]
            [reitit.ring.middleware.parameters :as params]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.flash :refer [wrap-flash]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [jsonista.core :as json])
  (:gen-class))

(defn inertia-handler
  ([id]
   (fn [_]
     (inertia/render id)))
  ([id props]
   (fn [_]
     (inertia/render id props))))

(comment
  (inertia/render "something"))

(defn route-implementations
  [context]
  {:home {:get {:handler (inertia-handler :home)}}
   :login {:get {:handler handlers/login}
           :post {:handler (handlers/authenticate context)}}
   :logout {:get {:handler handlers/logout}}

   :users {:get {:handler (handlers/users context)}}
   :edit-user {:get {:handler (handlers/edit-user-get context)}
               :post {:handler (handlers/edit-user-post context)}}
   :create-user {:get {:handler (inertia-handler :create-user)}
                 :post {:handler (handlers/create-user-post context)}}
   :delete-user {:delete {:handler (handlers/delete-user context)}}

   :notes {:get {:handler (handlers/notes context)}}
   :view-note {:get {:handler (handlers/view-note context)}}
   :edit-note {:get {:handler (handlers/edit-note-get context)}
               :post {:handler (handlers/edit-note-post context)}}
   :create-note {:get {:handler (inertia-handler :create-note)}
                 :post {:handler (handlers/create-note-post context)}}
   :delete-note {:delete {:handler (handlers/delete-note context)}}
   :cities {:get {:handler (handlers/cities context)}}
   :something-wrong {:get {:handler (inertia-handler :something-wrong)}}})

(defn check-route-implementations
  [context]
  (->> routes/routes
       (remove #(contains? (route-implementations context) (:id %)))
       (run! #(println "No route implementation found for" (:id %)))))

(def asset-version "1")
(def cookie-store-secret (byte-array 16))
(def backend (backends/session))

(defn wrap-inertia-share
  [handler {:keys [db] :as context}]
  (fn [request]
    (let [user-id (-> request :session :identity :id)
          user (users/get-user-by-id db user-id)
          success (-> request :flash :success)
          errors (-> request :flash :error)
          props {:errors (or errors {})
                 :auth {:user user}
                 :flash {:success success
                         :error nil}}]
      (handler (assoc request :inertia-share props)))))

(defn create-reitit-routes
  [routes impls]
  (reduce (fn [res {:keys [id path]}]
            (conj res
                  [path (get impls id)]))
          []
          routes))

(defn handler
  [{:keys [db] :as context}]
  (reitit.ring/ring-handler
   (reitit.ring/router
    (create-reitit-routes routes/routes (route-implementations context))
    {:conflicts nil
     :exception pretty/exception
     :data {:coercion schema-coercion/coercion
            :middleware [params/parameters-middleware
                         rrc/coerce-exceptions-middleware
                         rrc/coerce-request-middleware
                         rrc/coerce-response-middleware
                         wrap-keyword-params
                         [wrap-session
                          {:store (cookie-store {:key cookie-store-secret})}]
                         wrap-flash
                         [bam/wrap-authentication backend]
                         mw/wrap-auth
                         [wrap-inertia-share context]
                         [inertia/wrap-inertia template asset-version]]}})
   (reitit.ring/routes
    (reitit.ring/create-file-handler {:path "/"})
    (reitit.ring/create-default-handler
     {:not-found
      (->
       (fn [request]
         (inertia/render :something-wrong {:errors ["Page not found"]
                                           :redirect (routes/get-route-path :home)}))
       (inertia/wrap-inertia template asset-version))}))))

(defmethod ig/init-key ::server
  [_ {:keys [port dynamic? db] :as opts}]
  (log/infof "Starting Server {port: %d}" port)
  (let [context {:db db}]
    (check-route-implementations context)
    (jetty/run-jetty
     (if dynamic?
       (fn [request]
         ((handler context) request))
       (handler context))
     {:port port :join? false})))

(defmethod ig/halt-key! ::server [_ server]
  (when server
    (.stop server)))

(def system nil)

(defn log-config
  [system-config]
  nil)

(def uncaught-exception-handler
  (reify Thread$UncaughtExceptionHandler
    (uncaughtException [_ thread throwable]
      (log/fatal throwable "Shutting down service with Exception")
      (ig/halt! system))))

(defn -main
  [& _]
  (Thread/setDefaultUncaughtExceptionHandler uncaught-exception-handler)
  (let [system-config (es/system-config {:profile :prod})
        sys (ig/init system-config)]
    (log-config system-config)
    (.addShutdownHook
     (Runtime/getRuntime)
     (Thread.
      (fn []
        (ig/halt! sys))))
    (alter-var-root #'system (constantly sys)))
  @(promise))

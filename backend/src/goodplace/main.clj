(ns goodplace.main
  (:require [aero.core :as aero]
            [buddy.auth.backends :as backends]
            [buddy.auth.middleware :as bam]
            [easy.system :as es]
            [goodplace.handlers :refer [handlers]]
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
            [prone.middleware :as prone]
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

(defn check-handlers
  [handlers context]
  (->> routes/routes
       (remove #(contains? (handlers context) (:id %)))
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
  [routes handlers]
  (reduce (fn [res {:keys [id path]}]
            (conj res
                  [path (get handlers id)]))
          []
          routes))

(def not-found-inertia
  (-> (fn [request]
        (inertia/render :something-wrong
                        {:errors ["Page not found"]
                         :redirect (routes/get-route-path :home)}))
      (inertia/wrap-inertia template asset-version)))

(def not-found-plain
  (constantly {:status 404, :body "Page Not Found"}))

(defn handler
  [context]
  (-> (reitit.ring/ring-handler
       (reitit.ring/router
        (create-reitit-routes routes/routes (handlers context))
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
         {:not-found not-found-inertia})))
      ;; Prone must be a top level middleware
      (prone/wrap-exceptions {:app-namespaces ['goodplace]})))

(defmethod ig/init-key ::server
  [_ {:keys [port dynamic? db postgres] :as opts}]
  (log/infof "Starting Server {port: %d}" port)
  (let [context {:db db
                 :postgres postgres}]
    (check-handlers handlers context)
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

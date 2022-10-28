(ns goodplace.main
  (:require [aero.core :as aero]
            [easy.system :as es]
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
            [ring.middleware.session.cookie :refer [cookie-store]]))

(def system nil)

(defn log-config
  [system-config]
  (let [db-parameters (-> system-config
                          (get-in [:excel.api.db/crux-node
                                   :crux.jdbc/connection-pool
                                   :db-spec])
                          (update :password
                                  (fn [password]
                                    (when-not (nil? password)
                                      "<omitted>"))))]
    (log/infof "Database Connection: %s" db-parameters)))

(def uncaught-exception-handler
  (reify Thread$UncaughtExceptionHandler
    (uncaughtException [_ thread throwable]
      (log/fatal throwable "Shutting down service with Exception")
      (ig/halt! system))))

(defn inertia-handler
  [id]
  (fn [_]
    (inertia/render id)))

(defn create-reitit-routes
  [routes impls]
  (reduce (fn [res {:keys [id path]}]
            (conj res
                  [path (get impls id)]))
          []
          routes))

(def route-implementations
  {:home {:get {:handler (inertia-handler :home)}}
   :about {:get {:handler (inertia-handler :about)}}})

#_
(defn handler
  [request]
  {:status 200
   :body "OK"})

(def asset-version "1")
(def cookie-store-secret (byte-array 16))

#_
(defn wrap-inertia-share [handler db]
  (fn [request]
    (prn (:session request))
    (let [user-id (-> request :session :identity :id)
          user (db/get-user-by-id db user-id)
          success (-> request :flash :success)
          errors (-> request :flash :error)
          props {:errors (or errors {})
                 :auth {:user user}
                 :flash {:success success
                         :error nil}}]
      (handler (assoc request :inertia-share props)))))

(def handler
  (reitit.ring/ring-handler
   (reitit.ring/router
    (create-reitit-routes routes/routes route-implementations)
    {:conflicts nil
     :exception pretty/exception
     :data {:coercion schema-coercion/coercion
            :middleware [params/parameters-middleware
                         rrc/coerce-exceptions-middleware
                         rrc/coerce-request-middleware
                         rrc/coerce-response-middleware
                         wrap-keyword-params
                         [wrap-session {:store (cookie-store {:key cookie-store-secret})}]
                         wrap-flash
                         #_
                         [bam/wrap-authentication backend]
                         #_
                         [wrap-inertia-share db]
                         [inertia/wrap-inertia template asset-version]]}} )
   (reitit.ring/routes
    (reitit.ring/create-file-handler {:path "/"})
    (reitit.ring/create-default-handler
     {:not-found (constantly {:status 404})})) ))

(defn apply-middleware
  [handler middleware]
  (reduce (fn [handler f]
            (f handler))
          handler
          middleware))

(defmethod ig/init-key ::server
  [_ {:keys [port dynamic? middleware] :as opts}]
  (log/infof "Starting Server {port: %d}" port)
  (jetty/run-jetty
   (if dynamic?
     (fn [req]
       (let [handler (apply-middleware handler middleware)]
         (handler req)))
     (apply-middleware handler middleware))
   {:port port :join? false}))

(defmethod ig/halt-key! ::server [_ server]
  (when server
    (.stop server)))

(defn dev?
  [profile]
  (= :dev profile))

(comment
  (config nil)

  (es/system-config nil)

  )

(defn system-config
  [{:keys [profile]}]
  {::server {:port (if (dev? profile) 8090 8080)}})

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

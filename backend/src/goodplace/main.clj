(ns goodplace.main
  (:require [aero.core :as aero]
            [easy.system :as es]
            [integrant.core :as ig]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [ring.adapter.jetty :as jetty]))

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

(defn handler
  [request]
  {:status 200
   :body "OK"})

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

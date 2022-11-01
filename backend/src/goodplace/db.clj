(ns goodplace.db
  (:require [integrant.core :as ig]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [clojure.tools.logging :as log]
            [pg-embedded-clj.core :as pge]))

(defmethod ig/init-key ::db
  [_ {:keys [db-spec]}]
  (println "Starting db")
  (let [ds (jdbc/get-datasource db-spec)]
    (jdbc/with-options ds {:builder-fn rs/as-unqualified-maps})))

(defmethod ig/suspend-key! ::db
  [_ impl]
  impl)

(defmethod ig/resume-key ::db
  [key opts old-opts old-impl]
  (if (= opts old-opts)
    old-impl
    (do (ig/halt-key! key old-impl)
        (ig/init-key key opts))))

(defmethod ig/halt-key! ::db
  [_ node]
  (println "Stopping db (noop)")
  nil)

(defmethod ig/init-key ::postgres-client
  [_ {:keys [db-spec]}]
  (println "Creating postgres client")
  (let [ds (jdbc/get-datasource db-spec)]
    (jdbc/with-options ds {:builder-fn rs/as-unqualified-maps})))

(defmethod ig/init-key ::postgres-server
  [_ {:keys [db-spec]}]
  (println "Creating postgres server")
  (pge/init-pg))

(defmethod ig/suspend-key! ::postgres-server
  [_ impl]
  impl)

(defmethod ig/resume-key ::postgres-server
  [key opts old-opts old-impl]
  (if (= opts old-opts)
    old-impl
    (do (ig/halt-key! key old-impl)
        (ig/init-key key opts))))

(defmethod ig/halt-key! ::postgres-server
  [_ node]
  (println "Stopping postgres server")
  (pge/halt-pg!))

(comment
  (def ds
    (jdbc/get-datasource {:dbtype "postgres"
                          :user "postgres"
                          :password "postgres"
                          :port 5432}))

  (jdbc/execute! ds ["SELECT * FROM pg_catalog.pg_tables;"])

  (def embedded-pg
    (pge/init-pg))



  )

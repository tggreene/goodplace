(ns goodplace.db
  (:require [integrant.core :as ig]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [clojure.tools.logging :as log]))

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


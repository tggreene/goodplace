(ns goodplace.db
  (:require [clojure.tools.logging :as log]
            [integrant.core :as ig]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [hikari-cp.core :as hikari] ))

(defmethod ig/init-key ::pool
  [_ {:keys [options]}]
  (log/info "Creating" ::pool)
  (hikari/make-datasource options))

(defmethod ig/suspend-key! ::pool
  [_ impl]
  impl)

(defmethod ig/resume-key ::pool
  [key opts old-opts old-impl]
  (if (= opts old-opts)
    old-impl
    (do (ig/halt-key! key old-impl)
        (ig/init-key key opts))))

(defmethod ig/halt-key! ::pool
  [_ datasource]
  (log/info "Stopping" ::pool)
  (hikari/close-datasource datasource))

(defmethod ig/init-key ::postgres-client
  [_ {:keys [datasource]}]
  (log/info "Creating" ::postgres-client)
  (jdbc/with-options datasource {:builder-fn rs/as-unqualified-maps}))

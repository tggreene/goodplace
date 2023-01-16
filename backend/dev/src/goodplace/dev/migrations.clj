(ns goodplace.dev.migrations
  (:require [clojure.tools.logging :as log]
            [integrant.core :as ig]
            [migratus.core :as migratus]))

(defmethod ig/init-key ::migrations
  [_ config]
  (log/info "Starting" ::migrations)
  (migratus/migrate config))

(comment
  ;; run main migrations

  (def main-migration-config (:goodplace.migrations/migrations integrant.repl.state/config))

  (let [config (:goodplace.migrations/migrations integrant.repl.state/config)
        config (assoc config :db {:datasource (:comedyedits.db/pool integrant.repl.state/system)})]
    (migratus/migrate config))

  ;; create main migrations

  (migratus/create (:goodplace.migrations/migrations integrant.repl.state/config)
                   "create-users-table")

  (migratus/create (:goodplace.migrations/migrations integrant.repl.state/config)
                   "create-notes-table")

  )

(comment
  ;; run dev migrations

  (let [config (::migrations integrant.repl.state/config)
        config (assoc config :db {:datasource (:comedyedits.db/pool integrant.repl.state/system)})]
    (migratus/migrate config))

  ;; create dev migrations

  (migratus/create (::migrations integrant.repl.state/config) "create-test-users")
  (migratus/create (::migrations integrant.repl.state/config) "create-test-notes")

  )

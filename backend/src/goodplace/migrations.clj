(ns goodplace.migrations
  (:require [integrant.core :as ig]
            [goodplace.migrations.tables :as tables]
            [goodplace.migrations.data :as data]))

(defmethod ig/init-key ::migrations
  [_ {:keys [postgres]}]
  (println "Running migrations...")
  (tables/create-users-table! postgres)
  (tables/create-notes-table! postgres)
  (data/create-test-users! postgres)
  (data/create-test-notes! postgres)
  (println "Finished migrations!"))


(comment
  (def datasource
    (-> {:dbtype "sqlite"
         :dbname "resources/db/db.sqlite"}
        jdbc/get-datasource
        (jdbc/with-options {:builder-fn result-set/as-unqualified-maps })))

  (tables/create-users-table!
   (:goodplace.db/postgres-client integrant.repl.state/system))

  )

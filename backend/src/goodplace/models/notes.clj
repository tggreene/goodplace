(ns goodplace.models.notes
  (:require [crypto.password.bcrypt :as password]
            [honey.sql :as h]
            [honey.sql.helpers :refer [where]]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]))

(defn create-notes-table!
  [db]
  (jdbc/execute!
   db
   [(str "CREATE TABLE notes (\n"
         "  id TEXT PRIMARY KEY,\n"
         "  user INTEGER NOT NULL,\n"
         "  title TEXT NOT NULL,\n"
         "  contents TEXT NOT NULL,\n"
         "  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,\n"
         "  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,\n"
         "  deleted_at DATETIME,\n"
         "  FOREIGN KEY(user) REFERENCES user(id)"
         ");")]))

(defn destroy-notes-table!
  [db]
  (jdbc/execute!
   db
   ["DROP TABLE users;"]))

(defn get-note-by-id
  [db note-id]
  (let [query (h/format {:select [:*]
                         :from [:notes]
                         :where [:and
                                 [:= :id note-id]
                                 [:= :deleted_at nil]]})]
    (jdbc/execute-one! db query)))

(defn create-note!
  [db note]
  (let [id (random-uuid)
        note (assoc note :id id)
        query (h/format {:insert-into :notes
                         :values [note]})]
    (jdbc/execute! db query)
    (get-note-by-id db id)))

(defn update-note!
  [db {:keys [id] :as note}]
  (let [query (h/format {:update :notes
                         :set (merge note
                                     {:updated_at :current_timestamp})
                         :where [:= :id id]})]
    (jdbc/execute-one! db query)
    (get-note-by-id db id)))

(defn list-all-notes
  [db]
  (let [query (h/format {:select [:*]
                         :from [:notes]
                         :where [:= :deleted_at nil]
                         :order-by [:created_at]})]
    (jdbc/execute! db query)))

(defn list-user-notes
  [db user-id]
  (let [query (h/format {:select [:*]
                         :from [:notes]
                         :where [:and
                                 [:= :user user-id]
                                 [:= :deleted_at nil]]
                         :order-by [:created_at]})]
    (jdbc/execute! db query)))

(defn soft-delete-note!
  [db id]
  (let [query (h/format {:update :notes
                         :set {:deleted_at :current_timestamp
                               :updated_at :current_timestamp}
                         :where [:= :id id]})]
    (jdbc/execute-one! db query)))

(defn hard-delete-note!
  [db id]
  (let [query (h/format {:delete-from :notes
                         :where [:= :id id]})]
    (jdbc/execute-one! db query)))



(comment
  (def db (:goodplace.db/db integrant.repl.state/system))

  (list-all-notes db)

  (get-user-by-id db 1)

  (create-notes-table! db)

  (destroy-notes-table! db)

  (create-note!
   db
   {:user 1
    :title "Test"
    :contents "Something something"})

  (create-note!
   db
   {:user 1
    :title "My Thoughts About Blah Blah Blah"
    :contents "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."})

  (get-note-by-id db "d6012295-c3e5-4961-a529-b95f20121065")

  (hard-delete-note! db "5546f503-2c4e-49c9-8ba6-4a5e380c9fde")

  )

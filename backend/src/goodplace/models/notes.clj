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

(defn create-note!
  [db note]
  (let [id (random-uuid)
        query (h/format {:insert-into :notes
                         :values [(assoc note :id id)]})]
    (jdbc/execute! db query)))

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

  )

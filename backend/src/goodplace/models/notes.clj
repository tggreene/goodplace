(ns goodplace.models.notes
  (:require [crypto.password.bcrypt :as password]
            [honey.sql :as h]
            [honey.sql.helpers :refer [where]]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [goodplace.utils.coerce :as coerce]))

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

(defn create-notes-table-postgres!
  [db]
  (jdbc/execute!
   db
   [(str "CREATE TABLE notes (\n"
         "  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),\n"
         "  user_id INTEGER NOT NULL,\n"
         "  title TEXT NOT NULL,\n"
         "  contents TEXT NOT NULL,\n"
         "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n"
         "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n"
         "  deleted_at TIMESTAMP,\n"
         "  CONSTRAINT fk_user_id FOREIGN KEY(user_id) REFERENCES users(id)"
         ");")]))

(defn destroy-notes-table!
  [db]
  (jdbc/execute!
   db
   ["DROP TABLE notes;"]))

(defn get-note-by-id
  [db note-id]
  (let [query (h/format {:select [:*]
                         :from [:notes]
                         :where [:and
                                 [:= :id (coerce/to-uuid note-id)]
                                 [:= :deleted_at nil]]})]
    (jdbc/execute-one! db query)))

(defn create-note!
  [db note]
  (let [id (random-uuid)
        note (-> note
                 (assoc :id id)
                 (update :user_id coerce/to-int))
        query (h/format {:insert-into :notes
                         :values [note]
                         :returning [:id]})]
    (jdbc/execute! db query)))

(defn update-note!
  [db {:keys [id] :as note}]
  (let [query (h/format {:update :notes
                         :set (-> note
                                  (dissoc :id)
                                  (assoc :updated_at :current_timestamp))
                         :where [:= :id (coerce/to-uuid id)]})]
    (jdbc/execute-one! db query)))

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
                                 [:= :user_id (coerce/to-int user-id)]
                                 [:= :deleted_at nil]]
                         :order-by [:created_at]})]
    (jdbc/execute! db query)))

(defn soft-delete-note!
  [db id]
  (let [query (h/format {:update :notes
                         :set {:deleted_at :current_timestamp
                               :updated_at :current_timestamp}
                         :where [:= :id (coerce/to-uuid id)]})]
    (jdbc/execute-one! db query)))

(defn hard-delete-note!
  [db id]
  (let [query (h/format {:delete-from :notes
                         :where [:= :id (coerce/to-uuid id)]})]
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

(comment
  (def pg (:goodplace.db/postgres-client integrant.repl.state/system))

  pg

  (jdbc/execute! pg ["SELECT version();"])

  (doseq [note (->> (list-all-notes db)
                    (map #(-> %
                              (clojure.set/rename-keys {:user :user_id})
                              (dissoc :created_at :updated_at :deleted_at))))]
    (clojure.pprint/pprint note)
    (create-note! pg note))

  (clojure.pprint/pprint (list-all-notes pg))

  (create-notes-table-postgres! pg)

  (destroy-notes-table! pg)

  )

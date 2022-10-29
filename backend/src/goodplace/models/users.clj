(ns goodplace.models.users
  (:require [crypto.password.bcrypt :as password]
            [honey.sql :as h]
            [honey.sql.helpers :refer [where]]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]))

(defn create-users-table!
  [db]
  (jdbc/execute!
   db
   [(str "CREATE TABLE users (\n"
         "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
         "  first_name TEXT NOT NULL,\n"
         "  last_name TEXT NOT NULL,\n"
         "  username TEXT NOT NULL UNIQUE,\n"
         "  email TEXT NOT NULL UNIQUE,\n"
         "  password TEXT NOT NULL UNIQUE,\n"
         "  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,\n"
         "  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,\n"
         "  deleted_at DATETIME\n"
         ");")]))

(defn destroy-users-table!
  [db]
  (jdbc/execute!
   db
   ["DROP TABLE users;"]))

(defn create-user!
  [db user]
  (let [encrypted-user (cond-> user
                         (:password user)
                         (update :password password/encrypt))
        query (h/format {:insert-into :users
                         :values [encrypted-user]})]
    (jdbc/execute! db query)))

(defn sanitize-user
  [user]
  (dissoc user :password))

(defn update-user!
  [db user]
  (let [encrypted-user (cond-> user
                         (:password user)
                         (update :password password/encrypt))
        query (h/format {:update :users
                         :set (merge encrypted-user
                                     {:updated_at :current_timestamp})
                         :where [:= :email]})]
    (jdbc/execute! db query)))

(defn soft-delete-user!
  [db id]
  (let [query (h/format {:update :users
                         :set {:deleted_at :current_timestamp
                               :updated_at :current_timestamp}
                         :where [:= :id id]})]
    (jdbc/execute-one! db query)))

(defn restore-deleted-user!
  [db id]
  (let [query (h/format {:update :users
                         :set {:deleted_at nil
                               :updated_at :current_timestamp}
                         :where [:= :id id]})]
    (jdbc/execute-one! db query)))

(defn hard-delete-user
  [db id]
  (let [query (h/format {:update :users
                         :set {:deleted_at nil
                               :updated_at :current_timestamp}
                         :where [:= :id id]})]
    (jdbc/execute-one! db query)))

(defn list-users
  [db]
  (let [query (h/format {:select [:id :first_name]
                         :from [:users]
                         :where [:= :deleted_at nil]
                         :order-by [:id]})]
    (jdbc/execute-one! db query)))

(defn get-user-by-id
  [db id]
  (let [query (h/format {:select [:*]
                         :from [:users]
                         :where [:= :id id]})]
    (jdbc/execute-one! db query)))

(defn get-user-by-email
  [db email]
  (let [query (h/format {:select [:*]
                         :from [:users]
                         :where [:= :email email]})]
    (jdbc/execute-one! db query)))

(comment
  (def temp-db (:goodplace.db/db integrant.repl.state/system))

  (list-users temp-db)

  (get-user-by-id temp-db 1)

  (create-users-table temp-db)

  (destroy-users-table temp-db)

  (create-user
   temp-db
   {:first_name "Tim"
    :last_name "Greene"
    :username "tggreene"
    :email "tim.g.greene@gmail.com"
    :password "password"})

  )

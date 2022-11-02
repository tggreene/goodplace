(ns goodplace.models.users
  (:require [crypto.password.bcrypt :as password]
            [goodplace.utils.coerce :as coerce]
            [honey.sql :as h]
            [honey.sql.helpers :refer [where]]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]))

;; sqlite
(defn create-users-table-sqlite!
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

;; postgres
(defn create-users-table-postgres!
  [db]
  (jdbc/execute!
   db
   [(str "CREATE TABLE users (\n"
         "  id SERIAL PRIMARY KEY,\n"
         "  first_name TEXT NOT NULL,\n"
         "  last_name TEXT NOT NULL,\n"
         "  username TEXT NOT NULL UNIQUE,\n"
         "  email TEXT NOT NULL UNIQUE,\n"
         "  password TEXT NOT NULL UNIQUE,\n"
         "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n"
         "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n"
         "  deleted_at TIMESTAMP\n"
         ");")]))

(def create-users-table! create-users-table-sqlite!)

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
                         :values [encrypted-user]
                         :on-conflict [:id]
                         :do-nothing []
                         :returning [:id]})]
    (jdbc/execute! db query)))

(defn create-user-postgres!
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
  (let [{:keys [id]
         :as encrypted-user} (cond-> user
                               :always
                               (update :id coerce/to-int)
                               (:password user)
                               (update :password password/encrypt))
        query (h/format {:update :users
                         :set (-> encrypted-user
                                  (dissoc :id)
                                  (assoc :updated_at :current_timestamp))
                         :where [:= :id id]})]
    (jdbc/execute! db query)))

(defn soft-delete-user!
  [db id]
  (let [query (h/format {:update :users
                         :set {:deleted_at :current_timestamp
                               :updated_at :current_timestamp}
                         :where [:= :id (coerce/to-int id)]})]
    (jdbc/execute-one! db query)))

(defn restore-deleted-user!
  [db id]
  (let [query (h/format {:update :users
                         :set {:deleted_at nil
                               :updated_at :current_timestamp}
                         :where [:= :id (coerce/to-int id)]})]
    (jdbc/execute-one! db query)))

(defn hard-delete-user!
  [db id]
  (let [query (h/format {:delete-from :users
                         :where [:= :id (coerce/to-int id)]})]
    (jdbc/execute-one! db query)))

(defn list-users
  [db]
  (let [query (h/format {:select [:id :first_name :last_name :username :email
                                  :created_at :updated_at]
                         :from [:users]
                         :where [:= :deleted_at nil]
                         :order-by [:id]})]
    (jdbc/execute! db query)))

(defn get-user-by-id
  [db id]
  (let [query (h/format {:select [:*]
                         :from [:users]
                         :where [:= :id (coerce/to-int id)]})]
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

  (restore-deleted-user! temp-db 1)

  (create-user
   temp-db
   {:first_name "Tim"
    :last_name "Greene"
    :username "tggreene"
    :email "tim.g.greene@gmail.com"
    :password "password"})

  )

(comment
  (def db (:goodplace.db/db integrant.repl.state/system))

  (def pg (:goodplace.db/postgres-client integrant.repl.state/system))

  (prn pg)

  (list-users pg)

  (get-user-by-id db 1)

  (create-users-table db)

  (destroy-users-table db)

  (restore-deleted-user! db 1)

  (create-users-table-postgres! pg)

  (doseq [user (list-users db)
          :let [user (-> user
                         (dissoc :id :created_at)
                         (assoc :password "password"))]]
    (create-user! pg user))

  (clojure.pprint/pprint (list-users pg))

  (hard-delete-user! pg 5)

  (create-user!
   pg
   {:email "x@y.z",
    :first_name "x",
    :last_name "y",
    :password "password",
    :username "z"}
   )

  )

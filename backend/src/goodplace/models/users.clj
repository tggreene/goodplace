(ns goodplace.models.users
  (:require [crypto.password.bcrypt :as password]
            [goodplace.utils.coerce :as coerce]
            [honey.sql :as h]
            [honey.sql.helpers :refer [where]]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]))

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
    (jdbc/execute-one! db query)))

(defn update-user!
  [db user]
  (let [{:keys [id]
         :as encrypted-user} (cond-> user
                               :always
                               (update :id coerce/to-uuid)
                               (:password user)
                               (update :password password/encrypt))
        query (h/format {:update :users
                         :set (-> encrypted-user
                                  (dissoc :id)
                                  (assoc :updated_at :current_timestamp))
                         :where [:= :id id]})]
    (jdbc/execute-one! db query)))

(defn soft-delete-user!
  [db id]
  (let [query (h/format {:update :users
                         :set {:deleted_at :current_timestamp
                               :updated_at :current_timestamp}
                         :where [:= :id (coerce/to-uuid id)]})]
    (jdbc/execute-one! db query)))

(defn restore-deleted-user!
  [db id]
  (let [query (h/format {:update :users
                         :set {:deleted_at nil
                               :updated_at :current_timestamp}
                         :where [:= :id (coerce/to-uuid id)]})]
    (jdbc/execute-one! db query)))

(defn hard-delete-user!
  [db id]
  (let [query (h/format {:delete-from :users
                         :where [:= :id (coerce/to-uuid id)]})]
    (jdbc/execute-one! db query)))

(defn list-users
  [db]
  (let [query (h/format {:select [:id :first_name :last_name :email
                                  :role :created_at :updated_at]
                         :from [:users]
                         :where [:= :deleted_at nil]
                         :order-by [:id]})]
    (jdbc/execute! db query)))

(defn get-user-by-id
  [db id]
  (let [query (h/format {:select [:*]
                         :from [:users]
                         :where [:= :id (coerce/to-uuid id)]})]
    (jdbc/execute-one! db query)))

(defn get-user-by-email
  [db email]
  (let [query (h/format {:select [:*]
                         :from [:users]
                         :where [:= :email email]})]
    (jdbc/execute-one! db query)))

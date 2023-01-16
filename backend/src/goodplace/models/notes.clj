(ns goodplace.models.notes
  (:require [crypto.password.bcrypt :as password]
            [honey.sql :as h]
            [honey.sql.helpers :refer [where]]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [goodplace.utils.coerce :as coerce]))

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
  (let [note (update note :user_id coerce/to-int)
        query (h/format {:insert-into :notes
                         :values [note]
                         :returning [:id]
                         :on-conflict [:id]
                         :do-nothing []})]
    (jdbc/execute-one! db query)))

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
                                 [:= :user_id (coerce/to-uuid user-id)]
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

  (count (list-all-notes pg))

  (doseq [{:keys [id]} (list-all-notes pg)]
    (hard-delete-note! pg id))

  )

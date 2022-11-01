(ns goodplace.handlers
  (:require [clojure.string :as str]
            [crypto.password.bcrypt :as password]
            [goodplace.examples.cities]
            [goodplace.handlers.users :as users]
            [goodplace.models.notes :as notes]
            [goodplace.models.users :as users-model]
            [goodplace.shared.routes :as routes]
            [goodplace.utils.pagination :as pagination]
            [inertia.middleware :as inertia]
            [ring.util.response :as response]))

(defn get-user
  [request]
  (some-> request
          :session
          :identity))

(defn authenticated?
  [request]
  (-> request
      :session
      :identity
      not-empty
      boolean))

(defn authenticate
  "Check request username and password against authdata
  username and passwords."
  [{:keys [db] :as context}]
  (fn [request]
    (let [email (-> request :body-params :email)
          password (-> request :body-params :password)
          user (users-model/get-user-by-email db email)
          sanitized-user (dissoc user :password)
          session (:session request)]
      (if (and user (password/check password (:password user)))
        (let [updated-session (assoc session :identity sanitized-user)]
          (-> (response/redirect (routes/get-route-path :home))
              (assoc :session updated-session)))
        (-> (response/redirect (routes/get-route-path :login))
            (assoc :flash
                   {:error
                    {:email "These credentials do not match our records."}}))))))

(defn login
  [request]
  (if (authenticated? request)
    (response/redirect (routes/get-route-path :home) :see-other)
    (inertia/render :login)))

(defn logout
  [_]
  (-> (response/redirect "/" :see-other)
      (assoc :session nil)))

(def per-page 10)

(defn notes
  [{:keys [db]}]
  (fn [request]
    (let [{:keys [id] :as user} (get-user request)
          notes (notes/list-user-notes db id)]
      (inertia/render :notes {:notes notes}))))

(defn view-note
  [{:keys [db]}]
  (fn [request]
    (let [{:keys [id] :as user} (get-user request)
          note-id (get-in request [:path-params :note-id])
          note (notes/get-note-by-id db note-id)]
      (cond
        (= id (:user note)) (inertia/render :view-note {:note note})
        note (inertia/render :home {:errors ["Not permitted to view note"]
                                    :redirect (routes/get-route-path :notes)})
        :else (inertia/render :home {:errors ["Note not found"]
                                     :redirect (routes/get-route-path :notes)})))))

(defn edit-note-get
  [{:keys [db]}]
  (fn [request]
    (let [{:keys [id] :as user} (get-user request)
          note-id (get-in request [:path-params :note-id])
          note (notes/get-note-by-id db note-id)]
      (cond
        (= id (:user note)) (inertia/render :edit-note {:note note})
        note (inertia/render :home {:errors ["Not permitted to view note"]
                                    :redirect (routes/get-route-path :notes)})
        :else (inertia/render :home {:errors ["Note not found"]
                                     :redirect (routes/get-route-path :notes)})))))

(defn edit-note-post
  [{:keys [db]}]
  (fn [request]
    (let [note (:body-params request)
          _ (notes/update-note! db note)
          new-note (notes/get-note-by-id db (:id note))]
      (inertia/render :view-note {:note new-note}))))

(defn create-note-post
  [{:keys [db]}]
  (fn [request]
    (let [user (get-user request)
          note (:body-params request)
          {:keys [id] :as new-note} (notes/create-note! db (assoc note :user (:id user)))]
      (response/redirect (routes/get-route-path :view-note {:note-id id}) :see-other))))

(defn delete-note
  [{:keys [db]}]
  (fn [request]
    (let [user (get-user request)
          note-id (get-in request [:path-params :note-id])]
      (notes/soft-delete-note! db note-id)
      (response/redirect (routes/get-route-path :notes) :see-other))))

(defn cities
  [context]
  (fn [{:keys [params query-string uri] :as request}]
    (let [filters (select-keys params [:search :country])
          all-cities @goodplace.examples.cities/cities
          page (Integer/parseInt (get params :page "1"))
          offset (* (dec page) 10)
          count (count all-cities)
          cities (->> all-cities
                      (drop offset)
                      (take 10))
          props {:cities {:data cities
                          :current_page page
                          :links (pagination/links uri query-string page count 10)}
                 :filters filters}]
      (inertia/render :cities props))))

(defn inertia-handler
  ([id]
   (fn [_]
     (inertia/render id)))
  ([id props]
   (fn [_]
     (inertia/render id props))))

(defn handlers
  [context]
  {:home {:get {:handler (inertia-handler :home)}}
   :login {:get {:handler login}
           :post {:handler (authenticate context)}}
   :logout {:get {:handler logout}}
   :users {:get {:handler (users/list-users context)}}
   :edit-user {:get {:handler (users/edit-user-get context)}
               :post {:handler (users/edit-user-post context)}}
   :create-user {:get {:handler (inertia-handler :create-user)}
                 :post {:handler (users/create-user-post context)}}
   :delete-user {:delete {:handler (users/delete-user context)}}
   :notes {:get {:handler (notes context)}}
   :view-note {:get {:handler (view-note context)}}
   :edit-note {:get {:handler (edit-note-get context)}
               :post {:handler (edit-note-post context)}}
   :create-note {:get {:handler (inertia-handler :create-note)}
                 :post {:handler (create-note-post context)}}
   :delete-note {:delete {:handler (delete-note context)}}
   :cities {:get {:handler (cities context)}}
   :something-wrong {:get {:handler (inertia-handler :something-wrong)}}})

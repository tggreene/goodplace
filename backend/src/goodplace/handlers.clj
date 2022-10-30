(ns goodplace.handlers
  (:require [clojure.string :as str]
            [crypto.password.bcrypt :as password]
            [goodplace.examples.cities]
            [goodplace.models.notes :as notes]
            [goodplace.models.users :as users]
            [goodplace.shared.routes :as routes]
            [inertia.middleware :as inertia]
            [ring.util.response :as response]
            [goodplace.utils.pagination :as pagination]))

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
          user (users/get-user-by-email db email)
          sanitized-user (dissoc user :password)
          session (:session request)]
      (if (and user (password/check password (:password user)))
        (let [updated-session (assoc session :identity sanitized-user)]
          (-> (response/redirect "/")
              (assoc :session updated-session)))
        (-> (response/redirect "/login")
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

#_
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

(def per-page 10)

(defn users
  [{:keys [db]}]
  (fn [{:keys [params query-string uri] :as request}]
    (let [all-users (users/list-users db)
          page (Integer/parseInt (get params :page "1"))
          offset (* (dec page) per-page)
          count (count all-users)
          users (->> all-users
                     (drop offset)
                     (take per-page))
          props {:users {:data users
                         :current_page page
                         :links (pagination/links uri query-string page count per-page)}}]
      (inertia/render :users props))))

(defn view-user
  [{:keys [db]}]
  (fn [request]
    (let [{:keys [id] :as user} (get-user request)
          user-id (get-in request [:path-params :user-id])
          user (users/get-user-by-id db user-id)]
      (cond
        (= id (:user user)) (inertia/render :view-user {:user user})
        user (inertia/render :home {:errors ["Not permitted to view user"]
                                    :redirect (routes/get-route-path :users)})
        :else (inertia/render :home {:errors ["User not found"]
                                     :redirect (routes/get-route-path :users)})))))

(defn edit-user-get
  [{:keys [db]}]
  (fn [request]
    (let [{:keys [id] :as user} (get-user request)
          user-id (get-in request [:path-params :user-id])
          user (users/get-user-by-id db user-id)]
      (cond
        (= id (:user user)) (inertia/render :edit-user {:user user})
        user (inertia/render :home {:errors ["Not permitted to view user"]
                                    :redirect (routes/get-route-path :users)})
        :else (inertia/render :home {:errors ["User not found"]
                                     :redirect (routes/get-route-path :users)})))))

(defn edit-user-post
  [{:keys [db]}]
  (fn [request]
    (let [user (:body-params request)
          _ (users/update-user! db user)
          new-user (users/get-user-by-id db (:id user))]
      (inertia/render :view-user {:user new-user}))))

(defn create-user-post
  [{:keys [db]}]
  (fn [request]
    (let [user (get-user request)
          user (:body-params request)
          {:keys [id] :as new-user} (users/create-user! db (assoc user :user (:id user)))]
      (response/redirect (routes/get-route-path :view-user {:user-id id}) :see-other))))

(defn delete-user
  [{:keys [db]}]
  (fn [request]
    (let [user (get-user request)
          user-id (get-in request [:path-params :user-id])]
      (users/soft-delete-user! db user-id)
      (response/redirect (routes/get-route-path :users) :see-other))))

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

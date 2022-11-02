(ns goodplace.handlers.users
  (:require [goodplace.handlers.common :as common]
            [goodplace.models.users :as model]
            [goodplace.shared.routes :as routes]
            [goodplace.utils.pagination :as pagination]
            [inertia.middleware :as inertia]
            [ring.util.response :as response]
            [goodplace.utils.coerce :as coerce]))

(defn list-users
  [{:keys [postgres]}]
  (fn [{:keys [params query-string uri] :as request}]
    (let [all-users (model/list-users postgres)
          page (Integer/parseInt (get params :page "1"))
          offset (* (dec page) common/per-page)
          count (count all-users)
          users (->> all-users
                     (drop offset)
                     (take common/per-page))
          props {:users
                 {:data users
                  :current_page page
                  :links
                  (pagination/links uri query-string page count
                                    common/per-page)}}]
      (inertia/render :users props))))

(defn edit-user-get
  [{:keys [postgres]}]
  (fn [request]
    (let [user-id (get-in request [:path-params :user-id])
          user (model/get-user-by-id postgres user-id)]
      (inertia/render :edit-user {:user user}))))

(defn check-passwords-match
  [{:keys [password password2]}]
  (= password password2))

(defn sanitize-create-user
  [user]
  (dissoc user :password2))

(defn edit-user-post
  [{:keys [postgres]}]
  (fn [request]
    (let [user-id (get-in request [:path-params :user-id])
          user (assoc (:body-params request) :id user-id)]
      (if (check-passwords-match user)
        (do
          (model/update-user! postgres (sanitize-create-user user))
          (response/redirect (routes/get-route-path :users) :see-other))
        (-> (response/redirect (routes/get-route-path :edit-user {:user-id user-id}))
            (assoc :flash
                   {:error
                    {:password "Passwords don't match"}}))))))

(defn create-user-post
  [{:keys [postgres]}]
  (fn [request]
    (let [user (:body-params request)]
      (if (check-passwords-match user)
        (do
          (model/create-user! postgres (sanitize-create-user user))
          (response/redirect (routes/get-route-path :users) :see-other))
        (-> (response/redirect (routes/get-route-path :create-user))
            (assoc :flash
                   {:error
                    {:password "Passwords don't match"}}))))))

(defn delete-user
  [{:keys [postgres]}]
  (fn [request]
    (let [user (common/get-user request)
          user-id (get-in request [:path-params :user-id])]
      (model/hard-delete-user! postgres user-id)
      (response/redirect (routes/get-route-path :users) :see-other))))

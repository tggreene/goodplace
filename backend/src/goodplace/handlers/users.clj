(ns goodplace.handlers.users
  (:require [goodplace.handlers.common :as common]
            [goodplace.models.users :as model]
            [goodplace.shared.routes :as routes]
            [goodplace.utils.pagination :as pagination]
            [goodplace.utils.schema :as schema]
            [inertia.middleware :as inertia]
            [ring.util.response :as response]
            [goodplace.utils.coerce :as coerce]
            [malli.util :as mu]))

(def user-schema
  [:and
   [:map
    [:first_name schema/non-empty-string]
    [:last_name schema/non-empty-string]
    [:email schema/email-address]
    [:password [:string {:min 8}]]
    [:password2 [:string {:min 8}]]
    [:role [:enum "admin" "user"]]]
   [:fn {:error/message "Passwords don't match"
         :error/path [:password2]}
    (fn [{:keys [password password2]}]
      (= password password2))]])

(def update-user-schema
  (mu/merge
   user-schema
   [:map
    [:id :uuid]
    [:password {:optional true} [:string {:min 8}]]
    [:password2 {:optional true} [:string {:min 8}]]]))

(def validate-user
  (schema/make-validator user-schema))

(def validate-update-user
  (schema/make-validator update-user-schema))

(comment
  (validate-user
   {:email "tim.g.greene@gmail.com",
    :first_name "Tim",
    :last_name "Greene",
    :password "Password1!",
    :password2 "Passwo",
    :role "user"})

  )

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

(defn sanitize-user
  [user]
  (dissoc user :password2))

(defn edit-user-post
  [{:keys [postgres]}]
  (fn [request]
    (let [user-id (get-in request [:path-params :user-id])
          user (assoc (:body-params request) :id (coerce/to-uuid user-id))
          errors (validate-update-user user)]
      (if (empty? errors)
        (do
          (model/update-user! postgres (sanitize-user user))
          (response/redirect (routes/get-route-path :users) :see-other))
        (-> (response/redirect (routes/get-route-path :edit-user {:user-id user-id}))
            (assoc :flash {:error errors}))))))

(defn create-user-post
  [{:keys [postgres]}]
  (fn [request]
    (let [user (:body-params request)
          errors (validate-user user)]
      (if (empty? errors)
        (do
          (model/create-user! postgres (sanitize-user user))
          (response/redirect (routes/get-route-path :users) :see-other))
        (-> (response/redirect (routes/get-route-path :create-user))
            (assoc :flash {:error errors}))))))

(defn delete-user
  [{:keys [postgres]}]
  (fn [request]
    (let [user (common/get-user request)
          user-id (get-in request [:path-params :user-id])]
      (model/hard-delete-user! postgres user-id)
      (response/redirect (routes/get-route-path :users) :see-other))))

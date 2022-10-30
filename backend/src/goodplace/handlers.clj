(ns goodplace.handlers
  (:require [crypto.password.bcrypt :as password]
            [inertia.middleware :as inertia]
            [goodplace.models.users :as users]
            [ring.util.response :as response]
            [goodplace.shared.routes :as routes]
            [goodplace.models.notes :as notes]))


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

(defn notes
  [{:keys [db]}]
  (fn [request]
    (let [{:keys [id] :as user} (get-user request)
          notes #p (notes/list-user-notes db id)]
      (inertia/render :notes {:notes notes}))))

(defn create-note
  [])

(defn delete-note
  [])

(ns goodplace.handlers.auth
  (:require [crypto.password.bcrypt :as password]
            [inertia.middleware :as inertia]
            [goodplace.models.users :as users]
            [ring.util.response :as response]
            [goodplace.shared.routes :as routes]
            [goodplace.handlers.common :as common]))

(defn authenticate
  "Check request username and password against authdata
  username and passwords."
  [{:keys [postgres]}]
  (fn [request]
    (let [email (-> request :body-params :email)
          password (-> request :body-params :password)
          user (users/get-user-by-email postgres email)
          sanitized-user (dissoc user
                                 :password
                                 :created_at
                                 :updated_at
                                 :deleted_at)
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
  (if (common/authenticated? request)
    (response/redirect (routes/get-route-path :home) :see-other)
    (inertia/render :login)))

(defn logout
  [_]
  (-> (response/redirect (routes/get-route-path :home) :see-other)
      (assoc :session nil)))

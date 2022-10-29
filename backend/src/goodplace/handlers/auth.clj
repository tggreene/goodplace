(ns goodplace.handlers.auth
  (:require [crypto.password.bcrypt :as password]
            [inertia.middleware :as inertia]
            [goodplace.models.users :as users]
            [ring.util.response :as response]))

(defn authenticate
  "Check request username and password against authdata
  username and passwords."
  [db]
  (fn [request]
    #p request
    (let [email (-> request :body-params :email)
          password (-> request :body-params :password)
          user (users/get-user-by-email db email)
          sanitized-user #p (dissoc user :password)
          session (:session request)]
      (if (and user (password/check password (:password user)))
        (let [updated-session (assoc session :identity sanitized-user)]
          (-> (response/redirect "/")
              (assoc :session updated-session)))
        (-> (response/redirect "/login")
            (assoc :flash
                   {:error
                    {:email "These credentials do not match our records."}}))))))

(defn logout
  [_]
  (-> (response/redirect "/" :see-other)
      (assoc :session nil)))

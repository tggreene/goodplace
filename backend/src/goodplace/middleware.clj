(ns goodplace.middleware
  (:require [buddy.auth]
            [ring.util.response :as response]
            [goodplace.handlers.common :as common]
            [goodplace.shared.routes :as routes]
            [goodplace.models.users :as users]))

(defn get-session
  [request]
  (:session request))

;; Authentication is redundant when all authenticated users have authorization
;; priviledges but may still be a meaningful distinction for api requests

(defn either-route-not-authn-or-authn-present?
  [route request]
  (or (not (:authenticated? route))
      (and (:authenticated? route)
           (buddy.auth/authenticated? request))))

(defn wrap-authentication-redirect
  [handler]
  (fn [request]
    (let [uri (:uri request)
          route (routes/get-route-by-path uri)]
      (if (either-route-not-authn-or-authn-present? route request)
        (handler request)
        (response/redirect (routes/get-route-path :login))))))

(defn wrap-authorization
  "If a user lacks the required role for a route then redirect them back to
  their home"
  [handler]
  (fn [{:keys [uri] :as request}]
    (let [user (common/get-user request)
          route (routes/get-route-by-path uri)
          route-roles (:roles route)]
      (if (or (not route-roles)
              (and route-roles
                   (contains? route-roles (:role user))))
        (handler request)
        (response/redirect (routes/get-route-path (routes/user-home user)))))))

(defn wrap-inertia-session
  "If we have a user-id we should have the user but if something else goes wrong
  in between we don't want to leave users high and dry being unable to interact
  with the app, so logout if we don't have a user record"
  [handler {:keys [postgres] :as context}]
  (fn [request]
    (let [user-id (-> request :session :identity :id)
          user (users/get-user-by-id postgres user-id)
          success (-> request :flash :success)
          errors (-> request :flash :error)
          props {:errors (or errors {})
                 :auth {:user user}
                 :flash {:success success
                         :error nil}}]
      (if (and user-id (empty? user))
        (-> (response/redirect (routes/get-route-path :home) :see-other)
            (assoc :session nil))
        (handler (assoc request :inertia-share props))))))

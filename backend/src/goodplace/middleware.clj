(ns goodplace.middleware
  (:require [buddy.auth]
            [ring.util.response :as response]
            [goodplace.shared.routes :as routes]))

(defn wrap-auth
  [handler]
  (fn [request]
    (let [uri (:uri request)
          route (routes/get-route-by-path uri)]
      (if (or (not (:authenticated? route))
              (and (:authenticated? route)
                   (buddy.auth/authenticated? request)))
        (handler request)
        (response/redirect (routes/get-route-path :login))))))

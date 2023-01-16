(ns goodplace.handlers.common)

(def per-page 10)

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

(defn sanitized-user
  "We may need some of those dates back at some point"
  [user]
  (dissoc user
          :password
          :created_at
          :updated_at
          :deleted_at))

(defn session-set-identity
  [response user]
  (assoc-in response [:session :identity] (sanitized-user user)))

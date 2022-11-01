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

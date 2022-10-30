(ns goodplace.utils.inertia)

(defn wrap-inertia-share
  [handler db]
  (fn [request]
    (let [user-id (-> request :session :identity :id)
          user nil #_ (users/get-user-by-id db user-id)
          success (-> request :flash :success)
          errors (-> request :flash :error)
          props {:errors (or errors {})
                 :auth {:user user}
                 :flash {:success success
                         :error nil}}]
      (handler (assoc request :inertia-share props)))))

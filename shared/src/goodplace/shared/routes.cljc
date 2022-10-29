(ns goodplace.shared.routes)

(defn -index-by
  [f col]
  (reduce (fn [res item]
            (assoc res (f item) item))
          {}
          col))

(def routes
  [{:id :home
    :path "/"
    :name "Home"
    :title "GoodPlace"
    :page? true}
   {:id :about
    :path "/about"
    :name "About"
    :page? true}
   {:id :login
    :path "/login"
    :name "Login"
    :title "Sign In"
    :page? true}
   {:id :logout
    :path "/logout"
    :name "Logout"
    :title "Logout"}
   {:id :login-post
    :path "/api/login"
    :name "Login Post"}
   {:id :authenticate
    :path "/authenticate"
    :name "Authenticate"}])

(def pages
  (filter :page? routes))

(def indexed-routes
  (-index-by :id routes))

(def indexed-pages
  (-index-by :id pages))

(defn get-route
  [id]
  (get indexed-routes id))

(def get-route-path
  (comp :path get-route))

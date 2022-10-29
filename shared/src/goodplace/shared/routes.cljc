(ns goodplace.shared.routes)

(def routes
  [{:id :home
    :path "/"
    :name "Home"
    :page? true}
   {:id :about
    :path "/about"
    :name "About"
    :page? true}
   {:id :login
    :path "/login"
    :name "Login"
    :page? true}
   {:id :bodmin
    :path "/bidmin"
    :name "Bidmin"
    :page? true}
   {:id :login-post
    :path "/api/login"
    :name "Login Post"}])

(def pages
  (filter :page? routes))

(defn get-path-by-id
  [id]
  (:path (get routes id)))

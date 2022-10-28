(ns goodplace.shared.routes)

(def routes
  [{:id :home
    :path "/"
    :name "Home"
    :page? true}
   {:id :about
    :path "/about"
    :name "About"
    :page? true}])

(def pages
  (filter :page? routes))

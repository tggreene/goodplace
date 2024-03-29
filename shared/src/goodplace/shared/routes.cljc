(ns goodplace.shared.routes
  (:require [clojure.string :as str]))

;; {:id :home          - id used to refer to route
;;  :path "/"          - url path for resource
;;  :name "Home"       - Name of route used as a referent in the App
;;  :title "GoodPlace" - Title of route (or page typically) for display in app
;;  :page? true        - Whether route represents a page which should have a component
;;  :nav? true         - Whether a page should show in app nav
;;  }

(def routes
  [{:id :home
    :path "/"
    :name "Home"
    :title "GoodPlace"
    :page? true
    :nav? true}

   {:id :something-wrong
    :path "/oops"
    :name "Something Wrong"
    :title "Something Went Wrong"
    :page? true}

   {:id :login
    :path "/login"
    :name "Login"
    :title "Login"
    :page? true}
   {:id :logout
    :path "/logout"
    :name "Logout"
    :title "Logout"}

   ;; Users
   {:id :users
    :path "/users"
    :name "Users"
    :title "Users"
    :page? true
    :nav? true
    :roles #{"admin"}}

   {:id :edit-user
    :path "/users/:user-id/edit"
    :name "Edit User"
    :title "Edit User"
    :page? true
    :roles #{"admin"}}

   {:id :create-user
    :path "/create-user"
    :name "Create User"
    :title "Create User"
    :page? true
    :roles #{"admin"}}

   {:id :delete-user
    :path "/users/:user-id/delete"
    :name "Delete User"
    :title "Delete User"
    :roles #{"admin"}}

   ;; Notes
   {:id :notes
    :path "/notes"
    :name "Notes"
    :title "Notes"
    :page? true
    :nav? true
    :roles #{"user" "admin"}}

   {:id :view-note
    :path "/notes/:note-id"
    :name "View Note"
    :title "View Note"
    :page? true
    :roles #{"user" "admin"}}

   {:id :edit-note
    :path "/notes/:note-id/edit"
    :name "Edit Note"
    :title "Edit Note"
    :page? true
    :roles #{"user" "admin"}}

   {:id :create-note
    :path "/create-note"
    :name "Create Note"
    :title "Create Note"
    :page? true
    :roles #{"user" "admin"}}

   {:id :delete-note
    :path "/notes/:note-id/delete"
    :name "Delete Note"
    :title "Delete Note"
    :roles #{"user" "admin"}}

   ;; Cities
   {:id :cities
    :path "/cities"
    :name "Cities"
    :title "Cities"
    :page? true
    :nav? true
    :roles #{"user" "admin"}}])

(def landing-page :home)

(defn user-home
  [{:keys [role] :as user}]
  (case role
    "admin" :home
    "user" :home
    landing-page))

(defn -index-by
  [f col]
  (reduce (fn [res item]
            (assoc res (f item) item))
          {}
          col))

(defn -find-first
  [pred col]
  (first (filter pred col)))

(def pages
  (filter :page? routes))

(def indexed-routes
  (-index-by :id routes))

(def indexed-pages
  (-index-by :id pages))

(defn get-route
  ([id]
   (get indexed-routes id))
  ([id params]
   (some-> indexed-routes
           (get id)
           (update :path #(reduce (fn [path [param value]]
                                    (str/replace path (str param) value))
                                  %
                                  params)))))

(defn get-route-path
  ([id]
   (:path (get-route id)))
  ([id params]
   (:path (get-route id params))))

(defn get-route-by-path
  [path]
  (-find-first #(= path (:path %)) routes))

(defn check-routes
  [routes]
  (let [ids (map :id routes)
        unique? (= (count ids) (count (set ids)))]
    {:unique? unique?}))

(defn filter-pages
  [{{:keys [role]} :user} pages]
  (->> pages
       (filter :nav?)
       (filter #(or (empty? (:roles %))
                    (contains? (:roles %) role)))))

(comment
  ;; Check ids are unique
  (check-routes routes)

  )

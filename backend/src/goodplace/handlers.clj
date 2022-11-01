(ns goodplace.handlers
  (:require [goodplace.handlers.auth :as auth]
            [goodplace.handlers.cities :as cities]
            [goodplace.handlers.notes :as notes]
            [goodplace.handlers.users :as users]
            [inertia.middleware :as inertia]))

(defn inertia-handler
  ([id]
   (fn [_]
     (inertia/render id)))
  ([id props]
   (fn [_]
     (inertia/render id props))))

(defn handlers
  [context]
  {:home {:get {:handler (inertia-handler :home)}}
   :login {:get {:handler auth/login}
           :post {:handler (auth/authenticate context)}}
   :logout {:get {:handler auth/logout}}
   :users {:get {:handler (users/list-users context)}}
   :edit-user {:get {:handler (users/edit-user-get context)}
               :post {:handler (users/edit-user-post context)}}
   :create-user {:get {:handler (inertia-handler :create-user)}
                 :post {:handler (users/create-user-post context)}}
   :delete-user {:delete {:handler (users/delete-user context)}}
   :notes {:get {:handler (notes/list-notes context)}}
   :view-note {:get {:handler (notes/view-note context)}}
   :edit-note {:get {:handler (notes/edit-note-get context)}
               :post {:handler (notes/edit-note-post context)}}
   :create-note {:get {:handler (inertia-handler :create-note)}
                 :post {:handler (notes/create-note-post context)}}
   :delete-note {:delete {:handler (notes/delete-note context)}}
   :cities {:get {:handler (cities/list-cities context)}}
   :something-wrong {:get {:handler (inertia-handler :something-wrong)}}})

(ns goodplace.pages.users
  (:require
   ["@chakra-ui/react"
    :refer
    [Box Flex Heading Button Container Text Input Table Thead
     Tbody Tfoot Tr Th Td TabelCaption TableContainer
     Stack HStack VStack useToast FormControl FormLabel Textarea
     CircularProgress]]
   ["@inertiajs/inertia-react" :refer [Head InertiaLink usePage useForm]]
   [helix.core :refer [defnc $ <>]]
   [helix.hooks :as hooks]
   [goodplace.pages.common :refer [PageTemplate]]
   [goodplace.shared.routes :as routes]
   [applied-science.js-interop :as j]
   [clojure.pprint :refer [pprint]]
   [tggreene.inertia-cljs :as inertia-cljs]))

(defnc UsersTable
  [{:keys [users]}]
  (let [{:keys [data links current_page]} users]
    ($ VStack {:gap 4}
       ($ Box {:width "100%"
               :px 6}
          ($ InertiaLink {:href (routes/get-route-path :create-user)}
             ($ Button {:colorScheme "blue"} "Create User")))
       ($ TableContainer
          ($ Table {:variant "simple"}
             ($ Thead
                ($ Tr
                   ($ Th {:minWidth "xs"} "Name")
                   ($ Th {:minWidth "xs"} "Email")
                   ($ Th "Created")
                   ($ Th "Actions")))
             ($ Tbody
                (for [user data
                      :let [{:keys [id first_name last_name email created_at]}
                            (j/lookup user)
                            name (str first_name " " last_name)]]
                  ($ Tr {:key email}
                     ($ Td name)
                     ($ Td email)
                     ($ Td created_at)
                     ($ Td
                        ($ HStack
                           ($ InertiaLink {:as "span"
                                           :href (routes/get-route-path :edit-user {:user-id id})}
                              ($ Button {:colorScheme "blue"
                                         :size "sm"}
                                 "Edit User"))
                           ($ InertiaLink {:as "span"
                                           :href (routes/get-route-path :delete-user {:user-id id})
                                           :method "delete"}
                              ($ Button {:colorScheme "red"
                                         :size "sm"}
                                 "Delete User")))))))))
       ($ HStack
          (for [link links
                :let [{:keys [url label active]} (j/lookup link)]]
            ($ InertiaLink {:key (str url label)
                            :href url}
               (let [props (cond-> {:minWidth 14}
                             active (merge {:colorScheme "blue"})
                             (not url) (merge {:disabled true}))]
                 ($ Button {:& props} label))))))))

(defnc Users
  []
  (let [users (-> (usePage)
                   (j/get-in [:props :users])
                   (j/lookup))]
    ($ PageTemplate {:title "Users"}
       ($ UsersTable {:users users}))))

(defnc UserForm
  [{:keys [data setData onSubmit errors]}]
  ($ "form" {:onSubmit onSubmit}
     ($ Flex {:direction "column"
              :gap 4
              :width "2xl"
              :p 4}
        ($ FormControl
           ($ FormLabel "First Name")
           ($ Input {:type "text"
                     :value (:first_name data)
                     :onChange #(setData :first_name (.. % -target -value))}))
        ($ FormControl
           ($ FormLabel "Last Name")
           ($ Input {:type "text"
                     :value (:last_name data)
                     :onChange #(setData :last_name (.. % -target -value))}))
        ($ FormControl
           ($ FormLabel "Username")
           ($ Input {:type "text"
                     :value (:username data)
                     :onChange #(setData :username (.. % -target -value))}))
        ($ FormControl
           ($ FormLabel "Email")
           ($ Input {:type "text"
                     :value (:email data)
                     :onChange #(setData :email (.. % -target -value))}))
        ($ FormControl
           ($ FormLabel "Password")
           ($ Input {:type "password"
                     :value (:password data)
                     :onChange #(setData :password (.. % -target -value))}))
        ($ FormControl
           ($ FormLabel "Re-type Password")
           ($ Input {:type "password"
                     :value (:password2 data)
                     :onChange #(setData :password2 (.. % -target -value))}))
        (when (not-empty errors)
          ($ Box {:p 4
                  :bg "red.50"
                  :borderRadius 6
                  :minWidth "md"}
             (for [error (vals errors)]
               ($ Text {:color "red.500"} error))))
        ($ HStack {:mt 4}
           ($ InertiaLink {:href (routes/get-route-path :users)}
              ($ Button "Back To Users"))
           ($ Button {:type "submit"
                      :colorScheme "blue"}
              "Submit")))) )

(defnc CreateUser
  []
  (let [{:keys [data setData errors post processing]}
        (inertia-cljs/use-form {:first_name ""
                                :last_name ""
                                :username ""
                                :email ""
                                :password ""
                                :password2 ""})]
    ($ PageTemplate {:title "Create User"}
       ($ UserForm {:data data
                    :setData setData
                    :onSubmit #(do (.preventDefault %)
                                   (post (routes/get-route-path :create-user)))
                    :errors errors}))))

(defnc EditUser
  []
  (let [{:keys [id first_name last_name username email password]}
        (get-in (inertia-cljs/use-page) [:props :user])
        {:keys [data setData errors post processing]}
        (inertia-cljs/use-form {:first_name first_name
                                :last_name last_name
                                :username username
                                :email email
                                :password ""
                                :password2 ""})]
    ($ PageTemplate {:title "Edit User"}
       ($ UserForm {:data data
                    :setData setData
                    :onSubmit #(do (.preventDefault %)
                                   (post (routes/get-route-path :edit-user {:user-id id})))
                    :errors errors}))))

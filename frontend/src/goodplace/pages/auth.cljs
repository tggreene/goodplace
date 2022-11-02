(ns goodplace.pages.auth
  (:require
   ["@chakra-ui/react"
    :refer
    [Box Flex Button Container Text Input FormControl FormLabel]]
   [helix.core :refer [defnc $]]
   [goodplace.pages.common :refer [PageTemplate]]
   [goodplace.shared.routes :as routes]
   [tggreene.inertia-cljs :as inertia-cljs]))

(defnc Login
  []
  (let [{:keys [data setData errors post processing]}
        (inertia-cljs/use-form {:email ""
                                :password ""})]
    ($ PageTemplate
       {:title "Login"}
       ($ "form" {:on-submit
                  #(do (.preventDefault %)
                       (post (routes/get-route-path :login)))}
          ($ Flex {:direction "column"
                   :alignItems "center"
                   :p 2
                   :gap 2
                   :width #js ["xs" "md"]}
             ($ FormControl
                ($ FormLabel "Email")
                ($ Input {:type "text"
                          :placeholder "Email"
                          :value (:email data)
                          :onChange #(setData :email (.. % -target -value))
                          :maxWidth #js ["80%" "initial"]}))
             ($ FormControl
                ($ FormLabel "Password")
                ($ Input {:type "password"
                          :placeholder "Password"
                          :value (:password data)
                          :onChange #(setData :password (.. % -target -value))
                          :maxWidth #js ["80%" "initial"]}))
             ($ Button {:type "submit"
                        :colorScheme "blue"} "Submit")
             (when (not-empty errors)
               ($ Box {:p 4
                       :bg "red.50"
                       :borderRadius 6}
                  (for [error (vals errors)]
                    ($ Text {:color "red.500"} error))))))
       ($ Container {:fontStyle "italic"}
          "Psst, try example@example.com with password as a password."))))

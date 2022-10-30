(ns goodplace.pages
  (:require
   ["@chakra-ui/react" :refer [Box Flex Heading Button Container Text Input Table Thead
                               Tbody Tfoot Tr Th Td TabelCaption TableContainer
                               Stack HStack VStack useToast FormControl FormLabel Textarea
                               CircularProgress]]
   ["@inertiajs/inertia-react" :refer [Head InertiaLink usePage useForm]]
   [helix.core :refer [defnc $ <>]]
   [helix.hooks :as hooks]
   [goodplace.shared.routes :as routes]
   [applied-science.js-interop :as j]
   [clojure.pprint :refer [pprint]]))

(defnc JsObjectBlock
  [{:keys [object]}]
  ($ Text {:maxWidth "md"
           :whiteSpace "pre-wrap"
           :as "pre"}
     (-> object
         (js->clj :keywordize-keys true)
         pprint
         with-out-str)))

(defnc PageTemplate
  [{:keys [title children]}]
  (let [page (usePage)]
    ($ Flex {:direction "column"
             :justify "center"
             :align "center"
             :width "100%"
             :gap 4}
       ($ Box {:p 4}
          ($ Heading {:size "2xl"} title))
       children
       #_
       ($ Box {:mt 10}
          ($ JsObjectBlock {:object page})) )))

(defnc CircularProgressTimed
  [{:keys [time-ms]}]
  (let [[progress setProgress] (hooks/use-state 0)]
    (hooks/use-effect
     :once
     (let [interval-ms 100
           interval (atom nil)]
       (reset! interval
               (js/setInterval
                (fn []
                  (setProgress
                   #(if (< % 100)
                      (+ (* (/ 100 time-ms) interval-ms) %)
                      (do (js/clearInterval @interval)
                          100))))
                interval-ms))))
    ($ CircularProgress
       {:value progress})))

(defnc SomethingWrong
  []
  (let [page (usePage)
        errors (js->clj (j/get-in page [:props :errors]))
        redirect (j/get-in page [:props :redirect])
        toast (useToast)
        redirect-ms 3500]
    (hooks/use-effect
     :once
     (when (not-empty errors)
       (doseq [error errors]
         (toast #js {:title error
                     :status "error"
                     :position "top"
                     :duration 3000
                     :isClosable true}))))
    (hooks/use-effect
     :once
     (when redirect
       (js/setTimeout
        (fn []
          (js/window.location.replace redirect))
        redirect-ms)))
    ($ PageTemplate
       {:title "Something Went Wrong"}
       ($ Container
          "Don't worry we'll soon sort you out")
       ($ CircularProgressTimed {:time-ms redirect-ms}))))

(defnc Home
  []
  (let [page (usePage)
        errors (js->clj (j/get-in page [:props :errors]))
        redirect (j/get-in page [:props :redirect])
        toast (useToast)]
    (hooks/use-effect
     :once
     (when (not-empty errors)
       (doseq [error errors]
         (toast #js {:title error
                     :status "error"
                     :position "top"
                     :duration 3000
                     :isClosable true}))))
    (hooks/use-effect
     :once
     (when redirect
       (js/setTimeout
        (fn []
          (js/window.location.replace redirect))
        3500)))
    ($ PageTemplate
       {:title "Home"}
       ($ Container
          "GoodPlace is about getting a good place to start a product or tool with
        a relatively simple architecture and good enough performance. There are
        tons of tradeoffs within all of which will shed some people's interest,
        but that's okay. I think there's enough in here to remain interesting
        and I've been delighted by how easy it can be to add routes, pages and
        new functionality"))))

(defnc Login
  []
  (let [{:keys [data setData errors post processing]}
        (j/lookup (useForm #js {:email ""
                                :password ""}))
        errors (js->clj errors)]
    ($ PageTemplate
       {:title "Login"}
       ($ "form" {:on-submit
                  #(do (.preventDefault %)
                       (post (routes/get-route-path :login)))}
          ($ Flex {:direction "column"
                   :p 2
                   :gap 2
                   :width "md"}
             ($ Input {:type "text"
                       :placeholder "Email"
                       :value (.-email data)
                       :onChange #(setData "email" (.. % -target -value))})
             ($ Input {:type "password"
                       :placeholder "Password"
                       :value (.-password data)
                       :onChange #(setData "password" (.. % -target -value))})
             ($ Button {:type "submit"
                        :colorScheme "blue"} "Submit")
             (when (not-empty errors)
               ($ Box {:p 4
                       :bg "red.50"
                       :borderRadius 6}
                  (for [error (vals errors)]
                    ($ Text {:color "red.500"} error)))))))))

(defnc About
  []
  ($ PageTemplate {:title "About"}
     ))

(defn truncate-ellipsis
  ([s]
   (truncate-ellipsis s 200))
  ([s max]
   (let [length (count s)]
     (if (< length max)
       s
       (str (subs s 0 max) "...")))))

(defnc Note
  [note]
  (let [{:keys [id title contents]} note]
    ($ InertiaLink {:key id
                    :href (routes/get-route-path :view-note {:note-id id})}
       ($ Flex {:direction "column"
                :gap 1
                :width "xl"
                :bg "gray.50"
                :borderRadius 6
                :p 4}
          ($ Text {:fontWeight "bold"} title)
          ($ Text (truncate-ellipsis contents))))))

(defnc Notes
  []
  (let [notes (j/get-in (usePage) [:props :notes])]
    ($ PageTemplate {:title "Notes"}
       (map Note notes)
       ($ HStack {:mt 4}
          ($ InertiaLink {:href (routes/get-route-path :create-note)}
             ($ Button {:colorScheme "blue"}
                "Create Note"))) )))

(defnc ViewNote
  []
  (let [note (j/get-in (usePage) [:props :note])
        {:keys [id title contents]} (j/lookup note)]
    ($ PageTemplate {:title "Note"}
       ($ Flex {:direction "column"
                :gap 1
                :width "2xl"
                :p 4}
          ($ Text {:fontWeight "bold"} title)
          ($ Text contents))
       ($ HStack {:mt 8}
          ($ InertiaLink {:href (routes/get-route-path :notes)}
             ($ Button "Back To Notes"))
          ($ InertiaLink {:href (routes/get-route-path :edit-note {:note-id id})}
             ($ Button {:colorScheme "blue"} "Edit Note"))
          ($ InertiaLink {:as "span"
                          :href (routes/get-route-path :delete-note {:note-id id})
                          :method "delete"}
             ($ Button {:colorScheme "red"} "Delete Note"))))))

(defnc EditNote
  []
  (let [{:keys [id title contents] :as note}
        (j/lookup (j/get-in (usePage) [:props :note]))
        {:keys [data setData errors post processing]}
        (j/lookup (useForm #js {:id id
                                :title title
                                :contents contents}))]
    ($ "form" {:onSubmit
               #(do (.preventDefault %)
                    (post (routes/get-route-path :edit-note {:note-id id})))}
       ($ PageTemplate {:title "Note"}
          ($ Flex {:direction "column"
                   :gap 4
                   :width "2xl"
                   :p 4}
             ($ FormControl
                ($ FormLabel "Title")
                ($ Input {:type "text"
                          :value (.-title data)
                          :onChange #(setData "title" (.. % -target -value))}))
             ($ FormControl
                ($ FormLabel "Contents")
                ($ Textarea {:type "text"
                             :placeholder "contents"
                             :value (.-contents data)
                             :onChange #(setData "contents" (.. % -target -value))
                             :height "md"})))
          ($ HStack {:mt 4}
             ($ InertiaLink {:href (routes/get-route-path :view-note {:note-id id})}
                ($ Button "Back To Note"))
             ($ Button {:type "submit"
                        :colorScheme "blue"}
                "Submit"))))))

(defnc CreateNote
  []
  (let [{:keys [id title contents] :as note}
        (j/lookup (j/get-in (usePage) [:props :note]))
        {:keys [data setData errors post processing]}
        (j/lookup (useForm #js {:title ""
                                :contents ""}))]
    ($ "form" {:onSubmit
               #(do (.preventDefault %)
                    (post (routes/get-route-path :create-note)))}
       ($ PageTemplate {:title "Note"}
          ($ Flex {:direction "column"
                   :gap 4
                   :width "2xl"
                   :p 4}
             ($ FormControl
                ($ FormLabel "Title")
                ($ Input {:type "text"
                          :value (.-title data)
                          :onChange #(setData "title" (.. % -target -value))}))
             ($ FormControl
                ($ FormLabel "Contents")
                ($ Textarea {:type "text"
                             :value (.-contents data)
                             :onChange #(setData "contents" (.. % -target -value))
                             :height "md"})))
          ($ HStack {:mt 4}
             ($ InertiaLink {:href (routes/get-route-path :notes)}
                ($ Button "Back To Notes"))
             ($ Button {:type "submit"
                        :colorScheme "blue"}
                "Submit"))))))

(defnc CitiesTable
  [{:keys [cities]}]
  (let [{:keys [data links current_page]} cities]
    ($ VStack
       ($ TableContainer
          ($ Table {:variant "simple"}
             ($ Thead
                ($ Tr
                   ($ Th {:minWidth "md"} "City")
                   ($ Th "Country")
                   ($ Th "Latitude")
                   ($ Th "Longitude")))
             ($ Tbody
                (for [city data
                      :let [{:keys [name country lat lng]} (j/lookup city)]]
                  ($ Tr {:key (str name "-" lat "-" lng)}
                     ($ Td name)
                     ($ Td country)
                     ($ Td lat)
                     ($ Td lng))))))
       ($ HStack
          (for [link links
                :let [{:keys [url label active]} (j/lookup link)]]
            ($ InertiaLink {:key (str url label)
                            :href url}
               (let [props (cond-> {:minWidth 14}
                             active (merge {:colorScheme "blue"})
                             (not url) (merge {:disabled true}))]
                 ($ Button {:& props} label))))))))

(defnc Cities
  []
  (let [cities (-> (usePage)
                   (j/get-in [:props :cities])
                   (j/lookup))]
    ($ PageTemplate {:title "Cities"}
       ($ CitiesTable {:cities cities}))))

(defnc UsersTable
  [{:keys [users]}]
  (let [{:keys [data links current_page]} users]
    ($ VStack {:gap 4}
       ($ Box {:width "100%"
               :px 6}
          ($ Button {:colorScheme "blue"} "Create User"))
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
                      :let [{:keys [first_name last_name email created_at]}
                            (j/lookup user)
                            name (str first_name " " last_name)]]
                  ($ Tr {:key email}
                     ($ Td name)
                     ($ Td email)
                     ($ Td created_at)
                     ($ Td
                        ($ HStack
                           ($ Button {:colorScheme "blue"
                                      :size "sm"}
                              "Edit User")
                           ($ Button {:colorScheme "red"
                                      :size "sm"}
                              "Delete User"))))))))
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

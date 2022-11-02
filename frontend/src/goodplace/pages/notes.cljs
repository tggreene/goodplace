(ns goodplace.pages.notes
  (:require
   ["@chakra-ui/react"
    :refer
    [Box Flex Heading Button Text Input HStack Toast FormControl FormLabel
     Textarea]]
   ["@inertiajs/inertia-react" :refer [InertiaLink]]
   [helix.core :refer [defnc $ <>]]
   [goodplace.pages.common :refer [PageTemplate]]
   [goodplace.shared.routes :as routes]
   [tggreene.inertia-cljs :as inertia-cljs]))

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
                :minWidth #js {:base "80vw"
                               :lg "lg"}
                :bg "gray.50"
                :borderRadius 6
                :p 4}
          ($ Text {:fontWeight "bold"} title)
          ($ Text (truncate-ellipsis contents))))))

(defnc Notes
  []
  (let [notes (get-in (inertia-cljs/use-page) [:props :notes])]
    ($ PageTemplate {:title "Notes"}
       (map Note notes)
       ($ HStack {:mt 4}
          ($ InertiaLink {:href (routes/get-route-path :create-note)}
             ($ Button {:colorScheme "blue"}
                "Create Note"))) )))

(defnc ViewNote
  []
  (let [{:keys [id title contents] :as note}
        (get-in (inertia-cljs/use-page) [:props :note])]
    ($ PageTemplate {:title "Note"}
       ($ Flex {:direction "column"
                :gap 1
                :width #js {:base "80vw"
                            :lg "2xl"}
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
        (-> (inertia-cljs/use-page)
            (get-in [:props :note]))
        {:keys [data setData errors post processing]}
        (inertia-cljs/use-form {:title title
                                :contents contents})]
    ($ "form" {:onSubmit
               #(do (.preventDefault %)
                    (post (routes/get-route-path :edit-note {:note-id id})))}
       ($ PageTemplate {:title "Note"}
          ($ Flex {:direction "column"
                   :gap 4
                   :width #js {:base "75vw"
                               :lg "lg"}

                   :p 4}
             ($ FormControl
                ($ FormLabel "Title")
                ($ Input {:type "text"
                          :value (:title data)
                          :onChange #(setData :title (.. % -target -value))}))
             ($ FormControl
                ($ FormLabel "Contents")
                ($ Textarea {:type "text"
                             :placeholder "contents"
                             :value (:contents data)
                             :onChange #(setData :contents (.. % -target -value))
                             :height "md"})))
          ($ HStack {:mt 4}
             ($ InertiaLink {:href (routes/get-route-path :view-note {:note-id id})}
                ($ Button "Back To Note"))
             ($ Button {:type "submit"
                        :colorScheme "blue"}
                "Submit"))))))

(defnc CreateNote
  []
  (let [{:keys [data setData errors post processing]}
        (inertia-cljs/use-form {:title ""
                                :contents ""})]
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
                          :value (:title data)
                          :onChange #(setData :title (.. % -target -value))}))
             ($ FormControl
                ($ FormLabel "Contents")
                ($ Textarea {:type "text"
                             :value (:contents data)
                             :onChange #(setData :contents (.. % -target -value))
                             :height "md"})))
          ($ HStack {:mt 4}
             ($ InertiaLink {:href (routes/get-route-path :notes)}
                ($ Button "Back To Notes"))
             ($ Button {:type "submit"
                        :colorScheme "blue"}
                "Submit"))))))

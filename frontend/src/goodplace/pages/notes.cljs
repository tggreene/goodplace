(ns goodplace.pages.notes
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

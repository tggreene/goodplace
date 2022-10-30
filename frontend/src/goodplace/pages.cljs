(ns goodplace.pages
  (:require
   ["@chakra-ui/react" :refer [Box Flex Heading Button Container Text Input Table Thead
                               Tbody Tfoot Tr Th Td TabelCaption TableContainer
                               Stack HStack VStack]]
   ["@inertiajs/inertia-react" :refer [Head InertiaLink usePage useForm]]
   [helix.core :refer [defnc $ <>]]
   [goodplace.shared.routes :as routes]
   [applied-science.js-interop :as j]
   [clojure.pprint :refer [pprint]]))

(defnc JsObjectBlock
  [{:keys [object]}]
  ($ Text {:maxWidth "md"
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
       ($ Box {:mt 10}
          ($ JsObjectBlock {:object page})) )))

(defnc Home
  []
  ($ PageTemplate
     {:title "Home"}
     ($ Box {}
        (let [{:keys [path name]} (routes/get-route :about)]
          ($ InertiaLink {:href path} name)))))

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
                       (post (:path (routes/get-route :authenticate))))}
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
     ($ Container
        "GoodPlace is about getting a good place to start a product or tool with
        a relatively simple architecture and good enough performance. There are
        tons of tradeoffs within all of which will shed some people's interest,
        but that's okay. I think there's enough in here to remain interesting
        and I've been delighted by how easy it can be to add routes, pages and
        new functionality")))

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
    ($ Flex {:key id
             :direction "column"
             :gap 1
             :width "xl"
             :bg "gray.50"
             :borderRadius 6
             :p 4}
       ($ Text {:fontWeight "bold"} title)
       ($ Text (truncate-ellipsis contents)))))

(defnc Notes
  []
  (let [notes (j/get-in (usePage) [:props :notes])]
    ($ PageTemplate {:title "Notes"}
       (map Note notes))))

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

(ns goodplace.pages.cities
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

(defnc CitiesTable
  [{:keys [cities]}]
  (let [{:keys [data links current_page]} cities]
    ($ VStack {:display #js {:base "block"
                             :lg "flex"}
               :width #js {:base "90vw"
                           :lg "100%"}}
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
       ($ HStack {:display "flex"
                  :flexWrap #js ["wrap" nil]}
          (for [link links
                :let [{:keys [url label active]} (j/lookup link)]]
            ($ InertiaLink {:key (str url label)
                            :href url}
               (let [props (cond-> {:minWidth 14
                                    :m 1}
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

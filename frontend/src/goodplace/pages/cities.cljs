(ns goodplace.pages.cities
  (:require
   ["@chakra-ui/react"
    :refer
    [Box Button Table Thead Tbody Tr Th Td TableContainer HStack VStack]]
   ["@inertiajs/inertia-react" :refer [InertiaLink]]
   [helix.core :refer [defnc $ <>]]
   [goodplace.pages.common :refer [PageTemplate]]
   [goodplace.shared.routes :as routes]
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
                   ($ Th {:minWidth #js {:base "initial"
                                         :lg "md"}}
                      "City")
                   ($ Th "Country")
                   ($ Th "Latitude")
                   ($ Th "Longitude")))
             ($ Tbody
                (for [city data
                      :let [{:keys [name country lat lng]} city]]
                  ($ Tr {:key (str name "-" lat "-" lng)}
                     ($ Td name)
                     ($ Td country)
                     ($ Td lat)
                     ($ Td lng))))))
       ($ HStack {:display "flex"
                  :flexWrap #js ["wrap" nil]}
          (for [link links
                :let [{:keys [url label active]} link]]
            ($ InertiaLink {:key (str url label)
                            :href url}
               (let [props (cond-> {:minWidth 14
                                    :m 1}
                             active (merge {:colorScheme "blue"})
                             (not url) (merge {:disabled true}))]
                 ($ Button {:& props} label))))))))

(defnc Cities
  []
  (let [cities (-> (inertia-cljs/use-page)
                   (get-in [:props :cities]))]
    ($ PageTemplate {:title "Cities"}
       ($ CitiesTable {:cities cities}))))

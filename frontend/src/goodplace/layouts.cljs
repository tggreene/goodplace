(ns goodplace.layouts
  (:require
   ["@chakra-ui/react" :refer [Box ChakraProvider Flex Heading Text]]
   ["@inertiajs/inertia-react" :refer [Head InertiaLink usePage]]
   [helix.core :refer [defnc $ <>]]))

(defnc default
  [{:keys [pages children]}]
  (let [pageData (usePage)]
    ($ Flex {:direction "column"
             :justify "center"
             :align "center"
             :width "100%"}
       ($ Flex {:height 12
                :justify "space-between"
                :align "center"
                :width "100%"}
          ($ Box {:py 2
                  :px 4}
             ($ Text "MaybeLogo"))
          ($ Flex {:py 2
                   :gap 4
                   :width "100%"}
             (for [{:keys [id path name]} pages]
               ($ InertiaLink {:key id
                               :href path} name)))
          ($ Box {:py 2
                  :px 4}
             ($ Text "MaybeLoginOooh")))
       ($ Box children))))

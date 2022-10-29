(ns goodplace.pages
  (:require
   ["@chakra-ui/react" :refer [Box ChakraProvider Flex Heading Text]]
   ["@inertiajs/inertia-react" :refer [Head InertiaLink usePage]]
   [helix.core :refer [defnc $ <>]]))

(defnc Home
  []
  (let [page (usePage)]
    ($ Flex {:direction "column"
             :justify "center"
             :align "center"
             :width "100%"}
       ($ Box {:height 12})
       ($ Box {:padding 2}
          ($ Heading {:size "2xl"} "Homeishlyness"))
       ($ Box {}
          ($ Text {:as "pre"} (pr-str page))))))

(defnc Login
  []
  (let []))

(defnc About
  []
  ($ Text "About"))

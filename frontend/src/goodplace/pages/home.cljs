(ns goodplace.pages.home
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
   [goodplace.shared.copy :as copy]
   [goodplace.shared.routes :as routes]
   [applied-science.js-interop :as j]
   [clojure.pprint :refer [pprint]]
   [tggreene.inertia-cljs :as inertia-cljs]))

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
       ($ Container copy/home-blurb))))

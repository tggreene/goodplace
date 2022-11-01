(ns goodplace.pages.common
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
   [applied-science.js-interop :as j]
   [clojure.pprint :refer [pprint]]
   [tggreene.inertia-cljs :as inertia-cljs]))

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
    (<>
     ($ Head {:title title})
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
           ($ JsObjectBlock {:object page})) ))))

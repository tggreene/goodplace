(ns goodplace.pages.common
  (:require
   ["@chakra-ui/react"
    :refer
    [Box Flex Heading Button Container Text Input]]
   ["@inertiajs/inertia-react" :refer [Head]]
   [helix.core :refer [defnc $ <>]]
   [clojure.pprint :refer [pprint]]
   [tggreene.inertia-cljs :as inertia-cljs]))

(defnc DebugBlock
  [{:keys [object]}]
  (let [page (inertia-cljs/use-page)]
    ($ Text {:maxWidth "md"
             :whiteSpace "pre-wrap"
             :as "pre"}
       (-> page
           pprint
           with-out-str))))

(defnc PageTemplate
  [{:keys [title children]}]
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
         ($ DebugBlock)) )))

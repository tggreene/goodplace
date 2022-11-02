(ns goodplace.pages.errors
  (:require
   ["@chakra-ui/react" :refer [Box Container useToast]]
   [helix.core :refer [defnc $ <>]]
   [helix.hooks :as hooks]
   [goodplace.components :refer [CircularProgressTimed]]
   [goodplace.pages.common :refer [PageTemplate]]
   [tggreene.inertia-cljs :as inertia-cljs]))

(defnc SomethingWrong
  []
  (let [page (inertia-cljs/use-page)
        errors (get-in page [:props :errors])
        redirect (get-in page [:props :redirect])
        toast (useToast)
        redirect-ms 3500]
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
        redirect-ms)))
    ($ PageTemplate
       {:title "Something Went Wrong"}
       ($ Container
          "Don't worry we'll soon sort you out")
       ($ CircularProgressTimed {:time-ms redirect-ms}))))

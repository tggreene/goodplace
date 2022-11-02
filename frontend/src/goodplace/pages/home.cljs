(ns goodplace.pages.home
  (:require
   ["@chakra-ui/react" :refer [Container]]
   [helix.core :refer [defnc $]]
   [goodplace.pages.common :refer [PageTemplate]]
   [goodplace.shared.copy :as copy]))

(defnc Home
  []
  ($ PageTemplate
     {:title "Home"}
     ($ Container copy/home-blurb)))

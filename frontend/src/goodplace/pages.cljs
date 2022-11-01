(ns goodplace.pages
  (:require
   ["@chakra-ui/react"
    :refer
    [Box Flex Heading Button Container Text Input Table Thead
     Tbody Tfoot Tr Th Td TabelCaption TableContainer
     Stack HStack VStack useToast FormControl FormLabel Textarea
     CircularProgress]]
   ["@inertiajs/inertia-react" :refer [Head InertiaLink usePage useForm]]
   [goodplace.pages.auth :as auth]
   [goodplace.pages.cities :as cities]
   [goodplace.pages.errors :as errors]
   [goodplace.pages.home :as home]
   [goodplace.pages.notes :as notes]
   [goodplace.pages.users :as users]
   [goodplace.shared.routes :as routes]
   [helix.core :refer [defnc $ <>]]
   [helix.hooks :as hooks]
   [applied-science.js-interop :as j]
   [clojure.pprint :refer [pprint]]
   [tggreene.inertia-cljs :as inertia-cljs]))

(def page-components
  {:home home/Home
   :login auth/Login
   :users users/Users
   :edit-user users/EditUser
   :create-user users/CreateUser
   :notes notes/Notes
   :view-note notes/ViewNote
   :edit-note notes/EditNote
   :create-note notes/CreateNote
   :cities cities/Cities
   :something-wrong errors/SomethingWrong})

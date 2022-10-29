(ns goodplace.layouts
  (:require
   ["@chakra-ui/react" :refer [Box Button ChakraProvider Flex Heading Text
                               Menu MenuButton MenuList MenuItem]]
   ["@chakra-ui/icons" :refer [ChevronDownIcon]]
   ["@inertiajs/inertia-react" :refer [Head InertiaLink Link usePage]]
   [helix.core :refer [defnc $ <>]]
   [goodplace.shared.routes :as routes]
   [applied-science.js-interop :as j]))

(defnc UserMenu
  [{:keys [user] :as props}]
  (let [{:keys [first_name last_name]} (j/lookup user)
        name (str first_name " " last_name)]
    ($ Menu
       ($ MenuButton {:as Button
                      :rightIcon ($ ChevronDownIcon)} name)
       ($ MenuList
          (let [{:keys [name path]} (routes/get-route :logout)]
            ($ InertiaLink {:href path}
               ($ MenuItem name)))))))

(defnc Default
  [{:keys [pages children]}]
  (let [pageData (usePage)
        user (j/get-in pageData [:props :auth :user])]
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
             (if user
               ($ UserMenu {:user user})
               (let [{:keys [path name]} (routes/get-route :login)]
                 ($ Link {:href path}
                    ($ Button name))))))
       ($ Box {:py 8
               :px 8} children))))

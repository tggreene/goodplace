(ns goodplace.layouts
  (:require
   ["@chakra-ui/react" :refer [Box Button ChakraProvider Flex Heading Text
                               Menu MenuButton MenuList MenuItem IconButton
                               Slide Link Image]]
   ["@chakra-ui/icons" :refer [ChevronDownIcon HamburgerIcon]]
   ["@inertiajs/inertia-react" :refer [Head InertiaLink usePage]]
   [helix.core :refer [defnc $ <>]]
   [helix.hooks :as hooks]
   [goodplace.shared.routes :as routes]
   [applied-science.js-interop :as j]
   [react :as react]))

(def top-bar-height 14)
(def top-bar-height-var "var(--chakra-sizes-14)")

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

(defnc TopBar
  [{:keys [user setMenuOpen menuOpen]}]
  ($ Flex {:height top-bar-height
           :justify "space-between"
           :align "center"
           :width "100%"
           :borderBottomWidth 1
           :borderBottomColor "gray.100"
           :borderBottomStyle "solid"
           :zIndex 1}
     ($ Box {:px 2
             :minWidth "xs"}
        ($ IconButton {:icon ($ HamburgerIcon)
                       :onClick #(setMenuOpen (not menuOpen))}))
     ($ Box {:px 4}
        ($ Image {:src "/images/goodplace_small.png"
                  :height 6}))
     ($ Flex {:px 2
              :minWidth "xs"
              :justifyContent "flex-end"}
        (if user
          ($ UserMenu {:user user})
          (let [{:keys [path name]} (routes/get-route :login)]
            ($ InertiaLink {:href path}
               ($ Button name)))))) )

(defn filter-pages
  [{:keys [user]} pages]
  (cond->> pages
    user (remove #(= :login (:id %)))
    (not user) (remove :authenticated?)))

#_
(defnc NavigationMenu
  [{:keys [user pages menuOpen]}]
  (let [pages (filter-pages {:user user} pages)]
    ($ Slide {:direction "left"
              :in menuOpen
              :style #js {:left 0
                          :top "var(--chakra-sizes-12)"
                          :height "100%"
                          :position "absolute"}}
       ($ Box {:height "max(100%, 100vh)"
               :width "xs"
               :borderRightWidth 1
               :borderRightColor "gray.100"
               :borderRightStyle "solid"
               :boxShadow "xs"
               :position "relative"
               :bg "white"}
          ($ Flex {:direction "column"
                   :py 6
                   :px 8
                   :gap 4
                   :position "relative"}
             (for [{:keys [id path name]} #c pages]
               ($ InertiaLink {:as "span"
                               :key id
                               :href path}
                  ($ Link name))))))))

(defnc NavigationMenu
  [{:keys [user pages menuOpen]}]
  (let [pages (filter-pages {:user user} pages)]
    ($ Slide {:direction "left"
              :in menuOpen
              :style #js {:left 0
                          :top top-bar-height-var
                          :height "100%"
                          :position "absolute"}}
       ($ Flex {:height "max(100%, 100vh)"
                :width "xs"
                :borderRightWidth 1
                :borderRightColor "gray.100"
                :borderRightStyle "solid"
                :boxShadow "xs"
                :bg "white"
                :direction "column"
                :py 6
                :px 8
                :gap 4
                :zIndex 10}
          (for [{:keys [id path name]} #c pages]
            ($ InertiaLink {:as "span"
                            :key id
                            :href path}
               ($ Link name)))))))

(defnc Default
  [{:keys [pages children]}]
  (let [pageData (usePage)
        [menuOpen setMenuOpen] (hooks/use-state false)
        user (j/get-in pageData [:props :auth :user])]
    ($ Flex {:direction "column"
             :justify "center"
             :align "center"
             :width "100%"}
       ($ TopBar {:user user
                  :setMenuOpen setMenuOpen
                  :menuOpen menuOpen})
       ($ Flex {:direction "row"}
          ($ NavigationMenu {:user user
                             :pages pages
                             :menuOpen menuOpen})
          ($ Box {:py 8
                  :px 8
                  :width "100%"}
             children)))))

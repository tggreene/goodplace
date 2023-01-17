(ns goodplace.layouts
  (:require
   ["@chakra-ui/react"
    :refer
    [Box Button ChakraProvider Flex Heading Text
     Menu MenuButton MenuList MenuItem IconButton
     Slide Link Image Spacer]]
   ["@chakra-ui/icons" :refer [ChevronDownIcon HamburgerIcon]]
   ["@inertiajs/inertia-react"
    :refer
    [Head InertiaLink usePage useRemember]]
   ["@rehooks/local-storage" :refer [writeStorage useLocalStorage]
    :as localStorage]
   ["react-use" :refer [useMedia]]
   [helix.core :refer [defnc $ <>]]
   [helix.hooks :as hooks]
   [goodplace.components :refer [ResponsiveIndicator]]
   [goodplace.detect :as detect]
   [goodplace.shared.routes :as routes]
   [applied-science.js-interop :as j]
   [react :as react]
   [tggreene.inertia-cljs :as inertia-cljs]))

(def top-bar-height 14)
(def top-bar-height-var "var(--chakra-sizes-14)")

(defnc UserMenu
  [{:keys [user] :as props}]
  (let [{:keys [first_name last_name]} user
        name (str first_name " " last_name)]
    ($ Menu
       ($ MenuButton {:as Button
                      :data-test "user-menu"
                      :rightIcon ($ ChevronDownIcon)
                      :textOverflow "ellipsis"
                      :overflow "hidden"}
          name)
       ($ MenuList
          (let [{:keys [name path]} (routes/get-route :logout)]
            ($ InertiaLink {:href path}
               ($ MenuItem name)))))))

(defnc TopBar
  [{:keys [user setMenuOpen menuOpen]}]
  ($ Flex {:height top-bar-height
           :align "center"
           :width "100%"
           :borderBottomWidth 1
           :borderBottomColor "gray.100"
           :borderBottomStyle "solid"
           :zIndex 1}
     ($ Box {:px 2}
        ($ IconButton {:icon ($ HamburgerIcon)
                       :onClick #(setMenuOpen (not menuOpen))}))
     ($ Box {:px 2}
        ($ Image {:src "/images/goodplace_small.png"
                  :height 6}))
     #_
     ($ Box
        ($ ResponsiveIndicator))
     ($ Spacer)
     ($ Flex {:px 2
              :minWidth #js [0 "8rem"]
              :justifyContent "flex-end"}
        (if user
          ($ UserMenu {:user user})
          (let [{:keys [path name]} (routes/get-route :login)]
            ($ InertiaLink {:href path}
               ($ Button {:textOverflow "ellipsis"
                          :overflow "hidden"
                          :data-test "login"}
                  name)))))))

(defn filter-pages
  [{:keys [user]} pages]
  (cond->> pages
    :always (filter :nav?)
    user (remove #(= :login (:id %)))
    (not user) (remove :authenticated?)))

(defnc NavigationItem
  [{:keys [path name]}]
  ($ InertiaLink {:as "span"
                  :href path}
     ($ Button {:bg "white"
                :borderRadius 0
                :width "100%"
                :justifyContent "flex-start"
                :px 8
                :py 8} name)))

(defnc NavigationMenu
  [{:keys [user pages menuOpen]}]
  (let [pages (routes/filter-pages {:user user} pages)]
    ($ Slide {:direction "left"
              :in menuOpen
              :style #js {:left 0
                          :top top-bar-height-var
                          :width "var(--chakra-sizes-xs)"
                          :height "max(100%, 100vh)"
                          :position "absolute"
                          :zIndex 10}}
       ($ Flex {:height "max(100%, 100vh)"
                :width "xs"
                :bg "white"
                :direction "column"
                & (when menuOpen
                    {:borderRightWidth 1
                     :borderRightColor "gray.100"
                     :borderRightStyle "solid"
                     :boxShadow "xs"})}
          (for [{:keys [id path name]} pages]
            ($ NavigationItem {:key id
                               :path path
                               :name name}))))))

(defnc Default
  [{:keys [pages children]}]
  (let [page (inertia-cljs/use-page)
        [menuOpen] (useLocalStorage "MenuOpen" false)
        setMenuOpen (fn [value]
                          (writeStorage "MenuOpen" value))
        user (get-in page [:props :auth :user])]
    ($ Flex {:direction "column"
             :justify "center"
             :align "center"
             :width "100%"
             & (when detect/capacitor?
                 {:mt "2rem"})}
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

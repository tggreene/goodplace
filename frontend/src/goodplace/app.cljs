(ns goodplace.app
  (:require
   ["@inertiajs/inertia" :refer [Inertia]]
   ["@inertiajs/inertia-react" :refer [createInertiaApp Head InertiaLink usePage]]
   ["@inertiajs/progress" :refer [InertiaProgress]]
   ["@chakra-ui/react" :refer [Box ChakraProvider Flex Heading Text]]
   ["react" :as react]
   ["react-dom/client" :as rdomc]
   ["react-dom" :as rdom]
   [applied-science.js-interop :as j]
   [goodplace.shared.routes :as routes]
   [helix.core :refer [defnc $ <>]]))

(defnc home
  []
  (let [page (usePage)]
    ($ Flex {:direction "column"
             :justify "center"
             :align "center"
             :width "100%"}
       ($ Box {:height 12})
       ($ Box {:padding 2}
          ($ Heading {:size "2xl"} "Home"))
       ($ Box {}
          ($ Text {:as "pre"} (pr-str page))))))

(defnc about
  []
  ($ Text "About"))

(def pages
  {"home" home
   "about" about})

(defnc layout
  [{:keys [children]}]
  (let [pageData (usePage)]
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
             ($ InertiaLink {:href "/"} "Home")
             ($ InertiaLink {:href "/about"} "About"))
          ($ Box {:py 2
                  :px 4}
             ($ Text "MaybeLogin")))
       ($ Box children))))

(defn setup-inertia
  []
  (createInertiaApp
   #js {:resolve (fn [name]
                   (if-let [^js comp (get pages name)]
                     (do
                       (set! (.-layout comp)
                             (j/fn [^:js page]
                               ($ layout page)))
                       comp)
                     (js/console.error (str "No page called " name " exists"))))
        :title (constantly "Goodplace")
        :setup
        (j/fn [^:js {:keys [el App props]}]
                 (let [root (rdomc/createRoot el)]
                   (.render root ($ ChakraProvider
                                    (react/createElement App #c props)))))}))

(defn start!
  []
  (setup-inertia))

(start!)

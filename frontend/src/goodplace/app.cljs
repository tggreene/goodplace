(ns goodplace.app
  (:require
   ["@inertiajs/inertia" :refer [Inertia]]
   ["@inertiajs/inertia-react" :refer [createInertiaApp Head InertiaLink usePage]]
   ["@inertiajs/progress" :refer [InertiaProgress]]
   ["@chakra-ui/react" :refer [Box ChakraProvider Flex Heading Text]]
   ["react" :as react]
   ["react-dom/client" :as rdom]
   [applied-science.js-interop :as j]
   [goodplace.inertia :refer [create-inertia-app]]
   [goodplace.layouts :as layouts]
   [goodplace.pages :as pages]
   [goodplace.shared.routes :as routes]
   [helix.core :refer [defnc $ <>]]
   [clojure.set :as set]
   [clojure.walk :as walk]))

(.init InertiaProgress)

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

(defnc login
  []
  (let []))

(defnc about
  []
  ($ Text "About"))

(def page-implementations
  {:home pages/Home
   :about pages/About
   :login pages/Login})

(defn check-page-implementations!
  []
  (->> (for [{:keys [id]} routes/pages]
         [id (contains? page-implementations id)])
       (filter (comp false? second))
       (run! #(js/console.warn (str "No page implementation for " (first %))))))

(defn setup-inertia
  []
  (create-inertia-app
   {:page-fn #(get page-implementations (keyword %))
    :pages routes/pages
    :title-fn (constantly "Goodplace")
    :root-component ChakraProvider
    :layout-component layouts/default}))

(defn start!
  []
  (check-page-implementations!)
  (setup-inertia))

(start!)

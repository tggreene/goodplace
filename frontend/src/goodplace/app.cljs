(ns goodplace.app
  (:require
   ["@inertiajs/inertia" :refer [Inertia]]
   ["@inertiajs/progress" :refer [InertiaProgress]]
   ["@chakra-ui/react" :refer [ChakraProvider]]
   [goodplace.inertia :refer [create-inertia-app]]
   [goodplace.layouts :as layouts]
   [goodplace.pages :as pages]
   [goodplace.shared.routes :as routes]
   [clojure.set :as set]
   [clojure.walk :as walk]
   [potpuri.core :as potpuri]))

(.init InertiaProgress)

(def page-implementations
  {:home pages/Home
   :about pages/About
   :login pages/Login})

(def pages
  (reduce (fn [pages {:keys [id] :as page}]
            (conj pages
                  (if-let [impl (get page-implementations id)]
                    (assoc page :impl impl)
                    page)))
          []
          routes/pages))

(def indexed-pages
  (potpuri/index-by :id pages))

(defn check-page-implementations!
  []
  (->> pages
       (remove :impl)
       (run! #(js/console.warn (str "No page implementation for " (:id %))))))

(defn setup-inertia
  []
  (create-inertia-app
   {:page-fn #(get-in indexed-pages [(keyword %) :impl])
    :pages pages
    :title-fn (constantly "Goodplace")
    :root-component ChakraProvider
    :layout-component layouts/default}))

(defn start!
  []
  (check-page-implementations!)
  (setup-inertia))

(start!)

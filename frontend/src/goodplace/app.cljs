(ns goodplace.app
  (:require
   ["@inertiajs/inertia" :refer [Inertia]]
   ["@inertiajs/progress" :refer [InertiaProgress]]
   ["@chakra-ui/react" :refer [ChakraProvider]]
   [goodplace.layouts :as layouts]
   [goodplace.pages :refer [page-components]]
   [goodplace.shared.routes :as routes]
   [tggreene.inertia-cljs :as inertia-cljs]
   [tggreene.inertia-cljs.impl.react18 :as inertia-cljs-react]
   [clojure.set :as set]
   [clojure.walk :as walk]
   [potpuri.core :as potpuri]))

(def pages
  (reduce (fn [pages {:keys [id] :as page}]
            (conj pages
                  (if-let [impl (get page-components id)]
                    (assoc page :impl impl)
                    page)))
          []
          routes/pages))

(def indexed-pages
  (potpuri/index-by :id pages))

(defn check-page-components!
  []
  (->> pages
       (remove :impl)
       (run! #(js/console.warn (str "No page component for " (:id %))))))

(defn setup-inertia
  []
  (inertia-cljs/simple-inertia-app
   {:page-fn #(get-in indexed-pages [(keyword %) :impl])
    :title-fn #(str % " - GoodPlace")
    :layout-component layouts/Default
    :layout-props {:pages pages}
    :root-component ChakraProvider
    :react-root-renderer inertia-cljs-react/renderer}))

(defn start!
  []
  (check-page-components!)
  (setup-inertia))

(start!)

(ns goodplace.app
  (:require
   ["@chakra-ui/react" :refer [ChakraProvider Text]]
   ["react-dom/client" :as rdom]
   [applied-science.js-interop :as j]
   [helix.core :refer [defnc $ <>]]))

(defonce root
  (rdom/createRoot (js/document.getElementById "app")))

(js/console.log "hello")

(defnc something
  []
  ($ ChakraProvider
     ($ Text "Hello World")))

(def pages [])

(defn setup-inertia
  []
  (createInertiaApp
   #js {:resolve (fn [name]
                   (if-let [page (get pages name)]
                     ;; We can set layouts here but may not be required for this app
                     #_
                     (when-not (contains? unauthenticated-pages name)
                       (set! (.-layout comp) (fn [page] ($ layout page))))
                     page
                     (js/console.error (str "No page called " name " exists"))))
        :title (fn [title] (str title " | Ping CRM"))
        :setup (j/fn [^:js {:keys [el App props]}]
                 (.render root ($ App props)))}) )


(defn start!
  []
  )



(start!)

(defn init! []
  )

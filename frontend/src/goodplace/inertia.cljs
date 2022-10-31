(ns goodplace.inertia
  (:require
   ["@inertiajs/inertia-react" :refer [createInertiaApp useForm usePage]]
   ["react" :as react]
   ["react-dom/client" :as react-dom]
   [applied-science.js-interop :as j]))

(defonce root nil)

(defn create-inertia-app
  [{:keys [page-fn
           title-fn
           pages
           layout-component
           root-component]}]
  (createInertiaApp
   #js {:resolve
        (fn [name]
          (if-let [^js comp (page-fn name)]
            (do
              (set! (.-layout comp)
                    (fn [page]
                      (react/createElement layout-component
                                           #js {:pages pages}
                                           page)))
              comp)
            (js/console.error (str "No page called " name " exists"))))
        :title title-fn
        :setup
        (j/fn [^:js {:keys [el App props]}]
          (when-not root
            (set! root (react-dom/createRoot el)))
          (.render root (react/createElement root-component
                                             nil
                                             (react/createElement App props))))}))

(defn use-form
  [initialData]
  (let [{:keys [data setData post processing errors]}
        (j/lookup (useForm (clj->js initialData)))]
    {:data (js->clj data :keywordize-keys true)
     :setData #(setData (name %1) %2)
     :post post
     :processing processing
     :errors (js->clj errors :keywordize-keys true)}))

(defn use-page
  []
  (let [page (usePage)]
    (js->clj page :keywordize-keys true)))

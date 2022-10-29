(ns goodplace.pages
  (:require
   ["@chakra-ui/react" :refer [Box Flex Heading Button Text Input]]
   ["@inertiajs/inertia-react" :refer [Head InertiaLink usePage useForm]]
   [helix.core :refer [defnc $ <>]]
   [goodplace.shared.routes :as routes]
   [applied-science.js-interop :as j]
   [clojure.pprint :refer [pprint]]))

(defnc JsObjectBlock
  [{:keys [object]}]
  ($ Text {:as "pre"}
     (-> object
         (js->clj :keywordize-keys true)
         pprint
         with-out-str)))

(defnc PageTemplate
  [{:keys [title children]}]
  (let [page (usePage)]
    ($ Flex {:direction "column"
             :justify "center"
             :align "center"
             :width "100%"
             :gap 4}
       ($ Box {:p 4}
          ($ Heading {:size "2xl"} title))
       children
       ($ Box
          ($ JsObjectBlock {:object page})) )))

(defnc Home
  []
  ($ PageTemplate
     {:title "Home"}
     ($ Box {}
        (let [{:keys [path name]} (routes/get-route :about)]
          ($ InertiaLink {:href path} name)))))

(defnc Login
  []
  (let [{:keys [data setData errors post processing]}
        (j/lookup (useForm #js {:email ""
                                :password ""}))
        errors (js->clj errors)]
    ($ PageTemplate
       {:title "Login"}
       ($ "form" {:on-submit
                  #(do (.preventDefault %)
                       (post (:path (routes/get-route :authenticate))))}
          ($ Flex {:direction "column"
                   :p 2
                   :gap 2
                   :width "md"}
             ($ Input {:type "text"
                       :placeholder "Email"
                       :value (.-email data)
                       :onChange #(setData "email" (.. % -target -value))})
             ($ Input {:type "password"
                       :placeholder "Password"
                       :value (.-password data)
                       :onChange #(setData "password" (.. % -target -value))})
             ($ Button {:type "submit"
                        :colorScheme "blue"} "Submit")
             (when (not-empty errors)
               ($ Box {:p 4
                       :bg "red.50"
                       :borderRadius 6}
                  #_
                  {:p 4
                       :borderColor "red.100"
                       :borderWidth 2
                       :borderRadius 6}
                  (for [error (vals errors)]
                    ($ Text {:color "red.500"} error)))))))))

(defnc About
  []
  ($ PageTemplate {:title "About"}))

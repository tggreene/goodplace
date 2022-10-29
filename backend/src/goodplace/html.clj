(ns goodplace.html
  (:require [clojure.edn :as edn]
            [hiccup.page :as page]))

(defn template
  [data-page]
  (page/html5
   [:head
    [:meta {:charset "utf-8"}]
    [:link {:rel "icon" :type "image/svg+xml" :href "/favicon.svg"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
    [:script {:src (str "/js/libs.js") :defer true}]
    [:script {:src (str "/js/app.js") :defer true}]]
   [:body
    [:div {:id "app"
           :data-page data-page}]]))

(ns goodplace.templates.app
  (:require [clojure.edn :as edn]
            [hiccup.page :as page]))

(defn js-script
  []
  (-> (slurp "public/js/manifest.edn")
      edn/read-string
      first
      :output-name))

(defn template
  [data-page]
  (page/html5
   nil
   [:head
    [:meta {:charset "utf-8"}]
    [:link {:rel "icon" :type "image/svg+xml" :href "/favicon.svg"}]
    [:link {:rel "icon" :type "image/png" :href "/favicon.png"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
    [:script {:src "/js/libs.js" :defer true}]
    [:script {:src (str "/js/" (js-script)) :defer true}]]
   [:body
    [:div {:id "app"
           :data-page data-page}]]))

(ns goodplace.html)

(defn template [data-page]
  (page/html5
   [:head
    [:meta {:charset "utf-8"}]
    [:link {:rel "icon" :type "image/svg+xml" :href "/favicon.svg"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
    [:script {:src (str "/js/" (js-script)) :defer true}]]
   [:body
    [:div {:id "app"
           :data-page data-page}]]))

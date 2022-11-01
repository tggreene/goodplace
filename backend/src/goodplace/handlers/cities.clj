(ns goodplace.handlers.cities
  (:require [goodplace.handlers.common :as common]
            [goodplace.models.cities :as model]
            [goodplace.utils.pagination :as pagination]
            [inertia.middleware :as inertia]))

(defn list-cities
  [context]
  (fn [{:keys [params query-string uri] :as request}]
    (let [filters (select-keys params [:search :country])
          all-cities @model/cities
          page (Integer/parseInt (get params :page "1"))
          offset (* (dec page) common/per-page)
          count (count all-cities)
          cities (->> all-cities
                      (drop offset)
                      (take common/per-page))
          props {:cities
                 {:data cities
                  :current_page page
                  :links (pagination/links
                          uri
                          query-string
                          page
                          count
                          common/per-page)}
                 :filters filters}]
      (inertia/render :cities props))))

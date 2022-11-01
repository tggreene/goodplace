(ns goodplace.components
  (:require
   ["@chakra-ui/react" :refer [CircularProgress Text]]
   [helix.core :refer [defnc $ <>]]
   [helix.hooks :as hooks]))

(defnc CircularProgressTimed
  [{:keys [time-ms]}]
  (let [[progress setProgress] (hooks/use-state 0)]
    (hooks/use-effect
     :once
     (let [interval-ms 100
           interval (atom nil)]
       (reset! interval
               (js/setInterval
                (fn []
                  (setProgress
                   #(if (< % 100)
                      (+ (* (/ 100 time-ms) interval-ms) %)
                      (do (js/clearInterval @interval)
                          100))))
                interval-ms))))
    ($ CircularProgress
       {:value progress})))

(defnc ResponsiveIndicator
  []
  (<>
   ($ Text {:display #js ["inline" "none" "none" "none" "none" "none"]} "base")
   ($ Text {:display #js ["none" "inline" "none" "none" "none" "none"]} "sm")
   ($ Text {:display #js ["none" "none" "inline" "none" "none" "none"]} "md")
   ($ Text {:display #js ["none" "none" "none" "inline" "none" "none"]} "lg")
   ($ Text {:display #js ["none" "none" "none" "none" "inline" "none"]} "xl")
   ($ Text {:display #js ["none" "none" "none" "none" "none" "inline"]} "2xl")))

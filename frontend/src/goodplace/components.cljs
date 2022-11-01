(ns goodplace.components)

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

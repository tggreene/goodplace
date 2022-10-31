(ns goodplace.utils.forms)

(defn target-value
  [event]
  (.. event -target -value))

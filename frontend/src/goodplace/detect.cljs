(ns goodplace.detect)

(def capacitor?
  (try
    (some? js/Capacitor)
    (catch :default _
      false)))

(ns goodplace.dev
  (:require
   [easy.system :as es]
   [integrant.repl :as igr]
   [integrant-repl-autoreload.core :as igr-auto]
   [integrant.repl.state :as state]))

(igr/set-prep!
 (fn []
   (es/system-config {:profile :dev})))

(def go igr/go)
(def halt igr/halt)
(def reset igr/reset)
(def reset-all igr/reset-all)

(comment
  (go)

  (reset)

  (halt)

  (igr-auto/start-auto-reset)

  (igr-auto/stop-auto-reset)

  )

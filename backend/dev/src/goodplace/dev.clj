(ns ^{:clojure.tools.namespace.repl/load false} goodplace.dev
  (:require
   [clojure.tools.logging :as log]
   [goodplace.dev.db :as db]
   [integrant.repl :as igr]
   [integrant-repl-autoreload.core :as igr-auto]
   [integrant.repl.state :as state]
   [migratus.core :as migratus]
   [tggreene.easy-system :as es]))

(igr/set-prep!
 (fn []
   (es/system-config {:profile :dev})))

(def uncaught-exception-handler
  (reify Thread$UncaughtExceptionHandler
    (uncaughtException [_ thread throwable]
      (log/error throwable))))

(Thread/setDefaultUncaughtExceptionHandler uncaught-exception-handler)

(def go igr/go)
(def halt igr/halt)
(def reset igr/reset)
(def reset-all igr/reset-all)
(def auto-reset igr-auto/start-auto-reset)
(def stop-auto-reset igr-auto/stop-auto-reset)

(defn init!
  []
  ;; We can't manage the PG depedency via integrant for now because of it's
  ;; own state management
  (db/run-dev-db!)
  (go)
  (auto-reset))

(comment
  (go)

  (reset)

  (halt)

  (igr-auto/start-auto-reset)

  (igr-auto/stop-auto-reset)

  )

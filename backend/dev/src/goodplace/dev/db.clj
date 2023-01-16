(ns goodplace.dev.db
  (:require [clojure.tools.logging :as log]
            [pg-embedded-clj.core :as pge]))

(defn run-dev-db!
  []
  (log/info "Starting" ::postgres-server)
  (pge/init-pg)
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. (fn []
                               (log/info "Stopping" ::postgres-server)
                               (pge/halt-pg!)))))

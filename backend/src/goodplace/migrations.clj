(ns goodplace.migrations
  (:require [clojure.tools.logging :as log]
            [integrant.core :as ig]
            [migratus.core :as migratus]))

(defmethod ig/init-key ::migrations
  [_ config]
  (log/info "Starting" ::migrations)
  (migratus/migrate config))

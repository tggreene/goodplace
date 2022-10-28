(ns goodplace.system-config
  (:require
   [aero.core :as aero]
   [clojure.java.io :as io]
   [integrant.core :as ig]
   [clojure.tools.logging :as log]))

(defmethod aero/reader 'ig/ref [_ _ value]
  (ig/ref value))

(let [lock (Object.)]
  (defn- load-namespaces
    [system-config]
    (locking lock
      (ig/load-namespaces system-config))))

(defn config
  "Read EDN config, with the given aero options. See Aero docs at
  https://github.com/juxt/aero for details."
  [opts]
  (-> (io/resource "config.edn")
      (aero/read-config opts)))

(defn system-config
  "Construct a new system, configured with the given profile"
  [opts]
  (let [config (config opts)
        system-config (:ig/system config)]
    (load-namespaces system-config)
    (with-meta (ig/prep system-config)
      {:options opts})))

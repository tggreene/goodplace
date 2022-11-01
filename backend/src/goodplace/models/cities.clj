(ns goodplace.models.cities
  (:require [clojure.java.io :as io]
            [jsonista.core :as json])
  (:import [java.util.zip GZIPInputStream GZIPOutputStream]) )

(def -cities-file "resources/us-cities.json.gz")

(def cities
  (delay
    (with-open [is (GZIPInputStream.
                    (io/input-stream -cities-file))]
      (json/read-value is json/keyword-keys-object-mapper))) )

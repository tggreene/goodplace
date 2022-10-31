(ns goodplace.examples.cities
  (:require [clojure.java.io :as io]
            [jsonista.core :as json])
  (:import [java.util.zip GZIPInputStream GZIPOutputStream]))

(def cities
  (delay
    (with-open [is (GZIPInputStream. (io/input-stream "resources/us-cities.json.gz"))]
      (json/read-value is json/keyword-keys-object-mapper))) )

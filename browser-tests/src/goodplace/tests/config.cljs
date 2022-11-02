(ns goodplace.tests.config)

(def origin "http://localhost:8090")

(defn url
  ([] origin)
  ([path]
   (str origin path)))

(def default-timeout-ms 5000)

(def example-email "example@example.com")
(def example-password "password")
(def example-name "Example Man")

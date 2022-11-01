(ns goodplace.utils.coerce
  (:import [java.util UUID]))

(defn to-int
  [x]
  (cond
    (string? x) (Integer/parseInt x)
    (number? x) (int x)
    :else nil))

(defn to-uuid
  [x]
  (cond
    (string? x) (UUID/fromString x)
    :else nil))

(ns goodplace.utils.coerce
  (:import [java.util UUID]))

(defn to-int
  [x]
  (cond
    (int? x) x
    (string? x) (Integer/parseInt x)
    (number? x) (int x)
    :else nil))

(defn to-uuid
  [x]
  (cond
    (uuid? x) x
    (string? x) (UUID/fromString x)
    :else nil))

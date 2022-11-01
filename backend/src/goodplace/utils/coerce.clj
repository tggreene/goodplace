(ns goodplace.utils.coerce)

(defn to-int
  [x]
  (cond
    (string? x) (Integer/parseInt x)
    (number? x) (int x)
    :else nil))

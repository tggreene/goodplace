(ns goodplace.utils.pagination
  (:require [clojure.string :as str]))

(defn pagination-range
  [link-count current-page page-number]
  (let [half (int (Math/floor (/ link-count 2)))
        before (- current-page 1)
        after (- page-number current-page)
        current-range
        (cond
          (< before half) (range (- current-page before)
                                 (+ current-page (- link-count before)))
          (< after half) (range (inc (- current-page (- link-count after)))
                                (inc (+ current-page after)))
          :else (range (- current-page half)
                       (+ current-page half (if (odd? link-count) 1 0))))]
    (filter #(<= % page-number) current-range)))

(comment
  [(= (pagination-range 10 2 100) '(1 2 3 4 5 6 7 8 9 10))
   (= (pagination-range 10 30 100) '(25 26 27 28 29 30 31 32 33 34))
   (= (pagination-range 10 98 100) '(91 92 93 94 95 96 97 98 99 100))]

  (=
   [(pagination-range 11 2 100)
    (pagination-range 11 30 100)
    (pagination-range 11 98 100)]
   ['(1 2 3 4 5 6 7 8 9 10 11)
    '(25 26 27 28 29 30 31 32 33 34 35)
    '(90 91 92 93 94 95 96 97 98 99 100)])

  (pagination-range 11 1 1)

  )

(defn links
  [uri query-string current-page total per-page]
  (let [uri (str uri
                 "?"
                 (when query-string
                   (str/replace query-string #"&page=.*" ""))
                 "&page=")
        page-number (int (Math/ceil (/ total per-page)))
        previous-link {:url (when (> current-page 1) (str uri (dec current-page)))
                       :label "Previous"
                       :active nil}
        next-link {:url (when (< current-page page-number) (str uri (inc current-page)))
                   :label "Next"
                   :active nil}
        range (pagination-range 11 current-page page-number)
        links (->> (for [item range]
                     {:url (str uri item)
                      :label (str item)
                      :active (when (= item current-page) true)}))]
    (flatten [previous-link links next-link])))

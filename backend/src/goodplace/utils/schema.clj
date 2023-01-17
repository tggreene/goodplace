(ns goodplace.utils.schema
  (:require [clojure.string :as str]
            [malli.core :as m]
            [malli.error :as me]
            [malli.transform :as mt]
            [potpuri.core :as po]))

(def non-empty-string
  [:string {:error/message "Must not be empty"
            :min 1}])

(def email-address
  [:and non-empty-string
   [:re {:error/message "Must be a valid email address"}
    ".+@.+\\..+"]])

(defn fields-must-match
  [field1 field2 error-message]
  [:fn {:error/message "Passwords don't match"
        :error/path [:password2]}
   (fn [{:keys [password password2]}]
     (= password password2))])

(defn humanize-more
  [explanation]
  (->> explanation
       me/humanize
       (po/map-vals
        (po/fn->>
         (map str/capitalize)
         (str/join ", ")))))

(defn make-validator
  [schema]
  (let [explain (m/explainer schema)]
    (fn [item]
      (humanize-more (explain item)))))

(defn make-decoder
  [schema]
  (m/decoder schema mt/string-transformer))

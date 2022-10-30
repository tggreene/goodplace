(ns goodplace.handlers
  (:require [clojure.string :as str]
            [crypto.password.bcrypt :as password]
            [goodplace.examples.cities]
            [goodplace.models.notes :as notes]
            [goodplace.models.users :as users]
            [goodplace.shared.routes :as routes]
            [inertia.middleware :as inertia]
            [ring.util.response :as response]))

(defn get-user
  [request]
  (some-> request
          :session
          :identity))

(defn authenticated?
  [request]
  (-> request
      :session
      :identity
      not-empty
      boolean))

(defn authenticate
  "Check request username and password against authdata
  username and passwords."
  [{:keys [db] :as context}]
  (fn [request]
    (let [email (-> request :body-params :email)
          password (-> request :body-params :password)
          user (users/get-user-by-email db email)
          sanitized-user (dissoc user :password)
          session (:session request)]
      (if (and user (password/check password (:password user)))
        (let [updated-session (assoc session :identity sanitized-user)]
          (-> (response/redirect "/")
              (assoc :session updated-session)))
        (-> (response/redirect "/login")
            (assoc :flash
                   {:error
                    {:email "These credentials do not match our records."}}))))))

(defn login
  [request]
  (if (authenticated? request)
    (response/redirect (routes/get-route-path :home) :see-other)
    (inertia/render :login)))

(defn logout
  [_]
  (-> (response/redirect "/" :see-other)
      (assoc :session nil)))

(defn notes
  [{:keys [db]}]
  (fn [request]
    (let [{:keys [id] :as user} (get-user request)
          notes (notes/list-user-notes db id)]
      (inertia/render :notes {:notes notes}))))

(defn create-note
  [])

(defn delete-note
  [])

(defn get-pagination-range
  [link-count current-page page-number]
  (let [half (int (Math/floor (/ link-count 2)))
        before (- current-page 1)
        after (- page-number current-page)
        tweak-odd (if (odd? link-count)
                    inc
                    identity)]
    (cond
      (< before half) (range (- current-page before)
                             (+ current-page (- link-count before)))
      (< after half) (range (inc (- current-page (- link-count after)))
                            (inc (+ current-page after)))
      :else (range (- current-page half)
                   (+ current-page half (if (odd? link-count) 1 0))))))

(comment
  [(= (get-pagination-range 10 2 100) '(1 2 3 4 5 6 7 8 9 10))
   (= #p (get-pagination-range 10 30 100) '(25 26 27 28 29 30 31 32 33 34))
   (= #p (get-pagination-range 10 98 100) '(91 92 93 94 95 96 97 98 99 100))]

  #p
  (=
   [(get-pagination-range 11 2 100)
    (get-pagination-range 11 30 100)
    (get-pagination-range 11 98 100)]
   ['(1 2 3 4 5 6 7 8 9 10 11)
    '(25 26 27 28 29 30 31 32 33 34 35)
    '(90 91 92 93 94 95 96 97 98 99 100)])

  )
(defn pagination-links
  [uri query-string current-page total per-page]
  (let [uri (str uri
                 "?"
                 (when query-string
                   (str/replace query-string #"&page=.*" ""))
                 "&page=")
        page-number (/ total per-page)
        previous-link {:url (when (> current-page 1) (str uri (dec current-page)))
                       :label "Previous"
                       :active nil}
        next-link {:url (when (< current-page page-number) (str uri (inc current-page)))
                   :label "Next"
                   :active nil}
        range (get-pagination-range 11 current-page page-number)
        links (->> (for [item range]
                     {:url (str uri item)
                      :label (str item)
                      :active (when (= item current-page) true)}))]
    (flatten [previous-link links next-link])))

(defn cities
  [context]
  (fn [{:keys [params query-string uri] :as request}]
    (let [filters (select-keys params [:search :country])
          all-cities @goodplace.examples.cities/cities
          page (Integer/parseInt (get params :page "1"))
          offset (* (dec page) 10)
          count (count all-cities)
          cities (->> all-cities
                      (drop offset)
                      (take 10))
          props {:cities {:data cities
                          :current_page page
                          :links (pagination-links uri query-string page count 10)}
                 :filters filters}]
      (inertia/render :cities props))))

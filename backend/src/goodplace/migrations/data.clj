(ns goodplace.migrations.data
  (:require [goodplace.models.notes :as notes]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as result-set]
            [goodplace.models.users :as users]))

(def test-users
  [{:id 1
    :first_name "Tim"
    :last_name "Greene"
    :username "tggreene"
    :email "tim@example.com"
    :password "password"}
   {:id 2
    :first_name "Bill"
    :last_name "Wharton"
    :username "bill@example.com"
    :email "bill@example.com"
    :password "password"}
   {:id 3
    :first_name "Joey"
    :last_name "JoeJoeJoeson"
    :username "joe@example.com"
    :email "joe@example.com"
    :password "password"}
   {:id 4
    :first_name "Example"
    :last_name "Man"
    :username "exampleman"
    :email "example@example.com"
    :password "password"}])

(defn create-test-users!
  [db]
  (doseq [user test-users]
    (users/create-user! db user)))

(def test-notes
  [{:id #uuid "d6012295-c3e5-4961-a529-b95f20121065"
    :user_id 1
    :title "Test"
    :contents "Something something asdfasdfsadf dsfdsf"}
   {:id #uuid "5c598b52-ab5d-4a38-aaca-ffc1e715eb92"
    :user_id 1
    :title "My Thoughts About Blah Blah Blah"
    :contents "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."}
   {:id #uuid "63648867-07c0-4c68-b373-34a3e0420cd5"
    :user_id 1
    :title "This is pretty Cooool"
    :contents "Something about this app is nice"}
   {:id #uuid "fe815e29-b0c1-451c-ba34-b90c23f02f11"
    :user_id 1
    :title "ybuhhhb"
    :contents "hbblbhlbhjhj"}
   {:id #uuid "f9ab400b-e3e5-4402-8d4e-18d13c60d49c"
    :user_id 2
    :title "afasdf"
    :contents "asdfadfasdf"}
   {:id #uuid "04046b29-604a-4e08-a643-e4a098dff1b5"
    :user_id 4
    :title "Bing"
    :contents "Bong"}
   {:id #uuid "b04dc86b-545e-46df-ba91-e43769057ace"
    :user_id 2
    :title "Something"
    :contents "Good can work"}
   {:id #uuid "2e3a244a-0a49-4db4-bb99-ab2cb3d4d784"
    :user_id 2
    :title "Bing"
    :contents "Bong"}])

(defn create-test-notes!
  [db]
  (doseq [note test-notes]
    (notes/create-note! db note)))

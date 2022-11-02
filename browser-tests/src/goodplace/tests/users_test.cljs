(ns goodplace.tests.users-test
  (:require [cljs.test :refer [deftest testing is async]]
            [goodplace.tests.resources :as resources]
            [clojure.string :as str]
            [promesa.core :as p]
            [goodplace.tests.config :as config]
            [goodplace.tests.common :as common]))

(deftest can-add-and-delete-user
  (async done
    (-> (p/let [^js browser @resources/browser
                ^js context (.newContext browser)
                ^js page (.newPage context)]

          (testing "Add User"
            (p/let [_ (common/login page)
                    _ (.goto page (config/url "/users"))
                    _ (.. page
                          (getByRole "button")
                          (getByText "Create User")
                          (click))
                    _ (.. page
                          (waitForNavigation #js {:timeout config/default-timeout-ms}))
                    _ (.. page
                          (getByLabel "First Name")
                          (fill "Test"))
                    _ (.. page
                          (getByLabel "Last Name")
                          (fill "User"))
                    _ (.. page
                          (getByLabel "Username")
                          (fill "testuser"))
                    _ (.. page
                          (getByLabel "Email")
                          (fill "test@example.com"))
                    _ (.. page
                          (getByLabel "Password" #js {:exact true})
                          (fill "password"))
                    _ (.. page
                          (getByLabel "Re-type Password")
                          (fill "password"))
                    _ (.. page
                          (getByRole "button")
                          (getByText "Submit")
                          (click))
                    _ (.. page
                          (waitForNavigation #js {:timeout config/default-timeout-ms}))
                    current-url (.url page)
                    user-row (.. page
                                 (locator "table tbody tr:last-child")
                                 (textContent))]
              (is (= (str resources/origin "/users") current-url))
              (is (str/starts-with? user-row "Test Usertest@example.com"))))

          (testing "Delete User"
            (p/let [_ (.goto page (config/url "/users"))
                    _ (.. page
                          (locator "table tbody tr:last-child")
                          (getByRole "button")
                          (getByText "Delete User")
                          (click))
                    _ (.. page
                          (waitForNavigation #js {:timeout config/default-timeout-ms}))
                    user-row (.. page
                          (locator "table tbody tr:last-child")
                          (textContent))
                    current-url (.url page)]
              (is (= (str resources/origin "/users") current-url))
              (is (not (str/starts-with? user-row "Test Usertest@example.com"))) )))

        (p/catch
            (fn [err]
              (js/console.error err)
              (is false)))
        (p/finally
          (fn []
            (done))))))

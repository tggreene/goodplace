(ns goodplace.tests.login-test
  (:require [cljs.test :refer [deftest testing is async]]
            [clojure.string :as str]
            [goodplace.tests.config :as config]
            [goodplace.tests.resources :as resources]
            [promesa.core :as p]))

(deftest can-login-and-logout
  (async done
    (-> (p/let [^js browser @resources/browser
                ^js context (.newContext browser)
                ^js page (.newPage context)]

          (testing "Login"
            (p/let [_ (.goto page (config/url "/login"))
                    _ (.. page
                          (getByLabel "Email")
                          (fill config/example-email))
                    _ (.. page
                          (getByLabel "Password")
                          (fill config/example-password))
                    _ (.. page
                          (getByRole "button")
                          (getByText "Submit")
                          (click))
                    _ (.. page
                          (waitForNavigation #js {:timeout 5000}))
                    current-url (.url page)
                    button-text (.. page
                                    (locator "[data-test=user-menu]")
                                    (textContent))]
              (is (= (str resources/origin "/") current-url))
              (is (= config/example-name button-text))))

          (testing "logout"
            (p/let [_ (.. page
                          (locator "[data-test=user-menu]")
                          (click))
                    _ (.. page
                          (locator "text=Logout")
                          (click))
                    _ (.. page
                          (waitForNavigation #js {:timeout 5000}))
                    current-url (.url page)
                    button-text (.. page
                                    (locator "[data-test=login]")
                                    (textContent))]
              (is (= (str resources/origin "/") current-url))
              (is (= "Login" button-text)))))

        (p/catch
            (fn [err]
              (js/console.error err)
              (is false)))
        (p/finally
          (fn []
            (done))))))

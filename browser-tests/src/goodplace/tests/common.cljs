(ns goodplace.tests.common
  (:require [promesa.core :as p]
            [goodplace.tests.config :as config]))

#_
(defn login
  [^js page]
  (p/all [(.goto page (config/url "/login"))
          (.. page
              (getByLabel "Email")
              (fill config/example-email))
          (.. page
              (getByLabel "Password")
              (fill config/example-password))
          (.. page
              (getByRole "button")
              (getByText "Submit")
              (click))
          (.. page
              (waitForNavigation #js {:timeout config/default-timeout-ms}))]))

(defn login
  [^js page]
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
              (waitForNavigation #js {:timeout config/default-timeout-ms}))]))

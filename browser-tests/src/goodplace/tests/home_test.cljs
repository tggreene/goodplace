(ns goodplace.tests.home-test
  (:require [cljs.test :refer [deftest testing is async]]
            [goodplace.tests.resources :as resources]
            [clojure.string :as str]
            [promesa.core :as p]
            [goodplace.tests.config :as config]))

(deftest can-see-homepage-title
  (async done
    (-> (p/let [^js browser @resources/browser
                ^js context (.newContext browser)
                ^js page (.newPage context)
                _ (.goto page (config/url))
                content (.content page)]
          (is (str/includes? content "Home")))
        (p/catch
            (fn [err]
              (js/console.error err)
              (is false)))
        (p/finally
          (fn []
            (done))))))

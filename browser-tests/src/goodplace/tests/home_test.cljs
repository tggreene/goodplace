(ns goodplace.tests.home-test
  (:require [cljs.test :refer [deftest testing is async]]
            [goodplace.tests.resources :as resources]
            [clojure.string :as str]
            [promesa.core :as p]))

(deftest can-see-homepage-title
  (async done
    (p/let [^js browser @resources/browser]
      (-> (p/let [^js context (.newContext browser)
                  ^js page (.newPage context)
                  _ (.goto page "http://localhost:8090")
                  content (.content page)
                  _ (js/console.log content)]
            (is (str/includes? content "Home")))
          (p/catch
              (fn [err]
                (js/console.error err)
                (is false)))
          (p/finally
            (fn []
              (done)))))))

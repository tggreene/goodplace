(ns goodplace.tests.runner
  (:require
   ["playwright" :as playwright]
   [applied-science.js-interop :as j]
   [cljs.test]
   [goodplace.tests.resources :as resources]
   [promesa.core :as p]
   [shadow.test.node]))

(defn start-browser!
  []
  (swap! resources/browser
         (fn [browser]
           (if-not browser
             (let [browser (.launch playwright/chromium #js {:headless true})]
               (js/console.log "Browser starting")
               browser)
             (do (js/console.warn "Browser already running")
                 browser)))))

(defn close-browser!
  []
  (swap! resources/browser
         (fn [browser]
           (if browser
             (p/let [browser browser]
               (.close browser)
               (js/console.log "Browser stopped")
               nil)
             (js/console.warn "Browser not running") ))))

(defn run-tests
  []
  (start-browser!)
  (-> (shadow.test.node/main)
      #_
      (p/finally (close-browser!))))

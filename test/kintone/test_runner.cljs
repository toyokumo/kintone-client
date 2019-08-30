(ns kintone.test-runner
  (:require [cljs-test-display.core]
            [figwheel.main.testing :refer-macros [run-tests run-tests-async]]
            ;; require all the namespaces that have tests in them
            [kintone.authentication-test]
            [kintone.connection-test]
            [kintone.record-test]))

(goog-define ^boolean DEBUG false)

(when DEBUG
  (run-tests (cljs-test-display.core/init! "app")))

(defn -main [& args]
  ;; this needs to be the last statement in the main function so that it can
  ;; return the value `[:figwheel.main.async-result/wait 10000]`
  (run-tests-async 10000))

(ns cljs.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            ;; require all the namespaces that have tests in them
            [kintone.app-test]
            [kintone.authentication-test]
            [kintone.connection-test]
            [kintone.record-test]
            [kintone.url-test]))

(doo-tests 'kintone.app-test
           'kintone.authentication-test
           'kintone.connection-test
           'kintone.record-test
           'kintone.url-test)

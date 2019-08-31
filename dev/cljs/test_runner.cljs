(ns cljs.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            ;; require all the namespaces that have tests in them
            [kintone.authentication-test]
            [kintone.connection-test]
            [kintone.record-test]))

(doo-tests 'kintone.authentication-test
           'kintone.connection-test
           'kintone.record-test)

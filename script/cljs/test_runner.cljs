(ns cljs.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   ;; require all the namespaces that have tests in them
   [kintone-client.app-test]
   [kintone-client.authentication-test]
   [kintone-client.connection-test]
   [kintone-client.record-test]
   [kintone-client.url-test]
   [kintone-client.user-test]))

(doo-tests 'kintone-client.app-test
           'kintone-client.authentication-test
           'kintone-client.connection-test
           'kintone-client.record-test
           'kintone-client.url-test
           'kintone-client.user-test)

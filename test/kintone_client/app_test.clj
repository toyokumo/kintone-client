(ns kintone-client.app-test
  (:require [clojure.core.async :refer [<!!]]
            [clojure.test :refer :all]
            [kintone-client.app :as app]
            [kintone-client.test-helper :as h]
            [kintone-client.types :as t]))

(def ^:private app (rand-int 100))

(deftest get-app-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/app.json"
                               :req {:params {:id app}}}
                              nil)
         (<!! (app/get-app h/fake-conn app)))))

(deftest get-form-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/form.json"
                               :req {:params {:app app}}}
                              nil)
         (<!! (app/get-form h/fake-conn app)))))

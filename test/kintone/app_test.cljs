(ns kintone.app-test
  (:require [cljs.core.async :refer [<!] :refer-macros [go]]
            [cljs.test :refer-macros [async deftest is]]
            [kintone.app :as app]
            [kintone.test-helper :as h]
            [kintone.types :as t]))

; (def ^:private app (rand-int 100))
;
; (deftest get-app-test
;   (async done
;     (go
;       (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/app.json"
;                                    :req {:params {:id app}}}
;                                   nil)
;              (<! (app/get-app h/fake-conn app))))
;       (done))))
;
; (deftest get-form-test
;   (async done
;     (go
;       (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/form.json"
;                                    :req {:params {:app app}}}
;                                   nil)
;              (<!  (app/get-form h/fake-conn app))))
;       (done))))

;MIT License
;
;Copyright (c) 2017 ayato-p
;
;Permission is hereby granted, free of charge, to any person obtaining a copy
;of this software and associated documentation files (the "Software"), to deal
;in the Software without restriction, including without limitation the rights
;to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
;copies of the Software, and to permit persons to whom the Software is
;furnished to do so, subject to the following conditions:
;
;The above copyright notice and this permission notice shall be included in all
;copies or substantial portions of the Software.
;
;THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
;IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
;FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
;AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
;LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
;OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
;SOFTWARE.
(ns kintone.url-test
  (:require [kintone.url :as sut]
            [clojure.test :as t]))

(t/deftest parse-app-url-test
  (t/testing "parse default space app"
    (t/are [url parsed] (= (sut/parse-app-url url) parsed)
                        "https://foo.cybozu.com/k/1"
                        {:subdomain "foo" :domain "cybozu.com" :app-id "1"}

                        "https://foo.cybozu.com/k/99999999"
                        {:subdomain "foo" :domain "cybozu.com" :app-id "99999999"}

                        "https://foo.s.cybozu.com/k/1"
                        {:subdomain "foo" :domain "cybozu.com" :app-id "1"}

                        "https://foo-bar.cybozu.com/k/1"
                        {:subdomain "foo-bar" :domain "cybozu.com" :app-id "1"}

                        "https://foo-bar-baz.cybozu.com/k/1"
                        {:subdomain "foo-bar-baz" :domain "cybozu.com" :app-id "1"}

                        "https://foo99.cybozu.com/k/1"
                        {:subdomain "foo99" :domain "cybozu.com" :app-id "1"}

                        "https://foo99.cybozu.com/k/1/show"
                        {:subdomain "foo99" :domain "cybozu.com" :app-id "1"}

                        "https://foo99.cybozu.com/k/1/?q=foo%20%3D%20\"1\""
                        {:subdomain "foo99" :domain "cybozu.com" :app-id "1"}

                        "https://foo_bar.cybozu.com/k/1"
                        nil))

  (t/testing "parse guest space app"
    (t/are [url parsed] (= (sut/parse-app-url url) parsed)
                        "https://foo.cybozu.com/k/guest/11/1"
                        {:subdomain "foo" :domain "cybozu.com" :guest-space-id "11" :app-id "1"}

                        "https://foo.cybozu.com/k/guest/11/99999999"
                        {:subdomain "foo" :domain "cybozu.com" :guest-space-id "11" :app-id "99999999"}

                        "https://foo.s.cybozu.com/k/guest/11/1"
                        {:subdomain "foo" :domain "cybozu.com" :guest-space-id "11" :app-id "1"}

                        "https://foo-bar.cybozu.com/k/guest/11/1"
                        {:subdomain "foo-bar" :domain "cybozu.com" :guest-space-id "11" :app-id "1"}

                        "https://foo-bar-baz.cybozu.com/k/guest/11/1"
                        {:subdomain "foo-bar-baz" :domain "cybozu.com" :guest-space-id "11" :app-id "1"}

                        "https://foo99.cybozu.com/k/guest/11/1"
                        {:subdomain "foo99" :domain "cybozu.com" :guest-space-id "11" :app-id "1"}

                        "https://foo99.cybozu.com/k/guest/11/1/show"
                        {:subdomain "foo99" :domain "cybozu.com" :guest-space-id "11" :app-id "1"}

                        "https://foo99.cybozu.com/k/guest/11/1/?q=foo%20%3D%20\"1\""
                        {:subdomain "foo99" :domain "cybozu.com" :guest-space-id "11" :app-id "1"}

                        "https://foo_bar.cybozu.com/k/guest/11/1"
                        nil)))

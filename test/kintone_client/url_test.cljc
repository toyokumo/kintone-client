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
(ns kintone-client.url-test
  (:require #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest testing are run-tests]])
            [kintone-client.url :as sut]))

(deftest extract-base-url-test
  (are [url base-url] (= (sut/extract-base-url url) base-url)

                        "https://foo.cybozu.com"
                        "https://foo.cybozu.com"

                        "https://foo.cybozu.com/"
                        "https://foo.cybozu.com"

                        "https://foo.cybozu.com/k"
                        "https://foo.cybozu.com"

                        "https://foo.cybozu.com/k/"
                        "https://foo.cybozu.com"

                        "https://foo.cybozu.com/k/1"
                        "https://foo.cybozu.com"

                        "https://foo.cybozu.com/k/1/"
                        "https://foo.cybozu.com"

                        "https://foo.cybozu.com/k/99999999"
                        "https://foo.cybozu.com"

                        "https://foo.cybozu.com/k/abc"
                        "https://foo.cybozu.com"

                        "https://foo.s.cybozu.com/k/1"
                        "https://foo.s.cybozu.com"

                        "https://foo-bar.cybozu.com/k/1"
                        "https://foo-bar.cybozu.com"

                        "https://foo-bar-baz.cybozu.com/k/1"
                        "https://foo-bar-baz.cybozu.com"

                        "https://foo99.cybozu.com/k/1"
                        "https://foo99.cybozu.com"

                        "https://foo99.cybozu.com/k/1/show"
                        "https://foo99.cybozu.com"

                        "https://foo99.cybozu.com/k/1/?q=foo%20%3D%20\"1\""
                        "https://foo99.cybozu.com"

                        "https://foo.cybozu.com/k/guest/11/1"
                        "https://foo.cybozu.com"

                        "https://foo.cybozu-dev.com/k/1"
                        "https://foo.cybozu-dev.com"

                        "https://foo.kintone.com/k/1"
                        "https://foo.kintone.com"

                        "https://foo.kintone-dev.com/k/1"
                        "https://foo.kintone-dev.com"

                        "https://foo.cybozu.cn/k/1"
                        "https://foo.cybozu.cn"

                        "https://foo.cybozu-dev.cn/k/1"
                        "https://foo.cybozu-dev.cn"

                        "https://foo_bar.cybozu.com/k/1"
                        nil

                        "https://foo.bar.com/baz"
                        nil))

(deftest parse-base-url-test
  (are [url parsed] (= (sut/parse-base-url url) parsed)

                      "https://foo.cybozu.com"
                      {:domain "cybozu.com"
                       :subdomain "foo"}

                      "https://foo.cybozu.com/"
                      {:domain "cybozu.com"
                       :subdomain "foo"}

                      "https://foo.cybozu.com/k"
                      {:domain "cybozu.com"
                       :subdomain "foo"}

                      "https://foo.cybozu.com/k/"
                      {:domain "cybozu.com"
                       :subdomain "foo"}

                      "https://foo.cybozu.com/k/1"
                      {:domain "cybozu.com"
                       :subdomain "foo"}

                      "https://foo.cybozu.com/k/1/"
                      {:domain "cybozu.com"
                       :subdomain "foo"}

                      "https://foo.cybozu.com/k/99999999"
                      {:domain "cybozu.com"
                       :subdomain "foo"}

                      "https://foo.cybozu.com/k/abc"
                      {:domain "cybozu.com"
                       :subdomain "foo"}

                      "https://foo.s.cybozu.com/k/1"
                      {:domain "cybozu.com"
                       :subdomain "foo"}

                      "https://foo-bar.cybozu.com/k/1"
                      {:domain "cybozu.com"
                       :subdomain "foo-bar"}

                      "https://foo-bar-baz.cybozu.com/k/1"
                      {:domain "cybozu.com"
                       :subdomain "foo-bar-baz"}

                      "https://foo99.cybozu.com/k/1"
                      {:domain "cybozu.com"
                       :subdomain "foo99"}

                      "https://foo99.cybozu.com/k/1/show"
                      {:domain "cybozu.com"
                       :subdomain "foo99"}

                      "https://foo99.cybozu.com/k/1/?q=foo%20%3D%20\"1\""
                      {:domain "cybozu.com"
                       :subdomain "foo99"}

                      "https://foo.cybozu.com/k/guest/11/1"
                      {:domain "cybozu.com"
                       :subdomain "foo"}

                      "https://foo.cybozu-dev.com/k/1"
                      {:domain "cybozu-dev.com"
                       :subdomain "foo"}

                      "https://foo.kintone.com/k/1"
                      {:domain "kintone.com"
                       :subdomain "foo"}

                      "https://foo.kintone-dev.com/k/1"
                      {:domain "kintone-dev.com"
                       :subdomain "foo"}

                      "https://foo.cybozu.cn/k/1"
                      {:domain "cybozu.cn"
                       :subdomain "foo"}

                      "https://foo.cybozu-dev.cn/k/1"
                      {:domain "cybozu-dev.cn"
                       :subdomain "foo"}

                      "https://foo_bar.cybozu.com/k/1"
                      nil

                      "https://foo.bar.com/baz"
                      nil))

(deftest valid-base-url?-test
  (are [url res] (= (sut/valid-base-url? url) res)

                   "https://foo.cybozu.com"
                   true

                   "https://foo.cybozu.com/"
                   true

                   "https://foo.cybozu.com/k"
                   true

                   "https://foo.cybozu.com/k/"
                   true

                   "https://foo.cybozu.com/k/1"
                   true

                   "https://foo.cybozu.com/k/1/"
                   true

                   "https://foo.cybozu.com/k/99999999"
                   true

                   "https://foo.cybozu.com/k/abc"
                   true

                   "https://foo.s.cybozu.com/k/1"
                   true

                   "https://foo-bar.cybozu.com/k/1"
                   true

                   "https://foo-bar-baz.cybozu.com/k/1"
                   true

                   "https://foo99.cybozu.com/k/1"
                   true

                   "https://foo99.cybozu.com/k/1/show"
                   true

                   "https://foo99.cybozu.com/k/1/?q=foo%20%3D%20\"1\""
                   true

                   "https://foo.cybozu.com/k/guest/11/1"
                   true

                   "https://foo.cybozu-dev.com/k/1"
                   true

                   "https://foo.kintone.com/k/1"
                   true

                   "https://foo.kintone-dev.com/k/1"
                   true

                   "https://foo.cybozu.cn/k/1"
                   true

                   "https://foo.cybozu-dev.cn/k/1"
                   true

                   "https://foo_bar.cybozu.com/k/1"
                   false

                   "https://foo.bar.com/baz"
                   false))

(deftest extract-app-url-test

  (testing "default space app"
    (are [url app-url] (= (sut/extract-app-url url) app-url)

                         "https://foo.cybozu.com"
                         nil

                         "https://foo.cybozu.com/"
                         nil

                         "https://foo.cybozu.com/k"
                         nil

                         "https://foo.cybozu.com/k/"
                         nil

                         "https://foo.cybozu.com/k/1"
                         "https://foo.cybozu.com/k/1"

                         "https://foo.cybozu.com/k/1/"
                         "https://foo.cybozu.com/k/1"

                         "https://foo.cybozu.com/k/99999999"
                         "https://foo.cybozu.com/k/99999999"

                         "https://foo.cybozu.com/k/abc"
                         nil

                         "https://foo.s.cybozu.com/k/1"
                         "https://foo.s.cybozu.com/k/1"

                         "https://foo-bar.cybozu.com/k/1"
                         "https://foo-bar.cybozu.com/k/1"

                         "https://foo-bar-baz.cybozu.com/k/1"
                         "https://foo-bar-baz.cybozu.com/k/1"

                         "https://foo99.cybozu.com/k/1"
                         "https://foo99.cybozu.com/k/1"

                         "https://foo99.cybozu.com/k/1/show"
                         "https://foo99.cybozu.com/k/1"

                         "https://foo99.cybozu.com/k/1/?q=foo%20%3D%20\"1\""
                         "https://foo99.cybozu.com/k/1"

                         "https://foo.cybozu-dev.com/k/1"
                         "https://foo.cybozu-dev.com/k/1"

                         "https://foo.kintone.com/k/1"
                         "https://foo.kintone.com/k/1"

                         "https://foo.kintone-dev.com/k/1"
                         "https://foo.kintone-dev.com/k/1"

                         "https://foo.cybozu.cn/k/1"
                         "https://foo.cybozu.cn/k/1"

                         "https://foo.cybozu-dev.cn/k/1"
                         "https://foo.cybozu-dev.cn/k/1"

                         "https://foo_bar.cybozu.com/k/1"
                         nil

                         "https://foo.bar.com/baz"
                         nil))

  (testing "guest space app"
    (are [url app-url] (= (sut/extract-app-url url) app-url)

                         "https://foo.cybozu.com/k/guest/11/1"
                         "https://foo.cybozu.com/k/guest/11/1"

                         "https://foo.cybozu.com/k/guest/11/1/"
                         "https://foo.cybozu.com/k/guest/11/1"

                         "https://foo.cybozu.com/k/guest/11/99999999"
                         "https://foo.cybozu.com/k/guest/11/99999999"

                         "https://foo.s.cybozu.com/k/guest/11/1"
                         "https://foo.s.cybozu.com/k/guest/11/1"

                         "https://foo-bar.cybozu.com/k/guest/11/1"
                         "https://foo-bar.cybozu.com/k/guest/11/1"

                         "https://foo-bar-baz.cybozu.com/k/guest/11/1"
                         "https://foo-bar-baz.cybozu.com/k/guest/11/1"

                         "https://foo99.cybozu.com/k/guest/11/1"
                         "https://foo99.cybozu.com/k/guest/11/1"

                         "https://foo99.cybozu.com/k/guest/11/1/show"
                         "https://foo99.cybozu.com/k/guest/11/1"

                         "https://foo99.cybozu.com/k/1/?q=foo%20%3D%20\"1\""
                         "https://foo99.cybozu.com/k/1"

                         "https://foo99.cybozu.com/k/guest/11/1/?q=foo%20%3D%20\"1\""
                         "https://foo99.cybozu.com/k/guest/11/1"

                         "https://foo.cybozu-dev.com/k/guest/11/1"
                         "https://foo.cybozu-dev.com/k/guest/11/1"

                         "https://foo.kintone.com/k/guest/11/1"
                         "https://foo.kintone.com/k/guest/11/1"

                         "https://foo.kintone-dev.com/k/guest/11/1"
                         "https://foo.kintone-dev.com/k/guest/11/1"

                         "https://foo.cybozu.cn/k/guest/11/1"
                         "https://foo.cybozu.cn/k/guest/11/1"

                         "https://foo.cybozu-dev.cn/k/guest/11/1"
                         "https://foo.cybozu-dev.cn/k/guest/11/1"

                         "https://foo_bar.cybozu.com/k/guest/11/1"
                         nil

                         "https://foo.bar.com/k/guest/11/1"
                         nil

                         "https://foo.cybozu.com/k/guest"
                         nil

                         "https://foo.cybozu.com/k/guest/"
                         nil

                         "https://foo.cybozu.com/k/guest/11"
                         nil

                         "https://foo.cybozu.com/k/guest/11/"
                         nil)))

(deftest parse-app-url-test

  (testing "default space app"
    (are [url parsed] (= (sut/parse-app-url url) parsed)
                        "https://foo.cybozu.com"
                        nil

                        "https://foo.cybozu.com/"
                        nil

                        "https://foo.cybozu.com/k"
                        nil

                        "https://foo.cybozu.com/k/"
                        nil

                        "https://foo.cybozu.com/k/1"
                        {:subdomain "foo" :domain "cybozu.com" :app-id "1"}

                        "https://foo.cybozu.com/k/1/"
                        {:subdomain "foo" :domain "cybozu.com" :app-id "1"}

                        "https://foo.cybozu.com/k/99999999"
                        {:subdomain "foo" :domain "cybozu.com" :app-id "99999999"}

                        "https://foo.cybozu.com/k/abc"
                        nil

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

                        "https://foo.cybozu-dev.com/k/1"
                        {:subdomain "foo" :domain "cybozu-dev.com" :app-id "1"}

                        "https://foo.kintone.com/k/1"
                        {:subdomain "foo" :domain "kintone.com" :app-id "1"}

                        "https://foo.kintone-dev.com/k/1"
                        {:subdomain "foo" :domain "kintone-dev.com" :app-id "1"}

                        "https://foo.cybozu.cn/k/1"
                        {:subdomain "foo" :domain "cybozu.cn" :app-id "1"}

                        "https://foo.cybozu-dev.cn/k/1"
                        {:subdomain "foo" :domain "cybozu-dev.cn" :app-id "1"}

                        "https://foo_bar.cybozu.com/k/1"
                        nil

                        "https://foo.bar.com/baz"
                        nil))

  (testing "guest space app"
    (are [url parsed] (= (sut/parse-app-url url) parsed)
                        "https://foo.cybozu.com/k/guest/11/1"
                        {:subdomain "foo" :domain "cybozu.com" :guest-space-id "11" :app-id "1"}

                        "https://foo.cybozu.com/k/guest/11/1/"
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

                        "https://foo.cybozu-dev.com/k/guest/11/1"
                        {:subdomain "foo" :domain "cybozu-dev.com" :guest-space-id "11" :app-id "1"}

                        "https://foo.kintone.com/k/guest/11/1"
                        {:subdomain "foo" :domain "kintone.com" :guest-space-id "11" :app-id "1"}

                        "https://foo.kintone-dev.com/k/guest/11/1"
                        {:subdomain "foo" :domain "kintone-dev.com" :guest-space-id "11" :app-id "1"}

                        "https://foo.cybozu.cn/k/guest/11/1"
                        {:subdomain "foo" :domain "cybozu.cn" :guest-space-id "11" :app-id "1"}

                        "https://foo.cybozu-dev.cn/k/guest/11/1"
                        {:subdomain "foo" :domain "cybozu-dev.cn" :guest-space-id "11" :app-id "1"}

                        "https://foo_bar.cybozu.com/k/guest/11/1"
                        nil

                        "https://foo.bar.com/k/guest/11/1"
                        nil)))

(deftest valid-app-url?-test

  (testing "default space app"
    (are [url res] (= (sut/valid-app-url? url) res)

                     "https://foo.cybozu.com"
                     false

                     "https://foo.cybozu.com/"
                     false

                     "https://foo.cybozu.com/k"
                     false

                     "https://foo.cybozu.com/k/"
                     false

                     "https://foo.cybozu.com/k/1"
                     true

                     "https://foo.cybozu.com/k/99999999"
                     true

                     "https://foo.cybozu.com/k/abc"
                     false

                     "https://foo.s.cybozu.com/k/1"
                     true

                     "https://foo-bar.cybozu.com/k/1"
                     true

                     "https://foo-bar-baz.cybozu.com/k/1"
                     true

                     "https://foo99.cybozu.com/k/1"
                     true

                     "https://foo99.cybozu.com/k/1/show"
                     true

                     "https://foo99.cybozu.com/k/1/?q=foo%20%3D%20\"1\""
                     true

                     "https://foo.cybozu-dev.com/k/1"
                     true

                     "https://foo.kintone.com/k/1"
                     true

                     "https://foo.kintone-dev.com/k/1"
                     true

                     "https://foo.cybozu.cn/k/1"
                     true

                     "https://foo.cybozu-dev.cn/k/1"
                     true

                     "https://foo_bar.cybozu.com/k/1"
                     false

                     "https://foo.bar.com/baz"
                     false))

  (testing "guest space app"
    (are [url res] (= (sut/valid-app-url? url) res)

                     "https://foo.cybozu.com/k/guest/11/1"
                     true

                     "https://foo.cybozu.com/k/guest/11/1"
                     true

                     "https://foo.cybozu.com/k/guest/11/99999999"
                     true

                     "https://foo.s.cybozu.com/k/guest/11/1"
                     true

                     "https://foo-bar.cybozu.com/k/guest/11/1"
                     true

                     "https://foo-bar-baz.cybozu.com/k/guest/11/1"
                     true

                     "https://foo99.cybozu.com/k/guest/11/1"
                     true

                     "https://foo99.cybozu.com/k/guest/11/1/show"
                     true

                     "https://foo99.cybozu.com/k/1/?q=foo%20%3D%20\"1\""
                     true

                     "https://foo99.cybozu.com/k/guest/11/1/?q=foo%20%3D%20\"1\""
                     true

                     "https://foo.cybozu-dev.com/k/guest/11/1"
                     true

                     "https://foo.kintone.com/k/guest/11/1"
                     true

                     "https://foo.kintone-dev.com/k/guest/11/1"
                     true

                     "https://foo.cybozu.cn/k/guest/11/1"
                     true

                     "https://foo.cybozu-dev.cn/k/guest/11/1"
                     true

                     "https://foo_bar.cybozu.com/k/guest/11/1"
                     false

                     "https://foo.bar.com/k/guest/11/1"
                     false

                     "https://foo.cybozu.com/k/guest"
                     false

                     "https://foo.cybozu.com/k/guest/"
                     false

                     "https://foo.cybozu.com/k/guest/11"
                     false

                     "https://foo.cybozu.com/k/guest/11/"
                     false)))

(deftest ->base-url-test
  (are [m base-url] (= (sut/->base-url m) base-url)
                    {:subdomain "foo" :domain "cybozu.com"}
                    "https://foo.cybozu.com"
                    {:subdomain "foo" :domain "cybozu.com" :s? true}
                    "https://foo.s.cybozu.com"
                    ;; invalid
                    {:subdomain "foo"}
                    nil
                    ;; invalid
                    {:subdomain "foo_bar" :domain "cybozu.com"}
                    nil))

(deftest ->app-url-test
  (testing "default space app"
    (are [m app-url] (= (sut/->app-url m) app-url)
                     {:subdomain "foo" :domain "cybozu.com" :app-id "1"}
                     "https://foo.cybozu.com/k/1"
                     ;; s?
                     {:subdomain "foo" :domain "cybozu.com" :app-id "1" :s? true}
                     "https://foo.s.cybozu.com/k/1"
                     ;; invalid
                     {:subdomain "foo" :domain "cybozu.com"}
                     nil
                     ;; invalid
                     {:subdomain "foo_bar" :domain "cybozu.com" :app-id "1"}
                     nil))
  (testing "guest space app"
    (are [m app-url] (= (sut/->app-url m) app-url)
                     {:subdomain "foo" :domain "cybozu.com" :guest-space-id "11" :app-id "1"}
                     "https://foo.cybozu.com/k/guest/11/1"
                     {:subdomain "foo" :domain "cybozu.com" :guest-space-id "11" :app-id "1" :s? true}
                     "https://foo.s.cybozu.com/k/guest/11/1"
                     ;; invalid
                     {:subdomain "foo" :domain "cybozu.com" :guest-space-id "11"}
                     nil
                     ;; invalid
                     {:subdomain "foo_bar" :domain "cybozu.com" :guest-space-id "11" :app-id "1"}
                     nil)))

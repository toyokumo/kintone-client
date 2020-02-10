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
(ns kintone-client.url
  (:require [clojure.string :as str]))

(def domain-list
  ["cybozu.com"
   "cybozu-dev.com"
   "kintone.com"
   "kintone-dev.com"
   "cybozu.cn"
   "cybozu-dev.cn"])

(def ^:private re-base-url*
  (str "^https://([a-zA-Z0-9][a-zA-Z0-9\\-]{1,30}[a-zA-Z0-9])(?:\\.s)?\\."
       "("
       (->> (map #(str/replace % "." "\\.") domain-list)
            (str/join "|"))
       ")"))

(def ^:private re-base-url
  (re-pattern re-base-url*))

(defn extract-base-url
  "
  (extract-base-url \"https://hoge.cybozu.com\")\n=> \"https://hoge.cybozu.com\"
  (extract-base-url \"https://hoge.cybozu.com/k/12\")\n=> \"https://hoge.cybozu.com\"\n
  (extract-base-url \"https://foo.s.cybozu.com/k/guest/11/1\")\n=> \"https://foo.s.cybozu.com\"\n
  (extract-base-url \"https://hoge.hoge.com/k/11\")\n=> nil
  "
  [url]
  (some-> (re-find re-base-url url) first))

(comment
 (extract-base-url "https://hoge.cybozu.com")
 (extract-base-url "https://hoge.cybozu.com/k/12")
 (extract-base-url "https://foo.s.cybozu.com/k/guest/11/1")
 (extract-base-url "https://hoge.hoge.com/k/11"))

(defn parse-base-url
  "
  (parse-base-url \"https://hoge.cybozu.com\")\n=> {:domain \"cybozu.com\", :subdomain \"hoge\"}
  (parse-base-url \"https://hoge.cybozu.com/k/12\")\n=> {:domain \"cybozu.com\", :subdomain \"hoge\"}\n
  (parse-base-url \"https://foo.s.cybozu.com/k/guest/11/1\")\n=> {:domain \"cybozu.com\", :subdomain \"foo\"}\n
  (parse-base-url \"https://hoge.hoge.com/k/11\")\n=> nil
  "
  [url]
  (when-let [[_ subdomain domain] (re-find re-base-url url)]
    {:domain domain
     :subdomain subdomain}))

(comment
 (parse-base-url "https://hoge.cybozu.com")
 (parse-base-url "https://hoge.cybozu.com/k/12")
 (parse-base-url "https://foo.s.cybozu.com/k/guest/11/1")
 (parse-base-url "https://hoge.hoge.com/k/11"))

(defn valid-base-url?
  "
  (valid-base-url? \"https://hoge.cybozu.com\")\n=> true
  (valid-base-url? \"https://hoge.cybozu.com/k/12\")\n=> true\n
  (valid-base-url? \"https://foo.s.cybozu.com/k/guest/11/1\")\n=> true\n
  (valid-base-url? \"https://hoge.hoge.com/k/11\")\n=> false
  "
  [url]
  (not (str/blank? (extract-base-url url))))

(comment
 (valid-base-url? "https://hoge.cybozu.com")
 (valid-base-url? "https://hoge.cybozu.com/k/12")
 (valid-base-url? "https://foo.s.cybozu.com/k/guest/11/1")
 (valid-base-url? "https://hoge.hoge.com/k/11"))

(def ^:private re-app-url
  (re-pattern (str re-base-url* "/k/(\\d+)")))

(def ^:private re-guest-app-url
  (re-pattern (str re-base-url* "/k/guest/(\\d+)/(\\d+)")))

(defn extract-app-url
  "
  (extract-app-url \"https://hoge.cybozu.com\")\n=> nil\n
  (extract-app-url \"https://hoge.cybozu.com/k/12\")\n=> \"https://hoge.cybozu.com/k/12\"\n
  (extract-app-url \"https://foo.s.cybozu.com/k/guest/11/1\")\n=> \"https://foo.s.cybozu.com/k/guest/11/1\"\n
  (extract-app-url \"https://hoge.hoge.com/k/11\")\n=> nil
  "
  [url]
  (or (some-> (re-find re-app-url url) first)
      (some-> (re-find re-guest-app-url url) first)))

(comment
 (extract-app-url "https://hoge.cybozu.com")
 (extract-app-url "https://hoge.cybozu.com/k/12")
 (extract-app-url "https://foo.s.cybozu.com/k/guest/11/1")
 (extract-app-url "https://hoge.hoge.com/k/11"))

(defn parse-app-url
  "
  (parse-app-url \"https://hoge.cybozu.com\")\n=> nil\n
  (parse-app-url \"https://hoge.cybozu.com/k/12\")\n=> {:domain \"cybozu.com\", :subdomain \"hoge\", :app-id \"12\"}\n
  (parse-app-url \"https://foo.s.cybozu.com/k/guest/11/1\")\n=> {:domain \"cybozu.com\", :subdomain \"foo\", :guest-space-id \"11\", :app-id \"1\"}\n
  (parse-app-url \"https://hoge.hoge.com/k/11\")\n=> nil
  "
  [url]
  (or
   (when-let [[_ subdomain domain app-id] (re-find re-app-url url)]
     {:domain domain
      :subdomain subdomain
      :app-id app-id})
   (when-let [[_ subdomain domain guest-space-id app-id] (re-find re-guest-app-url url)]
     {:domain domain
      :subdomain subdomain
      :guest-space-id guest-space-id
      :app-id app-id})))

(comment
 (parse-app-url "https://hoge.cybozu.com")
 (parse-app-url "https://hoge.cybozu.com/k/12")
 (parse-app-url "https://foo.s.cybozu.com/k/guest/11/1")
 (parse-app-url "https://hoge.hoge.com/k/11"))

(defn valid-app-url?
  "
  (valid-app-url? \"https://hoge.cybozu.com\")\n=> false\n
  (valid-app-url? \"https://hoge.cybozu.com/k/12\")\n=> true\n
  (valid-app-url? \"https://foo.s.cybozu.com/k/guest/11/1\")\n=> true\n
  (valid-app-url? \"https://hoge.hoge.com/k/11\")\n=> false
  "
  [url]
  (some? (or (re-find re-app-url url)
             (re-find re-guest-app-url url))))

(comment
 (valid-app-url? "https://hoge.cybozu.com")
 (valid-app-url? "https://hoge.cybozu.com/k/12")
 (valid-app-url? "https://foo.s.cybozu.com/k/guest/11/1")
 (valid-app-url? "https://hoge.hoge.com/k/11"))

(defn ->base-url
  "generates kintone base url. returns nil if input data is not enough or generated app url is invalid."
  [{:keys [domain subdomain s?]}]
  (when (and domain subdomain)
    (let [base-url (str "https://" subdomain "." (when s? "s.") domain)]
      (when (valid-base-url? base-url)
        base-url))))

(defn ->app-url
  "generates kintone app url. returns nil if input data is not enough or generated app url is invalid."
  [{:keys [domain subdomain guest-space-id app-id s?]}]
  (when (and domain subdomain app-id)
    (let [app-url (if guest-space-id
                    (str "https://" subdomain "." (when s? "s.") domain "/k/guest/" guest-space-id "/" app-id)
                    (str "https://" subdomain "." (when s? "s.") domain "/k/" app-id))]
      (when (valid-app-url? app-url)
        app-url))))
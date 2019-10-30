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
(ns kintone.url
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

(defn extract-base-url [url]
  (some-> (re-find re-base-url url) first))

(defn parse-base-url [url]
  (when-let [[_ subdomain domain] (re-find re-base-url url)]
    {:domain domain
     :subdomain subdomain}))

(defn valid-base-url? [url]
  (not (str/blank? (extract-base-url url))))

(def ^:private re-app-url
  (re-pattern (str re-base-url* "/k/(\\d++)")))

(def ^:private re-guest-app-url
  (re-pattern (str re-base-url* "/k/guest/(\\d++)/(\\d++)")))

(defn extract-app-url [url]
  (or (some-> (re-find re-app-url url) first)
      (some-> (re-find re-guest-app-url url) first)))

(defn parse-app-url [url]
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

(defn valid-app-url? [url]
  (some? (or (re-find re-app-url url)
             (re-find re-guest-app-url url))))

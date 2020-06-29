# kintone-client

A [kintone](https://www.kintone.com) client for Clojure and ClojureScript.

[![CircleCI](https://circleci.com/gh/toyokumo/kintone-client.svg?style=svg)](https://circleci.com/gh/toyokumo/kintone-client)
[![cljdoc badge](https://cljdoc.org/badge/toyokumo/kintone-client)](https://cljdoc.org/d/toyokumo/kintone-client/CURRENT)
[![Clojars Project](https://img.shields.io/clojars/v/toyokumo/kintone-client.svg)](https://clojars.org/toyokumo/kintone-client)

## Overview

The SDK provides an easy way to use kintone API from Clojure or ClojureScript.

Every API run asynchronously to use [clj-http](https://github.com/dakrone/clj-http) for Clojure
, [cljs-ajax](https://github.com/JulianBirch/cljs-ajax) for ClojureScript and
[core.async](https://github.com/clojure/core.async).
They return a channel of core.async.

- `kintone-client.authentication` : Make Auth object.
- `kintone-client.connection` : Make connection object.
- `kintone-client.types` : Type definitions such as response object.
- `kintone-client.record` : kintone REST Record API.
- `kintone-client.app` : kintone REST App API.
- `kintone-client.user`: cybozu.com User API. (API Tokens cannot be used with user API.)
- `kintone-client.url`: kintone url utilities.

## Usage

Follow bellow the steps.

1. Make `Auth` object
1. Make `Connection` object
1. Call API with `Connection` object

### Make `Auth` object

`Auth` object is used in order to make `Connection` object.

This step is not necessary in case that you run JavaScript(ClojureScript)
on kintone as customize script for kintone app or portal.

```clojure
(require '[kintone-client.authentication :as auth])

;; API token
(auth/new-auth {:api-token "xyz..."})

;; Basic authentication and password authentication
(auth/new-auth {:basic {:username "basic-username" :password "basic-password"}
                :password {:username "login-name" :password "login-password"}})

;; Basic authentication, password authentication and API token
;; In this case, the API token is going to be ignored,
;; and the basic authentication and the password authentication is going to be used.
(auth/new-auth {:basic {:username "basic-username" :password "basic-password"}
                :password {:username "login-name" :password "login-password"}
                :api-token "xyz..."})
```

### Make `Connection` object

```clojure
(require '[kintone-client.authentication :as auth])
(require '[kintone-client.connection :as conn])

;; Auth and domain
(conn/new-connection {:auth (auth/new-auth {:api-token "xyz.."})
                      :domain "sample.kintone.com"})

;; To guest space
(conn/new-connection {:auth (auth/new-auth {:api-token "xyz.."})
                      :domain "sample.kintone.com"
                      :guest-space-id 1})

;; It is only accepted on ClojureScript that there is no auth.
(conn/new-connection {:domain "sample.cybozu.com"})
```

### Call API

You should use `Connection` object as the first argument on every API call.

```clojure
(require '[kintone-client.authentication :as auth])
(require '[kintone-client.connection :as conn])
(require '[kintone-client.record :as record])

;; Clojure
(require '[clojure.core.async :refer [<!!]])

(def conn (conn/new-connection {:auth (auth/new-auth {:api-token "xyz.."})
                                :domain "sample.kintone.com"}))

(let [app 1111
      id 1
      ;; Block the thread and get response
      res (<!! (record/get-record conn app id))]
  ;; Every API response is kintone-client.types/KintoneResonse
  (if (:err res)
    (log/error "Something bad happen")
    (:res res)))
;; success => {:record {:$id {:type "__ID__", :value "1"} ...}
;; fail => {:status 404
;;          :status-text "Not Found"
;;          :failure :error
;;          :response {:code "GAIA_RE01" ...}}


;; ClojureScript
(require '[cljs.core.async :refer [<!] :refer-macros [go]])

;; If you do not pass the domain string, (.-hostname js/location) will be used.
(def conn (conn/new-connection {:auth (auth/new-auth {:api-token "xyz.."})}))

;; Call in go block to handle response
(go
  (let [app 1111
        id 1
        res (<! (record/get-record conn app id))]
    (if (:err res)
      (:err res)
      (:res res))))
```

### bulk request
You can do insert, update, delete at once with `bulk-request`.
`bulk-request` can be performed among different apps.
```clojure
(<!! (r/bulk-request conn
                     [ ;; NOTE: conn is omitted here.
                      (r/add-record app {:name {:value "foo"}})
                      (r/update-record app
                                       {:id id
                                        :record {:name {:value "foo"}}})
                      (r/delete-records app [1 2 3])]))
```
`bulk-request` can be used with:
- add-record
- add-records
- update-record
- update-records
- delete-records
- delete-records-with-revision
- update-record-status
- update-records-status

### url utilities
```clojure
(extract-base-url "https://hoge.cybozu.com/k/12")
;; => "https://hoge.cybozu.com"
(parse-base-url "https://hoge.kintone.com/k/12")
;; => {:domain "kintone.com", :subdomain "hoge"}
(valid-base-url? "https://hoge.cybozu.com/k/12")
;; => true
(extract-app-url "https://hoge.cybozu.com/k/12/show")
;; => "https://hoge.cybozu.com/k/12"
(parse-app-url "https://foo.s.cybozu.com/k/guest/11/1")
;; => {:domain "cybozu.com", :subdomain "foo", :guest-space-id "11", :app-id "1"}
(valid-app-url? "https://hoge.cybozu.com")
;; => true
```

For more information, See [API documents](https://cljdoc.org/d/toyokumo/kintone-client/CURRENT), `test/`, and `dev/test.clj`.

## dev/test.clj
These tests actually interact with a kintone app and space (not included in CI).
These are good examples of the usage of kintone-client.

### How to run
- import dev-resources/kintone-clj-test.zip
- create kintone space (the tests create many apps in this space)
- fill dev-resources/config.edn
```edn
{:auth {:basic {:username "username" :password "password"}
        :api-token "api-token"}
 :domain "domain"
 :app 9999
 :space 99}
```
- try tests

## Note
- kintone-client doesn't convert a camelCase keyword (in kintone REST api response map) into kebab-case.
It costs too much to do so (response map can be so complicated, and keyword can be non-ascii character).

## License

```
Copyright 2019 TOYOKUMO,Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

For src/kintone_client/url.cljc and test/kintone_client/url_test.cljc:

```
MIT License

Copyright (c) 2017 ayato-p

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

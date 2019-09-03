# kintone-clj

A [kintone](https://www.kintone.com) SDK for Clojure and ClojureScript.

[![CircleCI](https://circleci.com/gh/toyokumo/kintone-clj.svg?style=svg)](https://circleci.com/gh/toyokumo/kintone-clj)

[![cljdoc badge](https://cljdoc.org/badge/toyokumo/kintone-clj)](https://cljdoc.org/d/toyokumo/kintone-clj/CURRENT)

## Overview

The SDK provides an easy way to use kintone API from Clojure or ClojureScript.

Every API run asynchronously to use [clj-http](https://github.com/dakrone/clj-http) for Clojure
, [cljs-ajax](https://github.com/JulianBirch/cljs-ajax) for ClojureScript and
[core.async](https://github.com/clojure/core.async).
They return a channel of core.async.

- `kintone.authentication` : Make Auth object.
- `kintone.connection` : Make connection object.
- `kintone.types` : Type definitions such as response object.
- `kintone.record` : kintone REST Record API.

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
(require '[kintone.authentication :as auth])

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
(require '[kintone.authentication :as auth])
(require '[kintone.connection :as conn])

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
(require '[kintone.authentication :as auth])
(require '[kintone.connection :as conn])
(require '[kintone.record :as record])

;; Clojure
(require '[clojure.core.async :refer [<!!]])

(def conn (conn/new-connection {:auth (auth/new-auth {:api-token "xyz.."})
                                :domain "sample.kintone.com"}))

(let [app 1111
      id 1
      ;; Block the thread and get response
      res (<!! (record/get-record conn app id))]
  ;; Every API response is kintone.types/KintoneResonse
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

For more information, See [API documents](https://cljdoc.org/d/toyokumo/kintone-clj/CURRENT).

## License

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

(ns kintone-client.test-helper
  (:require
   #?@(:clj  [[clojure.core.async :refer [<!! chan put!]]]
       :cljs [[cljs.core.async :refer [<! chan put!] :refer-macros [go]]])
   [kintone-client.authentication :as auth]
   [kintone-client.connection :as conn]
   [kintone-client.protocols :as pt]
   [kintone-client.types :as t]))

(def ^:private auth
  (auth/new-auth {:api-token "MyToken"}))

(def ^:private conn
  (conn/new-connection {:auth auth
                        :domain "test.kintone.com"}))

(defn- fake-url [path]
  (pt/-url conn path))

(defn- fake-user-api-url [path]
  (pt/-user-api-url conn path))

(def fake-conn
  (reify pt/IRequest
    (-path [_ path]
      (str "/k" path))
    (-url [_ path]
      (fake-url path))
    (-user-api-url [_ path]
      (fake-user-api-url path))
    (-get [_ url req]
      (let [c (chan)]
        (put! c (t/->KintoneResponse {:url url :req req} nil))
        c))
    (-post [_ url req]
      (let [c (chan)]
        (put! c (t/->KintoneResponse {:url url :req req} nil))
        c))
    (-put [_ url req]
      (let [c (chan)]
        (put! c (t/->KintoneResponse {:url url :req req} nil))
        c))
    (-delete [_ url req]
      (let [c (chan)]
        (put! c (t/->KintoneResponse {:url url :req req} nil))
        c))
    (-get-blob [_ url req]
      (let [c (chan)]
        (put! c (t/->KintoneResponse {:url url :req req} nil))
        c))
    (-multipart-post [_ url req]
      (let [c (chan)]
        (put! c (t/->KintoneResponse {:url url :req req} nil))
        c))))

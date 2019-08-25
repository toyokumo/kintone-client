(ns kintone.connection
  "Connection object has connection information to call kintone API"
  (:require [ajax.core :as ajax]
            #?(:clj [clojure.core.async :refer [chan put!]]
               :cljs [cljs.core.async :refer [chan put!]])
            [kintone.protocols :as pt]
            [kintone.types :as t]))

(def ^:dynamic *default-req*
  {:format :json
   :response-format :json
   :keywords? true
   :timeout (* 1000 30)})

(defn- ^:dynamic handler [channel res]
  (put! channel (t/->KintoneResponse res nil)))

(defn- ^:dynamic err-handler [channel err]
  (put! channel (t/->KintoneResponse nil err)))

(defn- ^:dynamic build-req [auth req channel]
  (let [headers (merge (pt/-header auth) (:headers req))]
    (merge *default-req*
           {:headers headers
            :params (:params req)
            :handler (partial handler channel)
            :error-handler (partial err-handler channel)})))

(deftype Connection
  [auth domain guest-space-id
   timeout headers]
  pt/IRequest
  (-path [_ path]
    (if (seq guest-space-id)
      (str "/k/guest/" guest-space-id path)
      (str "/k" path)))
  (-url [this path]
    (str "https://" domain (pt/-path this path)))
  (-get [this url req]
   ;; Use POST method to pass the URL bytes limitation.
    (pt/-post this url (assoc-in req [:headers "X-HTTP-Method-Override"] "GET")))
  (-post [_ url req]
    (let [c (chan)
          req (build-req auth req c)]
      (ajax/POST url req)
      c))
  (-put [_ url req]
    (let [c (chan)
          req (build-req auth req c)]
      (ajax/PUT url req)
      c))
  (-delete [_ url req]
    (let [c (chan)
          req (build-req auth req c)]
      (ajax/DELETE url req)
      c)))

(defn new-connection
  "Make a new connection object.

  :auth - kintone.authentication/Auth object
          required

  :domain - kintone domain name string
            required
            e.g. sample.kintone.com or sample.cybozu.com, etc..

  :guest-space-id - kintone guest space id
                    integer, optional

  See: https://github.com/JulianBirch/cljs-ajax

  :timeout - The ajax call's timeout.
             integer, milliseconds
             optional, default 30s

  :headers - map, optional"
  [{:keys [auth domain guest-space-id
           timeout headers]}]
  (->Connection auth domain guest-space-id
                timeout headers))

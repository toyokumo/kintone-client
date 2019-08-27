(ns kintone.connection
  "Connection object has connection information to call kintone API"
  (:require #?(:clj [clj-http.client :as client]
               :cljs [ajax.core :as ajax])
            #?(:clj [clojure.core.async :refer [chan put!]]
               :cljs [cljs.core.async :refer [chan put!]])
            [kintone.protocols :as pt]
            [kintone.types :as t])
  #?(:clj (:import (clojure.lang ExceptionInfo)
                   (java.lang Exception))))

(def ^:dynamic *default-req*
  #?(:clj {:accept :json
           :content-type :json
           :async? true
           :as :json
           :coerce :always
           :connection-timeout (* 1000 10)
           :socket-timeout (* 1000 30)}
     :cljs {:format :json
            :response-format :json
            :keywords? true
            :timeout (* 1000 30)}))

(defn- ^:dynamic handler [channel res]
  #?(:clj (put! channel (t/->KintoneResponse (:body res) nil))
     :cljs (put! channel (t/->KintoneResponse res nil))))

#?(:clj
   (defn- ^:dynamic format-err [err]
     (cond
       (instance? ExceptionInfo err)
       (let [{:keys [status body]} (ex-data err)]
         {:status status
          :status-text (str status)
          :response body})

       (instance? Exception err)
       {:status -1
        :status-text (.getMessage err)
        :response nil}

       :else
       err)))

(defn- ^:dynamic err-handler [channel err]
  #?(:clj (put! channel (t/->KintoneResponse nil (format-err err)))
     :cljs (put! channel (t/->KintoneResponse nil err))))

#?(:clj
   (defn- ^:dynamic build-req [auth req _]
     (let [headers (merge (pt/-header auth) (:headers req))]
       (merge *default-req*
              {:headers headers
               :form-params (:params req)}))))

#?(:cljs
   (defn- ^:dynamic build-req [auth req channel]
     (let [headers (merge (pt/-header auth) (:headers req))]
       (merge *default-req*
              {:headers headers
               :params (:params req)
               :handler (partial handler channel)
               :error-handler (partial err-handler channel)}))))

(defrecord Connection
  [auth domain guest-space-id
   connection-timeout socket-timeout timeout
   headers]
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
      #?(:clj (client/post url req (partial handler c) (partial err-handler c))
         :cljs (ajax/POST url req))
      c))
  (-put [_ url req]
    (let [c (chan)
          req (build-req auth req c)]
      #?(:clj (client/put url req (partial handler c) (partial err-handler c))
         :cljs (ajax/PUT url req))
      c))
  (-delete [_ url req]
    (let [c (chan)
          req (build-req auth req c)]
      #?(:clj (client/delete url req (partial handler c) (partial err-handler c))
         :cljs (ajax/DELETE url req))
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

  See: https://github.com/dakrone/clj-http or https://github.com/JulianBirch/cljs-ajax

  :Connection-timeout - The time to wait for establishing
                        the connection with the remote host.
                        integer, milliseconds, Only for Clojure
                        optional, default 10s

  :socket-timeout - The time to wait for getting data
                    after the connection established.
                    integer, milliseconds, Only for Clojure
                    optional, default 30s

  :timeout - The ajax call's timeout.
             integer, milliseconds
             optional, default 30s, Only for ClojureScript

  :headers - map, optional"
  [{:keys [auth domain guest-space-id
           connection-timeout socket-timeout timeout
           headers]}]
  (->Connection auth domain guest-space-id
                connection-timeout socket-timeout timeout
                headers))

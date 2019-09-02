(ns kintone.connection
  "Connection object has connection information to call kintone API"
  (:require #?(:clj [clj-http.client :as client]
               :cljs [ajax.core :as ajax])
            #?(:clj [clojure.core.async :refer [chan put! thread]]
               :cljs [cljs.core.async :refer [chan put!]])
            [kintone.protocols :as pt]
            [kintone.types :as t])
  #?(:clj (:import (clojure.lang ExceptionInfo)
                   (java.lang Exception))))

(def ^:dynamic ^:private *default-req*
  "Default request parameters."
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
        :response err}

       :else
       err)))

(defn- ^:dynamic err-handler [channel err]
  #?(:clj (put! channel (t/->KintoneResponse nil (format-err err)))
     :cljs (put! channel (t/->KintoneResponse nil err))))

#?(:clj
   (defn- ^:dynamic build-req
     [{:keys [auth connection-timeout socket-timeout headers]} req _]
     (cond-> (assoc *default-req*
                    :headers (merge (pt/-header auth)
                                    headers
                                    (:headers req)))
       connection-timeout (assoc :connection-timeout connection-timeout)
       socket-timeout (assoc :socket-timeout socket-timeout)
       (:params req) (assoc :form-params (:params req)))))

#?(:cljs
   (defn- ^:dynamic build-req
     [{:keys [auth timeout headers]} req channel]
     (cond-> (assoc *default-req*
                    :headers (merge (pt/-header auth)
                                    headers
                                    (:headers req))
                    :handler (partial handler channel)
                    :error-handler (partial err-handler channel))
       timeout (assoc :timeout timeout)
       (:params req) (assoc :params (:params req)))))

(defn- post-as-get [req]
  (assoc-in req [:headers "X-HTTP-Method-Override"] "GET"))

(defrecord Connection
  [auth domain guest-space-id
   connection-timeout socket-timeout timeout
   headers]
  pt/IRequest
  (-path [_ path]
    (if guest-space-id
      (str "/k/guest/" guest-space-id path)
      (str "/k" path)))
  (-url [this path]
    (str "https://" domain (pt/-path this path)))
  (-get [this url req]
   ;; Use POST method to pass the URL bytes limitation.
    (pt/-post this url (post-as-get req)))
  (-post [this url req]
    (let [c (chan)
          req (build-req this req c)]
      #?(:clj (client/post url req (partial handler c) (partial err-handler c))
         :cljs (ajax/POST url req))
      c))
  (-put [this url req]
    (let [c (chan)
          req (build-req this req c)]
      #?(:clj (client/put url req (partial handler c) (partial err-handler c))
         :cljs (ajax/PUT url req))
      c))
  (-delete [this url req]
    (let [c (chan)
          req (build-req this req c)]
      #?(:clj (client/delete url req (partial handler c) (partial err-handler c))
         :cljs (ajax/DELETE url req))
      c))
  (-get-blob [this url req]
    (let [c (chan)
          req (post-as-get req)
          req #?(:clj (-> (build-req this req c)
                          (dissoc :accept :as :coerce))
                 :cljs (-> (build-req this req c)
                           (dissoc :format)
                           (assoc :response-format (ajax/raw-response-format))))]
      #?(:clj (client/post url req (partial handler c) (partial err-handler c))
         :cljs (ajax/POST url req))
      c))
  (-multipart-post [this url req]
    (let [c (chan)
          req (assoc req :json-req? false?)
          req #?(:clj (-> (build-req this req c)
                          (dissoc :content-type :async?)
                          (assoc :multipart (:multipart req)))
                 :cljs (-> (build-req this req c)
                           (dissoc :format)
                           (assoc :body (:multipart req))))]
      #?(:clj (thread
               (try
                 (handler c (client/post url req))
                 (catch ExceptionInfo e
                   (err-handler c e))
                 (catch Exception e
                   (err-handler c e))))
         :cljs (ajax/POST url req))
      c)))

(defn new-connection
  "Make a new connection object.

  :auth - kintone.authentication/Auth object
          required

  :domain - kintone domain name string
            required(Clojure), optional(ClojureScript)
            e.g. sample.kintone.com or sample.cybozu.com, etc..

  :guest-space-id - kintone guest space id
                    integer, optional

  See: https://github.com/dakrone/clj-http or https://github.com/JulianBirch/cljs-ajax

  :connection-timeout - The time to wait for establishing
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
  (let [domain #?(:clj domain
                  :cljs (or domain (.-hostname js/location)))]
    (->Connection auth domain guest-space-id
                  connection-timeout socket-timeout timeout
                  headers)))

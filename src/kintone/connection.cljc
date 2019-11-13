(ns kintone.connection
  "Connection object has connection information to call kintone API"
  (:require #?(:clj [cheshire.core :as json])
            #?(:clj [clj-http.client :as client]
               :cljs [ajax.core :as ajax])
            #?(:clj [clojure.core.async :refer [chan put! thread]]
               :cljs [cljs.core.async :refer [chan put!]])
            #?(:clj [clojure.string :as str])
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
           :coerce :unexceptional
           :connection-timeout (* 1000 10)
           :socket-timeout (* 1000 30)}
     :cljs {:format :json
            :response-format :json
            :keywords? true
            :timeout (* 1000 30)}))

(defn- default-handler [channel res]
  #?(:clj (put! channel (t/->KintoneResponse (:body res) nil))
     :cljs (put! channel (t/->KintoneResponse res nil))))

(defn- wrap-handler [handler channel]
  (fn [res]
    (put! channel (t/->KintoneResponse (handler res) nil))))

(defn- ->handler [handler channel]
  (if handler
    (wrap-handler handler channel)
    (partial default-handler channel)))

#?(:clj
   (defn- format-err [err]
     (cond
       (instance? ExceptionInfo err)
       (let [{:keys [status headers body]} (ex-data err)]
         {:status status
          :status-text (str status)
          ;; kintone API sometimes returns HTML, for example basic authentication fails
          :response (if (some-> (get headers "Content-Type")
                                str/lower-case
                                (str/starts-with? "application/json"))
                      (json/parse-string body true)
                      body)})

       (instance? Exception err)
       {:status -1
        :status-text (.getMessage err)
        :response err}

       :else
       err)))

(defn- default-err-handler [channel err]
  #?(:clj (put! channel (t/->KintoneResponse nil (format-err err)))
     :cljs (put! channel (t/->KintoneResponse nil err))))

(defn- wrap-error-handler [error-handler channel]
  (fn [err]
    (put! channel (t/->KintoneResponse nil (error-handler err)))))

(defn- ->error-handler [error-handler channel]
  (if error-handler
    (wrap-error-handler error-handler channel)
    (partial default-err-handler channel)))

#?(:clj
   (defn- build-req
     [{:keys [auth connection-timeout socket-timeout headers]} req _]
     (cond-> (assoc *default-req*
                    :headers (merge (pt/-header auth)
                                    headers
                                    (:headers req)))
       connection-timeout (assoc :connection-timeout connection-timeout)
       socket-timeout (assoc :socket-timeout socket-timeout)
       (:params req) (assoc :form-params (:params req)))))

#?(:cljs
   (defn- build-req
     [{:keys [auth timeout headers handler error-handler]} req channel]
     (cond-> (assoc *default-req*
                    :headers (merge (pt/-header auth)
                                    headers
                                    (:headers req))
                    :handler (->handler handler channel)
                    :error-handler (->error-handler error-handler channel))
       timeout (assoc :timeout timeout)
       (:params req) (assoc :params (:params req)))))

(defn- post-as-get [req]
  (assoc-in req [:headers "X-HTTP-Method-Override"] "GET"))

(defrecord Connection
  [auth domain guest-space-id
   handler error-handler
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
      #?(:clj (client/post url req (->handler handler c) (->error-handler error-handler c))
         :cljs (ajax/POST url req))
      c))
  (-put [this url req]
    (let [c (chan)
          req (build-req this req c)]
      #?(:clj (client/put url req (->handler handler c) (->error-handler error-handler c))
         :cljs (ajax/PUT url req))
      c))
  (-delete [this url req]
    (let [c (chan)
          req (build-req this req c)]
      #?(:clj (client/delete url req (->handler handler c) (->error-handler error-handler c))
         :cljs (ajax/DELETE url req))
      c))
  (-get-blob [this url req]
    (let [c (chan)
          req (post-as-get req)
          req #?(:clj (-> (build-req this req c)
                          (dissoc :accept :coerce)
                          (assoc :as (or (:as req) :byte-array)))
                 :cljs (-> (build-req this req c)
                           (dissoc :format)
                           (assoc :response-format (ajax/raw-response-format))))]
      #?(:clj (client/post url req (->handler handler c) (->error-handler error-handler c))
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
                           (assoc :body (:multipart req))))
          handler (->handler handler c)
          error-handler (->error-handler error-handler c)]
      #?(:clj (thread
               (try
                 (handler (client/post url req))
                 (catch ExceptionInfo e
                   (error-handler e))
                 (catch Exception e
                   (error-handler e))))
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

  :handler - The handler function for successful operation should accept a single parameter
             which is the response. If you do not provide this,
             the default-handler above will be called instead.
             The value it returns will put into the channel, which is the return value.
             function, optional

  :error-handler - The handler function for error operation should accept a single parameter
                   which is the response. If you do not provide this,
                   the default-error-handler above will be called instead.
                   The value it returns will put into the channel, which is the return value.
                   function, optional

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
           handler error-handler
           connection-timeout socket-timeout timeout
           headers]}]
  (let [domain #?(:clj domain
                  :cljs (or domain (.-hostname js/location)))]
    (->Connection auth domain guest-space-id
                  handler error-handler
                  connection-timeout socket-timeout timeout
                  headers)))

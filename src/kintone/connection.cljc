(ns kintone.connection
  "Connection object has connection information to call kintone API"
  (:require #?(:clj [clojure.core.async :refer [chan put!]]
               :cljs [cljs.core.async :refer [chan put!]])
            [kintone.protocols :as pt]
            #?(:clj [clj-http.client :as client]
               :cljs [ajax.core :as ajax])))

(def ^:dynamic *default-req*
  #?(:clj {:accept :json
           :content-type :json
           :async? true
           :as :json
           :connection-timeout (* 1000 10)
           :socket-timeout (* 1000 30)
           :coerce :unexceptional}
     :cljs {:format :json
            :response-format :json
            :keywords? true
            :timeout 30}))

#?(:clj
   (defn- build-req [auth req]
     (let [headers (merge (pt/-header auth) (:headers req))]
       (merge *default-req*
              {:headers headers
               :form-params (:params req)}))))

#?(:cljs
   (defn- build-req [auth req channel]
     (let [headers (merge (pt/-header auth) (:headers req))]
       (merge *default-req*
              {:headers headers
               :params (:params req)
               :handler #(put! channel %)
               :error-handler #(put! channel %)}))))

(deftype Connection
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
          req #?(:clj (build-req auth req)
                 :cljs (build-req auth req c))]
      #?(:clj (client/post url req #(put! c (:body %)) #(put! c %))
         :cljs (ajax/POST url req))
      c))
  (-put [_ url req]
    (let [c (chan)
          req #?(:clj (build-req auth req)
                 :cljs (build-req auth req c))]
      #?(:clj (client/put url req #(put! c (:body %)) #(put! c %))
         :cljs (ajax/PUT url req))
      c))
  (-delete [_ url req]
    (let [c (chan)
          req #?(:clj (build-req auth req)
                 :cljs (build-req auth req c))]
      #?(:clj (client/delete url req #(put! c (:body %)) #(put! c %))
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

  :connection-timeout - The time to wait for establishing
                        the connection with the remote host.
                        integer, milliseconds, Only for Clojure
                        optional, default 10s

  :socket-timeout - The time to wait for getting data
                    after the connection established.
                    integer, milliseconds, Only for Clojure
                    optional, default 30s

  :timeout - The ajax call's timeout.
             integer, milliseconds, Only for ClojureScript
             optional, default 30s

  :headers - map, optional"
  [{:keys [auth domain guest-space-id
           connection-timeout socket-timeout timeout
           headers]}]
  (->Connection auth domain guest-space-id
                connection-timeout socket-timeout timeout
                headers))

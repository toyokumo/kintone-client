(ns kintone-client.authentication
  "Authentication module is used to make Connection object.
  This module allows authenticating with the Kintone app
  by password authenticator or API token authenticator.
  This module is also supported the basic authenticator"
  (:require [kintone-client.protocols :as pt]
            #?(:cljs [goog.crypt.base64 :as goog.base64]))
  #?(:clj (:import java.util.Base64)))

(defn- base64-encode [s]
  #?(:clj (.encodeToString (Base64/getEncoder) (.getBytes s))
     :cljs (goog.base64/encodeString s)))

(defrecord Auth [basic password api-token]
  pt/IAuth
  (-header [_]
   (cond-> {}
     basic (assoc "Authorization" (str "Basic " basic))
     password (assoc "X-Cybozu-Authorization" password)
     api-token (assoc "X-Cybozu-API-Token" api-token))))

(defn new-auth
  "Make a new Auth object.

  :basic - Basic authentication params
           {:username \"...\" :password \"...\"}

  :password - Password authentication params
              {:username \"...\" :password \"...\"}

  :api-token - kintone app api token.
               string"
  #?(:cljs
     ([]
      (->Auth nil nil nil)))
  ([{:keys [basic password api-token]}]
   (let [basic (when (and (seq (:username basic))
                          (seq (:password basic)))
                 (base64-encode (str (:username basic) ":" (:password basic))))
         password (when (and (seq (:username password))
                             (seq (:password password)))
                    (base64-encode (str (:username password) ":" (:password password))))
         api-token (when (seq api-token)
                     api-token)]
     (->Auth basic password api-token))))

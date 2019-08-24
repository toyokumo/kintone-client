(ns kintone.record
  (:require #?(:clj [clojure.core.async :refer [go go-loop <!]]
               :cljs [cljs.core.async :refer [<!] :refer-macros [go go-loop]])
            [kintone.constant.path.record :as path]
            [kintone.protocols :as pt]))

(defn get-record
  "Retrieves details of 1 record from an app.

  app - The kintone app ID.
        integer
  id - The record ID in kintone app.
       integer"
  [conn app id]
  (let [url (pt/-url conn path/record)]
    (pt/-get conn url {:params {:app app :id id}})))

(defn get-records
  "Retrieves details of multiple records from an app using a query string.

  app - The kintone app ID.
        integer

  :fields - List of field codes you want in the response.
            sequence of string or nil

  :query - The kintone query.
           string or nil

  :total-count - If true, the request will retrieve
                 total count of records match with query conditions.
                 boolean or nil"
  [conn app & [{:keys [fields query total-count]}]]
  (let [url (pt/-url conn path/records)
        params (cond-> {:app app}
                 (seq fields) (assoc :fields fields)
                 (seq query) (assoc :query query)
                 (not (nil? total-count)) (assoc :total-count total-count))]
    (pt/-get conn url {:params params})))

(defn create-cursor
  "Create a cursor that is used to retrieve records.

  app - The kintone app ID.
        integer

  :fields - List of field codes you want in the response.
            sequence of string or nil

  :query - The kintone query.
           string or nil

  :size - Number of records to retrieve per request.
          integer or nil
          Default: 100.
          Maximum: 500"
  [conn app & [{:keys [fields query size]}]]
  (let [url (pt/-url conn path/cursor)
        size (or size 100)
        params (cond-> {:app app :size size}
                 (seq fields) (assoc :fields fields)
                 (seq query) (assoc :query query))]
    (pt/-post conn url {:params params})))

(defn get-records-by-cursor
  "Get one block of records to use cursor.

  :id - Cursor id
        string"
  [conn {:as cursor :keys [id]}]
  (let [url (pt/-url conn path/cursor)
        params {:id id}]
    (pt/-get conn url {:params params})))

(defn get-all-records
  "Get all records to use cursor.

  cursor - The kintone record cursor
           A map has cursor id"
  [conn cursor]
  (go-loop [ret []]
    (let [{:keys [records next]} (<! (get-records-by-cursor conn cursor))
          ret (apply conj ret records)]
      (if next
        (recur ret)
        {:records ret}))))

(defn delete-cursor
  "Delete a cursor.

  cursor - The kintone record cursor
           A map has cursor id"
  [conn {:as cursor :keys [id]}]
  (let [url (pt/-url conn path/cursor)
        params {:id id}]
    (pt/-delete conn url {:params params})))

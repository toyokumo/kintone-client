(ns kintone.record
  (:require #?(:clj [clojure.core.async :refer [go go-loop <!]]
               :cljs [cljs.core.async :refer [<!] :refer-macros [go go-loop]])
            [kintone.constant.path.record :as path]
            [kintone.protocols :as pt]
            [kintone.types :as t]))

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

(defn add-record
  "Add one record to an app.

  app - The kintone app ID.
        integer

  record - The record data that you want to add to kintone app.
           See API reference regarding record format."
  [conn app record]
  (let [url (pt/-url conn path/record)
        params (cond-> {:app app}
                 (seq record) (assoc :record record))]
    (pt/-post conn url {:params params})))

(defn add-records
  "Add multiple records to an app.

  app - The kintone app ID.
        integer

  records - The sequence of record data that you want to add to kintone app.
            The size of records must be 100 or less.
            See API reference regarding record format.
            If the request fail, all registration will be canceled."
  [conn app records]
  (let [url (pt/-url conn path/records)
        params {:app app :records records}]
    (pt/-post conn url {:params params})))

(defn add-all-records
  "Add Multiple Records to an app over 100.

  app - The kintone app ID.
        integer

  records - The sequence of record data that you want to add to kintone app.
            See API reference regarding record format.
            If the request fail, this will stop executing and
            return the response that includes both of completed and failed values."
  [conn app records]
  (go-loop [[records :as rests] (partition-all 100 records)
            ret (t/->KintoneResponse nil nil)]
    (if (empty? records)
      ret
      (let [res (<! (add-records conn app records))]
        (if (.err res)
          (t/->KintoneResponse (.res ret) (.err res))
          (let [ids (vec (concat (:ids (.res ret))
                                 (:ids (.res res))))
                revisions (vec (concat (:revisions (.res ret))
                                       (:revisions (.res res))))]
            (recur (rest rests)
                   (t/->KintoneResponse {:ids ids :revisions revisions} nil))))))))

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

(defn get-records-by-query
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
    (let [res (<! (get-records-by-cursor conn cursor))]
      (if (:err res)
        res
        (let [{:keys [records next]} (:res res)
              ret (apply conj ret records)]
          (if next
            (recur ret)
            (t/->KintoneResponse {:records ret} nil)))))))

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
  "Add Multiple Records to an app.
  This API can add more than 100 records unlike `add-records`.
  If the request fail, this will stop executing and
  return the response that includes both of completed and failed values.

  app - The kintone app ID.
        integer

  records - The sequence of record data that you want to add to kintone app.
            See API reference regarding record format."
  [conn app records]
  (go-loop [[records :as rests] (partition-all 100 records)
            ret (t/->KintoneResponse nil nil)]
    (if (empty? records)
      ret
      (let [res (<! (add-records conn app records))]
        (if (:err res)
          (t/->KintoneResponse (:res ret) (:err res))
          (let [ids (vec (concat (:ids (:res ret))
                                 (:ids (:res res))))
                revisions (vec (concat (:revisions (:res ret))
                                       (:revisions (:res res))))]
            (recur (rest rests)
                   (t/->KintoneResponse {:ids ids :revisions revisions} nil))))))))

(defn- ->update-params
  [{:keys [id update-key record revision]}]
  (cond-> {:record record}
    (not update-key) (assoc :id id)
    (not id) (assoc :updateKey update-key)
    revision (assoc :revision revision)))

(defn update-record
  "Updates a record.
  There must be :id or :update-key in the params.

  app - The kintone app ID.
        integer

  params - A map of update request params.

    :id - The record ID of the kintone app.
          If :id is none or nil, :update-key is necessary.
          integer

    :update-key - The unique key of the record to be updated.
                  If :update-key is none or nil, :id is necessary.
                  string

    :record - The record data that you want to update.
              See API reference regarding record format.

    :revision - The revision number of the record.
                integer, optional"
  [conn app {:as params :keys [id update-key record revision]}]
  (let [url (pt/-url conn path/record)
        params (assoc (->update-params params)
                      :app app)]
    (pt/-put conn url {:params params})))

(defn update-records
  "Updates details of multiple records in an app,
  by specifying their record id, or a different unique key.

  app - The kintone app ID.
        integer

  records - The record data that you want to update.
            See API reference regarding record format.
            The size of records must be 100 or less.
            If the request fail, all updating will be canceled.
            Each record must be following map.

    params - A map of update request params.

      :id - The record ID of the kintone app.
            If :id is none or nil, :update-key is necessary.
            integer

      :update-key - The unique key of the record to be updated.
                    If :update-key is none or nil, :id is necessary.
                    string

      :record - The record data that you want to update.
                See API reference regarding record format.

      :revision - The revision number of the record.
                  integer, optional"
  [conn app records]
  (let [url (pt/-url conn path/records)
        records (mapv ->update-params records)
        params {:app app
                :records records}]
    (pt/-put conn url {:params params})))

(defn update-all-records
  "Updates details of multiple records in an app,
  by specifying their record id, or a different unique keys.
  This API can update more than 100 records unlike `update-records`.
  If the request fail, this will stop executing and
  return the response that includes both of completed and failed values.

  app - The kintone app ID.
        integer

  records - The record data that you want to update.
            See API reference regarding record format.
            If the request fail, all updating will be canceled.  "
  [conn app records]
  (go-loop [[records :as rests] (partition-all 100 records)
            ret (t/->KintoneResponse nil nil)]
    (if (empty? records)
      ret
      (let [res (<! (update-records conn app records))]
        (if (:err res)
          (t/->KintoneResponse (:res ret) (:err res))
          (let [records (vec (concat (:records (:res ret))
                                     (:records (:res res))))]
            (recur (rest rests)
                   (t/->KintoneResponse {:records records} nil))))))))

(defn delete-records
  "Deletes multiple records in an app.

  app - The kintone app ID.
        integer

  ids - Sequence of record id that you want to delete.
        The size of records must be 100 or less.
        sequence of integer"
  [conn app ids]
  (let [url (pt/-url conn path/records)
        params {:app app
                :ids ids}]
    (pt/-delete conn url {:params params})))

(defn delete-records-with-revision
  "Deletes multiple records in an app.

  app - The kintone app ID.
        integer

  params - params includes following keys.
           The size of params must be 100 or less.

    :id - record id that you want to delete.
          integer

    :revision - The revision number of the record.
                  integer"
  [conn app params]
  (let [url (pt/-url conn path/records)
        params {:app app
                :ids (mapv :id params)
                :revisions (mapv :revisions params)}]
    (pt/-delete conn url {:params params})))

(defn- get-id-from-record [record]
  (when-let [sid (get-in record [:$id :value])]
    #?(:clj (Long/parseLong sid)
       :cljs (js/parseInt sid 10))))

(defn delete-all-records-by-query
  "Deletes all records in an app by query string.
  Can delete over 2000 records, but can't do rollback.

  app - The kintone app ID.
        integer

  query - The kintone query.
          string"
  [conn app query]
  (go
    (let [res (<! (create-cursor conn app {:fields [:$id]
                                           :query query
                                           :size 100}))]
      (if (:err res)
        res
        (loop []
          (let [cursor (:res res)
                res (<! (get-records-by-cursor conn cursor))]
            (if (:err res)
              res
              (let [{:keys [records next]} (:res res)
                    ids (map get-id-from-record records)]
                (if (empty? ids)
                  (t/->KintoneResponse {} nil)
                  (let [res (<! (delete-records conn app ids))]
                    (if (or (:err res) (not next))
                      res
                      (recur))))))))))))

(defn add-comment
  "Add a comment to a record in an app.

  app - The kintone app ID.
        integer

  record-id - record id that you want to add comment.
              integer

  comment - A map including comment details.

    :text - The comment text.
            The maximum characters of the comment is 65535.

    :mentions - A sequence including information to mention other users.

      :code - The code the user, group or organization that will be mentioned.
              The maximum number of mentions is 10.
              The mentioned users will be placed in front of the comment text when the API succeeds.

      :type - The type of the mentioned target.
              USER or GROUP or ORGANIZATION"
  [conn app record-id {:as comment :keys [text mentions]}]
  (let [url (pt/-url conn path/comment)
        params {:app app
                :record record-id
                :comment {:text text :mentions mentions}}]
    (pt/-post conn url {:params params})))

(defn delete-comment
  "Delete a comment in a record in an app

  app - The kintone app ID.
        integer

  record-id - The record id.
              integer

  comment-id - The comment id.
               integer"
  [conn app record-id comment-id]
  (let [url (pt/-url conn path/comment)
        params {:app app
                :record record-id
                :comment comment-id}]
    (pt/-delete conn url {:params params})))

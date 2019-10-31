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

  opts

    :fields - Sequence of field codes you want in the response.
              sequence of string or nil

    :query - The kintone query.
             string or nil

   A response map always contains the totalCount of query-matched records.
  "
  ([conn app]
   (get-records-by-query conn app nil))
  ([conn app {:keys [fields query]}]
   (let [url (pt/-url conn path/records)
         params (cond-> {:app app :totalCount true}
                  (seq fields) (assoc :fields fields)
                  (seq query) (assoc :query query))]
     (pt/-get conn url {:params params}))))

(defn create-cursor
  "CAUTION: Consider using get-all-records first.
  Use this only if get-all-records returns too many records to eat up the whole RAM of your machine.

  Create a cursor that is used to retrieve records.

  app - The kintone app ID.
        integer

  opts

    :fields - Sequence of field codes you want in the response.
              sequence of string or nil

    :query - The kintone query. It can't have limit or offset.
             string or nil

    :size - Number of records to retrieve per request.
            integer or nil
            Default: 100.
            Maximum: 500"
  ([conn app]
   (create-cursor conn app nil))
  ([conn app {:keys [fields query size]}]
   (let [url (pt/-url conn path/cursor)
         size (or size 100)
         params (cond-> {:app app :size size}
                  (seq fields) (assoc :fields fields)
                  (seq query) (assoc :query query))]
     (pt/-post conn url {:params params}))))

(defn get-records-by-cursor
  "CAUTION: Consider using get-all-records first.
  Use this only if get-all-records returns too many records to eat up the whole RAM of your machine.

  Get one block of records with cursor.

  :id - Cursor id
        string"
  [conn {:as cursor :keys [id]}]
  (let [url (pt/-url conn path/cursor)
        params {:id id}]
    (pt/-get conn url {:params params})))

(defn get-all-records
  "Get all records with cursor.

  The cursor is released when all records are fetched.

  app - The kintone app ID.
        integer

  opts

    :fields - Sequence of field codes you want in the response.
              sequence of string or nil

    :query - The kintone query. It can't have limit or offset.
             string or nil

    :size - Number of records to retrieve per request.
            integer or nil
            Default: 100.
            Maximum: 500"
  ([conn app]
   (get-all-records conn app nil))
  ([conn app {:as opts :keys [fields query size]}]
   (go
     (let [res (<! (create-cursor conn app opts))
           cursor (:res res)]
       (if (:err res)
         res
         (loop [ret []]
           (let [res (<! (get-records-by-cursor conn cursor))]
             (if (:err res)
               res
               (let [{:keys [records next]} (:res res)
                     ret (apply conj ret records)]
                 (if next
                   (recur ret)
                   (t/->KintoneResponse {:records ret} nil)))))))))))

(defn delete-cursor
  "CAUTION: Consider using get-all-records first.
  Use this only if get-all-records returns too many records to eat up the whole RAM of your machine.

  Delete a cursor.

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
  ([app record]
   (t/map->BulkRequest {:method :POST
                        :path path/record
                        :payload {:app app
                                  :record record}}))
  ([conn app record]
   (let [url (pt/-url conn path/record)
         params (cond-> {:app app}
                  (seq record) (assoc :record record))]
     (pt/-post conn url {:params params}))))

(defn add-records
  "
  CAUTION: Consider using add-all-records first.
  Use this only if add-all-records takes too many records to eat up the whole RAM of the machine.

  Add multiple records to an app.

  app - The kintone app ID.
        integer

  records - The sequence of record data that you want to add to kintone app.
            The size of records must be 100 or less.
            See API reference regarding record format.
            If the request fail, all registration will be canceled."
  ([app records]
   (t/map->BulkRequest {:method :POST
                        :path path/records
                        :payload {:app app
                                  :records records}}))
  ([conn app records]
   (let [url (pt/-url conn path/records)
         params {:app app :records records}]
     (pt/-post conn url {:params params}))))

(defn add-all-records
  "Add Multiple Records to an app.

  This API can add more than 100 records unlike `add-records`.

  If the request fail, this will stop executing and
  return the response that includes both of completed and failed values.

  app - The kintone app ID.
        integer.

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
  ([app {:as params :keys [id update-key record revision]}]
   (t/map->BulkRequest
    {:method :PUT
     :path path/record
     :payload (assoc (->update-params params)
                     :app app)}))
  ([conn app {:as params :keys [id update-key record revision]}]
   (let [url (pt/-url conn path/record)
         params (assoc (->update-params params)
                       :app app)]
     (pt/-put conn url {:params params}))))

(defn update-records
  "
  CAUTION: Consider using update-all-records first.
  Use this only if update-all-records takes too many records to eat up the whole RAM of the machine.

  Updates details of multiple records in an app,
  by specifying their record id, or a different unique key.

  app - The kintone app ID.
        integer

  records - The record data that you want to update.
            See API reference regarding record format.
            The size of records must be 100 or less.
            If the request fail, all updating will be canceled.
            Each record must be following map.

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
  ([app records]
   (t/map->BulkRequest
    {:method :PUT
     :path path/records
     :payload {:app app
               :records (mapv ->update-params records)}}))
  ([conn app records]
   (let [url (pt/-url conn path/records)
         records (mapv ->update-params records)
         params {:app app
                 :records records}]
     (pt/-put conn url {:params params}))))

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
            If the request fail, all updating will be canceled."
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
  ([app ids]
   (t/map->BulkRequest {:method :DELETE
                        :path path/records
                        :payload {:app app
                                  :ids ids}}))
  ([conn app ids]
   (let [url (pt/-url conn path/records)
         params {:app app
                 :ids ids}]
     (pt/-delete conn url {:params params}))))

(defn delete-records-with-revision
  "Deletes multiple records in an app.

  app - The kintone app ID.
        integer

  params - params includes following keys.
           The size of params must be 100 or less.
           sequence of map

    :id - record id that you want to delete.
          integer

    :revision - The revision number of the record.
                integer"
  ([app params]
   (t/map->BulkRequest {:method :DELETE
                        :path path/records
                        :payload {:app app
                                  :ids (mapv :id params)
                                  :revisions (mapv :revision params)}}))
  ([conn app params]
   (let [url (pt/-url conn path/records)
         params {:app app
                 :ids (mapv :id params)
                 :revisions (mapv :revision params)}]
     (pt/-delete conn url {:params params}))))

(defn- get-id-from-record [record]
  (when-let [sid (get-in record [:$id :value])]
    #?(:clj (Long/parseLong sid)
       :cljs (js/parseInt sid 10))))

(defn delete-all-records-by-query
  "Deletes all records in an app by query string.
  Can delete over 2000 records, but can't do rollback.

  app - The kintone app ID.
        integer

  query - The kintone query. It can't have limit or offset.
          string"
  [conn app query]
  (go
    (let [res (<! (create-cursor conn app {:fields [:$id]
                                           :query query
                                           :size 500}))]
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

(defn get-comments
  "Retrieves multiple comments from a record in an app.

  app - The kintone app ID.
        integer

  id - The record id.
       integer

  opts

    :order - The sort order of the Comment ID.
             Specifying \"asc\" will sort the comments in ascending order,
             and \"desc\" will sort the comments in descending order.

    :offset - This skips the retrieval of the first number of comments.
              If it is 30, response skips the first 30 comments,
              and retrieves from the 31st comment.
              There is no maximum for this value.

    :limit - The number of records to retrieve.
             If it is 5, response retrieve the first 5 comments.
             The default and maximum is 10 comments."
  ([conn app id]
   (get-comments conn app id nil))
  ([conn app id {:as opts :keys [order offset limit]}]
   (let [url (pt/-url conn path/comments)
         params {:app app
                 :record id
                 :order (or order "desc")
                 :offset (or offset 0)
                 :limit (or limit 10)}]
     (pt/-get conn url {:params params}))))

(defn add-comment
  "Add a comment to a record in an app.

  app - The kintone app ID.
        integer

  id - The record id.
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
  [conn app id {:as comment :keys [text mentions]}]
  (let [url (pt/-url conn path/comment)
        params {:app app
                :record id
                :comment {:text text :mentions mentions}}]
    (pt/-post conn url {:params params})))

(defn delete-comment
  "Delete a comment in a record in an app

  app - The kintone app ID.
        integer

  id - The record id.
       integer

  comment-id - The comment id.
               integer"
  [conn app id comment-id]
  (let [url (pt/-url conn path/comment)
        params {:app app
                :record id
                :comment comment-id}]
    (pt/-delete conn url {:params params})))

(defn update-record-assignees
  "Update assignees of a record.

  app - The kintone app ID.
        integer

  id - The record id.
       integer

  assignees - The user codes of the assignees.
              If empty, no users will be assigned.
              The maximum number of assignees is 100.
              sequence of string

  revision - The revision number of the record.
             integer, optional"
  [conn app id assignees revision]
  (let [url (pt/-url conn path/assignees)
        params (cond-> {:app app
                        :record id
                        :assignees assignees}
                 revision (assoc :revision revision))]
    (pt/-put conn url {:params params})))

(defn- ->status-record [{:keys [id action assignee revision]}]
  (cond-> {:id id :action action}
    assignee (assoc :assignee assignee)
    revision (assoc :revision revision)))

(defn update-record-status
  "Updates the Status of a record of an app.

  app - The kintone app ID.
        integer

  params

    :id - The record id.
          integer

    :action - The Action name of the action you want to run.
              If the localization feature has been used
              to apply multiple translations of the action,
              specify the name of the action in the language settings
              of the user that will run the API.
              Note \"Action name\" refers to the text on the button
              that is pressed to move Process Management on to the next status.

    :assignee - The user code of the assignee.
                If empty, no users will be assigned.
                The maximum number of assignees is 100.
                sequence of string

    :revision - The revision number of the record.
                integer, optional"
  ([app {:as params :keys [id action assignee revision]}]
   (t/map->BulkRequest {:method :PUT
                        :path path/status
                        :payload (assoc (->status-record params)
                                        :app app)}))
  ([conn app {:as params :keys [id action assignee revision]}]
   (let [url (pt/-url conn path/status)
         params (assoc (->status-record params)
                       :app app)]
     (pt/-put conn url {:params params}))))

(defn update-records-status
  "Updates the Status of multiple records of an app.

  app - The kintone app ID.
        integer

  records - sequence of map.
            each map has following keys.
            The size must be 100 or less.

    :id - The record id.
          integer

    :action - The Action name of the action you want to run.
              If the localization feature has been used
              to apply multiple translations of the action,
              specify the name of the action in the language settings
              of the user that will run the API.
              Note \"Action name\" refers to the text on the button
              that is pressed to move Process Management on to the next status.

    :assignee - The user code of the assignee.
                If empty, no users will be assigned.
                The maximum number of assignees is 100.
                sequence of string

    :revision - The revision number of the record.
                integer, optional"
  ([app [{:keys [id action assignee revision]} :as records]]
   (t/map->BulkRequest {:method :PUT
                        :path path/statuses
                        :payload {:app app
                                  :records (mapv ->status-record records)}}))
  ([conn app [{:keys [id action assignee revision]} :as records]]
   (let [url (pt/-url conn path/statuses)
         params {:app app
                 :records (mapv ->status-record records)}]
     (pt/-put conn url {:params params}))))

(defn file-upload
  "Uploads a file to kintone.

  When a file is uploaded, it produces a file key.
  Note that although this API uploads a file to kintone,
  it does not upload the file to an attachment field of an App.
  To upload the file to an Attachment field,
  the file key is used with the Add Record or Update Record API.

  file - (Clojure) String, InputStream, File, a byte-array,
                   or an instance of org.apache.http.entity.mime.content.ContentBody
         (ClojureScript) File object

  filename - The filename you want to set to
             string, Only for ClojureScript"
  #?(:clj
     ([conn file]
      (let [url (pt/-url conn path/file)
            multipart [{:name "file" :content file}]]
        (pt/-multipart-post conn url {:multipart multipart})))

     :cljs
     ([conn file filename]
      (let [url (pt/-url conn path/file)
            multipart (doto (js/FormData.)
                        (.append "file" file filename))]
        (pt/-multipart-post conn url {:multipart multipart})))))

(defn file-download
  "Download a file from an attachment field in an app.

  file-key - The value(string) is set on the attachment field in the response
             that is gotten from GET record APIs.
             So the file-key is different from that
             obtained from the response when using the Upload File API.

  as - (Only Clojure) A keyword that is used for output coercion.
       default :byte-array
       See clj-http document for detail."
  ([conn file-key]
   (let [url (pt/-url conn path/file)
         params {:fileKey file-key}]
     (pt/-get-blob conn url {:params params})))

  #?(:clj
     ([conn file-key as]
      (let [url (pt/-url conn path/file)
            params {:fileKey file-key}]
        (pt/-get-blob conn url {:params params :as as})))))

(defn bulk-request
  "Runs multiple API requests to multiple apps in one go.

  requests - The multiple request maps.
             Its size must be 20 or less.
             The following functions return a request map
             which is used here when you does not pass the connection map to these.
               - add-record
               - add-records
               - update-record
               - update-records
               - delete-records
               - delete-records-with-revision
               - update-record-status
               - update-records-status"
  [conn requests]
  (let [url (pt/-url conn path/bulk-request)
        requests (mapv (fn [{:as m :keys [path]}]
                         (-> m
                             (assoc :api (pt/-path conn path))
                             (dissoc :path)))
                       requests)
        params {:requests requests}]
    (pt/-post conn url {:params params})))

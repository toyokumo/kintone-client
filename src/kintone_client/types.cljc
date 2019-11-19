(ns kintone-client.types)

(defrecord KintoneResponse [res err])

(defrecord BulkRequest [method path payload])

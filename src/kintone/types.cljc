(ns kintone.types)

(defrecord KintoneResponse [res err])

(defrecord BulkRequest [method path payload])

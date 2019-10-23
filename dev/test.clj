(ns test
  (:require [clojure.core.async :refer [<!!]]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer :all]
            [kintone.authentication :as auth]
            [kintone.connection :as conn]
            [kintone.record :as record]))

(def conf (edn/read-string (slurp "dev-resources/config.edn")))

(def auth (auth/new-auth (:auth conf)))

(def conn (conn/new-connection {:auth auth
                                :domain (:domain conf)}))

(def app (:app conf))

(def string-field-code :文字列__1行_)

(def attachment-file-field-code :添付ファイル)

(deftest file-upload-filename-mojibake-test
  (testing "japanese filename"
    (let [upload-file-key (-> (<!! (record/file-upload conn (io/file "dev-resources/日本語ファイル名.txt")))
                              :res
                              :fileKey)
          record-id (-> (<!! (record/add-record conn app {string-field-code {:value "file name test"}
                                                          attachment-file-field-code {:value [{:fileKey upload-file-key}]}}))
                        :res
                        :id)
          file-name (-> (<!! (record/get-record conn app record-id))
                        :res
                        :record
                        attachment-file-field-code
                        :value
                        first
                        :name)]
      (is (= "日本語ファイル名.txt" file-name))))
  (testing "ascii filename"
    (let [upload-file-key (-> (<!! (record/file-upload conn (io/file "dev-resources/ascii-filename.txt")))
                              :res
                              :fileKey)
          record-id (-> (<!! (record/add-record conn app {string-field-code {:value "file name test"}
                                                          attachment-file-field-code {:value [{:fileKey upload-file-key}]}}))
                        :res
                        :id)
          file-name (-> (<!! (record/get-record conn app record-id))
                        :res
                        :record
                        attachment-file-field-code
                        :value
                        first
                        :name)]
      (is (= "ascii-filename.txt" file-name)))))

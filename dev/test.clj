(ns test
  (:require [clojure.core.async :refer [<!!]]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer :all]
            [kintone-client.app :as app]
            [kintone-client.user :as user]
            [kintone-client.authentication :as auth]
            [kintone-client.connection :as conn]
            [kintone-client.record :as record]
            [clojure.string :as str]))

(def conf (edn/read-string (slurp "dev-resources/config.edn")))

(def auth (auth/new-auth (:auth conf)))

(def conn (conn/new-connection {:auth auth
                                :domain (:domain conf)}))

(def app (:app conf))

(defn delete-all-record [conn app]
  (record/delete-all-records-by-query conn app ""))

(defmacro with-cleanup [& body]
  `(try ~@body
        (finally
          (<!! (delete-all-record conn app)))))

(defn pp
  [x]
  (println x)
  x)

;; TODO: throws weird error on Cursive
;; Error handling response - class java.lang.IndexOutOfBoundsException: Wrong line: 140. Available lines count: 140

(deftest get-record-test
  (with-cleanup
    (let [id (-> (<!! (record/add-record conn app {:文字列__1行_ {:value "ほげ"}}))
                 :res
                 :id)]
      (is (= nil (:err (<!! (record/get-record conn app id))))))))

(deftest get-all-records-test
  (with-cleanup
    (let [_ (-> (<!! (record/add-record conn app {:文字列__1行_ {:value "ほげ1"}}))
                  :res
                  :id)
          _ (-> (<!! (record/add-record conn app {:文字列__1行_ {:value "ほげ2"}}))
                  :res
                  :id)
          _ (-> (<!! (record/add-record conn app {:文字列__1行_ {:value "ほげ3"}}))
                  :res
                  :id)
          res (<!! (record/get-all-records conn app))]
      (is (= nil (:err res)))
      (is (= (set ["ほげ1" "ほげ2" "ほげ3"])
             (->> res :res :records (map #(-> % :文字列__1行_ :value)) set))))))

(deftest get-records-by-query-test
  (with-cleanup
    (let [_ (-> (<!! (record/add-record conn app {:文字列__1行_ {:value "ほげ1"}}))
                :res
                :id)
          _ (-> (<!! (record/add-record conn app {:文字列__1行_ {:value "ほげ2"}}))
                :res
                :id)
          _ (-> (<!! (record/add-record conn app {:文字列__1行_ {:value "ほげ3"}}))
                :res
                :id)]
      (is (= nil (:err (<!! (record/get-records-by-query conn app)))))
      (is (= "3" (-> (<!! (record/get-records-by-query conn app))
                     :res
                     :totalCount)))
      (is (= nil (:err (<!! (record/get-records-by-query conn app {:fields [:$id]})))))
      (is (= nil (:err (<!! (record/get-records-by-query conn app {:fields [:作成者]
                                                                   :query "order by 作成者 asc"})))))
      (is (= nil (:err (<!! (record/get-records-by-query conn app {:fields [:作成者]
                                                                   :query "order by 作成者"})))))
      (is (= 1 (-> (<!! (record/get-records-by-query conn app {:query "order by 作成者 limit 1"}))
                   :res
                   :records
                   count)))
      (is (= nil (:err (<!! (record/get-records-by-query conn app {:query "offset 1"})))))
      (is (= ["ほげ3" "ほげ2" "ほげ1"]
             (->> (<!! (record/get-records-by-query conn app {:query "order by 文字列__1行_ desc"}))
                  :res
                  :records
                  (mapv #(-> % :文字列__1行_ :value)))))
      (is (= ["ほげ1" "ほげ2" "ほげ3"]
             (->> (<!! (record/get-records-by-query conn app {:query "order by 文字列__1行_ asc"}))
                  :res
                  :records
                  (mapv #(-> % :文字列__1行_ :value))))))))

(deftest get-record-by-cursor-test
  (with-cleanup
    (let [_ (-> (<!! (record/add-record conn app {:文字列__1行_ {:value "ほげ1"}}))
                :res
                :id)
          _ (-> (<!! (record/add-record conn app {:文字列__1行_ {:value "ほげ2"}}))
                :res
                :id)
          _ (-> (<!! (record/add-record conn app {:文字列__1行_ {:value "ほげ3"}}))
                :res
                :id)
          cursor (:res (<!! (record/create-cursor conn app)))]
      (try
        (let [res (<!! (record/get-records-by-cursor conn cursor))]
          (is (= nil (:err res)))
          (is (= 3 (-> res :res :records count))))
        (finally
          (<!! (record/delete-cursor conn cursor)))))))

(deftest add-record-test
  (with-cleanup
    (let [res (<!! (record/add-record conn app {:文字列__1行_  {:value "ほげ"}}))]
      (is (= nil (:err res)))
      (is (= 1 (-> (<!! (record/get-all-records conn app))
                   :res
                   :records
                   count))))))

(deftest add-records-test
  (with-cleanup
    (let [res (<!! (record/add-records conn app [{:文字列__1行_ {:value "ほげ1"}}
                                                 {:文字列__1行_ {:value "ほげ2"}}]))]
      (is (= nil (:err res)))
      (is (= 2 (-> (<!! (record/get-all-records conn app))
                   :res
                   :records
                   count))))))

(deftest add-all-records-test
  (with-cleanup
    (let [res (<!! (record/add-all-records conn app [{:文字列__1行_ {:value "ほげ1"}}
                                                     {:文字列__1行_ {:value "ほげ2"}}]))]
      (is (= nil (:err res)))
      (is (= 2 (-> (<!! (record/get-all-records conn app))
                   :res
                   :records
                   count))))))

(deftest update-record-test
  (with-cleanup
    (let [id (-> (<!! (record/add-record conn app {:文字列__1行_ {:value "あああ"}}))
                 :res
                 :id)
          res (<!! (record/update-record conn app {:id id
                                                   :record {:文字列__1行_ {:value "いいい"}}}))]
      (is (= nil (:err res)))
      (is (= "いいい"
             (-> (<!! (record/get-record conn app id))
                 :res
                 :record
                 :文字列__1行_
                 :value))))))

(deftest update-records-test
  (with-cleanup
    (let [id1 (-> (<!! (record/add-record conn app {:文字列__1行_ {:value "あああ"}}))
                  :res
                  :id)
          id2 (-> (<!! (record/add-record conn app {:文字列__1行_ {:value "あああ"}}))
                  :res
                  :id)
          res (<!! (record/update-records conn app [{:id id1
                                                     :record {:文字列__1行_ {:value "いいい"}}}
                                                    {:id id2
                                                     :record {:文字列__1行_ {:value "ううう"}}}]))]
      (is (= nil (:err res)))
      (is (= "いいい"
             (-> (<!! (record/get-record conn app id1))
                 :res
                 :record
                 :文字列__1行_
                 :value)))
      (is (= "ううう"
             (-> (<!! (record/get-record conn app id2))
                 :res
                 :record
                 :文字列__1行_
                 :value))))))

(deftest update-all-records-test
  (with-cleanup
    (let [id1 (-> (<!! (record/add-record conn app {:文字列__1行_ {:value "あああ"}}))
                  :res
                  :id)
          id2 (-> (<!! (record/add-record conn app {:文字列__1行_ {:value "あああ"}}))
                  :res
                  :id)
          res (<!! (record/update-all-records conn app [{:id id1
                                                         :record {:文字列__1行_ {:value "いいい"}}}
                                                        {:id id2
                                                         :record {:文字列__1行_ {:value "ううう"}}}]))]
      (is (= nil (:err res)))
      (is (= "いいい"
             (-> (<!! (record/get-record conn app id1))
                 :res
                 :record
                 :文字列__1行_
                 :value)))
      (is (= "ううう"
             (-> (<!! (record/get-record conn app id2))
                 :res
                 :record
                 :文字列__1行_
                 :value))))))

(deftest file-upload-test
  (with-cleanup
    (let [file-key (-> (<!! (record/file-upload conn (io/file "dev-resources/aa.txt")))
                       :res
                       :fileKey)
          res (<!! (record/add-record conn app {:添付ファイル {:value [{:fileKey file-key}]}}))]
      (is (= nil (:err res))))))

(deftest file-download-test
  (with-cleanup
    (let [upload-file-key (-> (<!! (record/file-upload conn (io/file "dev-resources/aa.txt")))
                              :res
                              :fileKey)
          record-id (-> (<!! (record/add-record conn app {:添付ファイル {:value [{:fileKey upload-file-key}]}}))
                        :res
                        :id)
          download-file-key (-> (<!! (record/get-record conn app record-id))
                                :res
                                :record
                                :添付ファイル
                                :value
                                first
                                :fileKey)
          res (<!! (record/file-download conn download-file-key))]
      (is (= nil (:err res))))))

(deftest bulk-request-test
  (with-cleanup
    (let [id (-> (<!! (record/add-record conn app {:文字列__1行_ {:value "あああ"}}))
                 :res
                 :id)
          res (<!! (record/bulk-request conn
                                        [(record/add-record app {:文字列__1行_ {:value "いいい"}})
                                         (record/update-record app {:id id
                                                               :record {:文字列__1行_ {:value "ううう"}}})]))
          records (<!! (record/get-all-records conn app))]
      (is (= nil (:err res)))
      (is (= (set ["いいい" "ううう"])
             (->> records :res :records (map #(-> % :文字列__1行_ :value)) set))))))

(deftest get-app-test
  (with-cleanup
    (let [{:keys [res err]}  (<!! (app/get-app conn app))]
      (is (nil? err))
      (is (= "kintone-cljファイルアップロードテスト" (:name res)))
      (is (= (str app) (:appId res))))))

(deftest get-form-test
  (with-cleanup
    (let [{:keys [res err]} (<!! (app/get-form conn app))
          [number-field single-line-text-field file-field
           :as props] (some->> res :properties (sort-by :code))
          test-fields [:type :label :code]]
      (is (nil? err))
      (is (= 3 (count props)))
      (is (= {:type "NUMBER" :code "数値" :label "数値"}
             (select-keys number-field test-fields)))
      (is (= {:type "SINGLE_LINE_TEXT" :code "文字列__1行_" :label "文字列 (1行)"}
             (select-keys single-line-text-field test-fields)))
      (is (= {:type "FILE" :code "添付ファイル" :label "添付ファイル"}
             (select-keys file-field test-fields))))))


(comment
 (deftest user-tests
   (let [user-codes (map #(str "kintone-client-TEST-USER-" %) (range 3))
         [user1 user2 user3] user-codes
         new-code1 "kintone-client-TEST-USER-100"
         new-code2 "kintone-client-TEST-USER-10"]
     (testing "add-users-test"
       (let [user-data (map (fn [name] {:code name
                                        :name (str "name of " name)
                                        :password "PASSWORD"
                                        :description (str "DESC of " name)})
                            user-codes)
             {:keys [err]} (<!! (user/add-users conn user-data))]
         (is (nil? err))))

     (testing "get-users-test"
       (let [{:keys [res err]} (<!! (user/get-users conn {:codes user-codes}))
             fetched-users (:users res)]
         (is (nil? err))
         (is (= 3 (count fetched-users)))
         (is (every? #(str/starts-with? (:description %) "DESC of") fetched-users))
         (is (every? #(str/starts-with? (:name %) "name of") fetched-users)))
       (let [{:keys [res err]} (<!! (user/get-users conn {:codes user-codes :size 2}))]
         (is (nil? err))
         (is (= 2 (count (:users res)))))
       (let [{:keys [res err]} (<!! (user/get-users conn {:codes user-codes :offset 2}))]
         (is (nil? err))
         (is (= 1 (count (:users res))))))

     (testing "update-users-test"
       (let [{:keys [err]} (<!! (user/update-users conn [{:code user1
                                                          :name "modified name"}
                                                         {:code user3
                                                          :description ""}]))]
         (is (nil? err)))
       (let [{:keys [res]} (<!! (user/get-users conn {:codes [user1 user3]}))]
         (is (= "modified name" (get-in res [:users 0 :name])))
         (is (= "" (get-in res [:users 1 :description])))))

     (testing "update-user-codes-test"
       (let [{:keys [err]} (<!! (user/update-user-codes conn [{:currentCode user1
                                                               :newCode new-code1}
                                                              {:currentCode user2
                                                               :newCode new-code2}]))]
         (is (nil? err)))
       (let [{:keys [res err]} (<!! (user/get-users conn {:codes user-codes}))]
         (is (nil? err))
         (is (= 1 (count (:users res)))))
       (let [{:keys [res err]} (<!! (user/get-users conn {:codes [user3 new-code1 new-code2]}))
             fetched-users (:users res)]
         (is (nil? err))
         (is (= 3 (count fetched-users)))))

     (testing "delete-users-test"
       (let [{:keys [err]} (<!! (user/delete-users conn [new-code1]))]
         (is (nil? err)))
       (let [{:keys [res err]} (<!! (user/get-users conn {:codes [new-code1 new-code2]}))]
         (is (nil? err))
         (is (= 1 (count (:users res)))))
       (let [{:keys [err]} (<!! (user/delete-users conn [user3 new-code2]))]
         (is (nil? err))))))

 (deftest organization-test
   (let [org-codes (map #(str "kintone-client-TEST-ORG-" %) (range 3))
         [org1 org2 org3] org-codes
         new-code1 "kintone-client-TEST-ORG-100"
         new-code2 "kintone-client-TEST-ORG-10"]
     (testing "add-organizations-test"
       (let [org-data (map (fn [name] {:code name
                                       :name (str "name of " name)
                                       :password "PASSWORD"
                                       :description (str "DESC of " name)})
                           org-codes)
             {:keys [err]} (<!! (user/add-organizations conn org-data))]
         (is (nil? err))))

     (testing "get-organizations-test"
       (let [{:keys [res err]} (<!! (user/get-organizations conn {:codes org-codes}))
             fetched-orgs (:organizations res)]
         (is (nil? err))
         (is (= 3 (count fetched-orgs)))
         (is (every? #(str/starts-with? (:description %) "DESC of") fetched-orgs))
         (is (every? #(str/starts-with? (:name %) "name of") fetched-orgs)))
       (let [{:keys [res err]} (<!! (user/get-organizations conn {:codes org-codes :size 2}))]
         (is (nil? err))
         (is (= 2 (count (:organizations res)))))
       (let [{:keys [res err]} (<!! (user/get-organizations conn {:codes org-codes :offset 2}))]
         (is (nil? err))
         (is (= 1 (count (:organizations res))))))

     (testing "update-organizations-test"
       (let [{:keys [err]} (<!! (user/update-organizations conn [{:code org1
                                                                  :name "modified name"}
                                                                 {:code org3
                                                                  :description ""}]))]
         (is (nil? err)))
       (let [{:keys [res]} (<!! (user/get-organizations conn {:codes [org1 org3]}))]
         (is (= "modified name" (get-in res [:organizations 0 :name])))
         (is (= "" (get-in res [:organizations 1 :description])))))

     (testing "update-org-codes-test"
       (let [{:keys [err]} (<!! (user/update-org-codes conn [{:currentCode org1
                                                              :newCode new-code1}
                                                             {:currentCode org2
                                                              :newCode new-code2}]))]
         (is (nil? err)))
       (let [{:keys [res err]} (<!! (user/get-organizations conn {:codes org-codes}))]
         (is (nil? err))
         (is (= 1 (count (:organizations res)))))
       (let [{:keys [res err]} (<!! (user/get-organizations conn {:codes [org3 new-code1 new-code2]}))
             fetched-orgs (:organizations res)]
         (is (nil? err))
         (is (= 3 (count fetched-orgs)))))

     (testing "delete-organizations-test"
       (let [{:keys [err]} (<!! (user/delete-organizations conn [new-code1]))]
         (is (nil? err)))
       (let [{:keys [res err]} (<!! (user/get-organizations conn {:codes [new-code1 new-code2]}))]
         (is (nil? err))
         (is (= 1 (count (:organizations res)))))
       (let [{:keys [err]} (<!! (user/delete-organizations conn [org3 new-code2]))]
         (is (nil? err)))))))

(comment
 (run-tests 'test))

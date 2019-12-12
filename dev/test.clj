(ns test
  (:require [clojure.core.async :refer [<!! <! chan go-loop timeout]]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer :all]
            [kintone-client.app :as app]
            [kintone-client.authentication :as auth]
            [kintone-client.connection :as conn]
            [kintone-client.record :as record]))

(def conf (edn/read-string (slurp "dev-resources/config.edn")))

(def auth (auth/new-auth (:auth conf)))

(def conn (conn/new-connection {:auth auth
                                :domain (:domain conf)}))

(def app (:app conf))

(def space (:space conf))

(defn delete-all-record [conn app]
  (record/delete-all-records-by-query conn app ""))

(defmacro with-cleanup [& body]
  `(try ~@body
        (finally
          (<!! (delete-all-record conn app)))))

(defn wait-app-deploy [conn app]
  (<!! (go-loop []
         (when (= (get-in (<!! (app/get-app-deploy-status conn [app])) [:res :apps 0 :status])
                  "PROCESSING")
           (<! (timeout 2000))
           (recur)))))

(defmacro with-app [app & body]
  `(let [~app (-> (<!! (app/add-preview-app conn {:space space
                                                  :name "kintone-client-dev-app"
                                                  :thread space}))
                  :res
                  :app
                  Integer/parseInt)]
     (<!! (app/deploy-app-settings conn [{:app ~app}] {}))
     (wait-app-deploy conn ~app)
     ~@body))

(defn map-includes?
  [parent child]
  (= (select-keys parent (keys child)) child))

(defn pp
  [x]
  (println x)
  x)

;; TODO: throws weird error on Cursive
;; Error handling response - class java.lang.IndexOutOfBoundsException: Wrong line: 140. Available lines count: 140

;; Record API tests

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
    (let [res (<!! (record/add-record conn app {:文字列__1行_ {:value "ほげ"}}))]
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

;; App API tests

(deftest get-app-test
  (with-cleanup
    (let [{:keys [res err]} (<!! (app/get-app conn app))]
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

(deftest get-apps-test
  (let [names ["test-app1" "test-app2" "dev-app3"]
        apps (map #(-> (<!! (app/add-preview-app conn {:name %
                                                       :space space
                                                       :thread space}))
                       :res
                       :app
                       Integer/parseInt)
                  names)
        [app1 app2 app3] apps]
    (<!! (app/deploy-app-settings conn (map (fn [app] {:app app}) apps) {}))
    (<!! (timeout 10000))                                 ; wait 10 seconds
    (let [{:keys [res err]} (<!! (app/get-apps conn {:name "test-app" :space-ids [space] :ids apps}))]
      (is (nil? err))
      (is (= (map str [app1 app2]) (->> res
                                        :apps
                                        (map :appId)))))
    (let [{:keys [res err]} (<!! (app/get-apps conn {:offset 1 :space-ids [space] :ids apps}))]
      (is (nil? err))
      (is (= (map str [app2 app3]) (->> res
                                        :apps
                                        (map :appId)))))))

(deftest get-form-layout-test
  (let [{:keys [res err]} (<!! (app/get-form-layout conn app {}))
        layout (:layout res)
        fields (map :fields layout)]
    (is (nil? err))
    (is (some? (:revision res)))
    (is (= [[{:type "NUMBER" :code "数値" :size {:width "193"}}]
            [{:type "SINGLE_LINE_TEXT" :code "文字列__1行_" :size {:width "193"}}]
            [{:type "FILE" :code "添付ファイル" :size {:width "207"}}]]
           fields))))

(deftest update-form-layout-test
  (with-app app
    (<!! (app/add-form-fields conn app {:TEXT1 {:type "SINGLE_LINE_TEXT"
                                                :code "TEXT1"
                                                :label "文字列1行"}
                                        :FILE1 {:type "FILE"
                                                :code "FILE1"
                                                :label "ファイル"}
                                        :DATE1 {:type "DATE"
                                                :code "DATE1"
                                                :label "日付"}} {}))
    (let [layout [{:type "ROW" :fields [{:type "SINGLE_LINE_TEXT"
                                         :code "TEXT1"
                                         :size {:width "100"}}]}
                  {:type "ROW" :fields [{:type "FILE"
                                         :code "FILE1"
                                         :size {:width "200"}}
                                        {:type "DATE"
                                         :code "DATE1"
                                         :size {:width "300"}}]}]
          {:keys [err]} (<!! (app/update-form-layout conn app layout {}))]
      (is (nil? err))
      (let [{:keys [res]} (<!! (app/get-form-layout conn app {:is-preview? true}))]
        (is (= layout (:layout res)))))))

(deftest get-form-fields-test
  (let [{:keys [res err]} (<!! (app/get-form-fields conn app {}))
        test-keys [:type :code :label]
        fields (:properties res)]
    (is (nil? err))
    (is (some? (:revision res)))
    (is (= {:type "NUMBER" :code "数値" :label "数値"}
           (select-keys (:数値 fields) test-keys)))
    (is (= {:type "SINGLE_LINE_TEXT" :code "文字列__1行_" :label "文字列 (1行)"}
           (select-keys (:文字列__1行_ fields) test-keys)))
    (is (= {:type "FILE" :code "添付ファイル" :label "添付ファイル"}
           (select-keys (:添付ファイル fields) test-keys)))
    (is (= {:type "MODIFIER" :code "更新者" :label "更新者"}
           (select-keys (:更新者 fields) test-keys)))))

(deftest add-form-fields-test
  (with-app app
    (let [original-fields (-> (<!! (app/get-form-fields conn app {}))
                              :res
                              :properties)
          additional-fields {:TEXT1 {:type "SINGLE_LINE_TEXT"
                                     :code "TEXT1"
                                     :label "文字列1行"}
                             :ファイル {:type "FILE"
                                    :code "ファイル"
                                    :label "ファイル"}
                             :checkbox {:type "CHECK_BOX"
                                        :code "checkbox"
                                        :label "チェックボックス"
                                        :options {:A {:label "A" :index 1}
                                                  :B {:label "B" :index 2}
                                                  ":C" {:label ":C" :index 3}}
                                        :defaultValue ["A"]}}
          {:keys [err]} (<!! (app/add-form-fields conn app additional-fields {}))]
      (is (nil? err))
      (let [new-fields (-> (<!! (app/get-form-fields conn app {:is-preview? true}))
                           :res
                           :properties)]
        (is (map-includes? new-fields original-fields))
        (is (every? #(map-includes? (% new-fields) (% additional-fields)) [:TEXT1 :ファイル]))
        (is (map-includes? (:checkbox new-fields)
                           {:type "CHECK_BOX"
                            :code "checkbox"
                            :label "チェックボックス"
                            :options {:A {:label "A" :index "0"}
                                      :B {:label "B" :index "1"}
                                      (keyword ":C") {:label ":C" :index "2"}}
                            :defaultValue ["A"]}))))))

(deftest update-form-fields-test
  (with-app app
    (let [original-fields (-> (<!! (app/get-form-fields conn app {}))
                              :res
                              :properties)
          additional-field {:TEXT1 {:type "SINGLE_LINE_TEXT"
                                    :code "TEXT1"
                                    :label "文字列1行"}}
          modified-field {:TEXT1 {:type "SINGLE_LINE_TEXT"
                                  :code "TEXT1"
                                  :label "変更後"}}]
      (<!! (app/add-form-fields conn app additional-field {}))
      (let [{:keys [err]} (<!! (app/update-form-fields conn app modified-field {}))]
        (is (nil? err))
        (let [new-fields (-> (<!! (app/get-form-fields conn app {:is-preview? true}))
                             :res
                             :properties)]
          (is (map-includes? new-fields original-fields))
          (is (map-includes? (:TEXT1 new-fields)
                             (:TEXT1 modified-field))))))))

(deftest delete-form-fields-test
  (with-app app
    (let [original-fields (-> (<!! (app/get-form-fields conn app {}))
                              :res
                              :properties)
          additional-field {:TEXT1 {:type "SINGLE_LINE_TEXT"
                                    :code "TEXT1"
                                    :label "文字列1行"}
                            :FILE1 {:type "FILE"
                                    :code "FILE1"
                                    :label "ファイル"}}]
      (<!! (app/add-form-fields conn app additional-field {}))
      (let [{:keys [err]} (<!! (app/delete-form-fields conn app [:TEXT1] {}))]
        (is (nil? err))
        (let [new-fields (-> (<!! (app/get-form-fields conn app {:is-preview? true}))
                             :res
                             :properties)]
          (is (map-includes? new-fields original-fields))
          (is (map-includes? (:FILE1 new-fields)
                             (:FILE1 additional-field)))
          (is (not-any? #(= % :TEXT1) (keys new-fields))))))))

(deftest add-preview-app-test
  (let [{:keys [res err]} (<!! (app/add-preview-app conn {:name "ADD-APP-TEST"
                                                          :space space
                                                          :thread space}))
        app (-> res
                :app
                Integer/parseInt)]
    (is (nil? err))
    (<!! (app/deploy-app-settings conn [{:app app}] {}))
    (wait-app-deploy conn app)
    (let [{:keys [res]} (<!! (app/get-app conn app))]
      (is (= "ADD-APP-TEST" (:name res)))
      (is (= (str app) (:appId res)))
      (is (= (str space) (:spaceId res)))
      (is (= (str space) (:threadId res))))))

(deftest deploy-app-settings-test
  (let [app (-> (<!! (app/add-preview-app conn {:space space
                                                :name "kintone-client-dev-app"
                                                :thread space}))
                :res
                :app
                Integer/parseInt)]
    (<!! (app/add-form-fields conn app {:TEXT1 {:code "TEXT1"
                                                :type "SINGLE_LINE_TEXT"
                                                :label "text"}} {}))
    (is (-> (app/get-form-fields conn app {})
            <!!
            :res
            :properties
            (contains? :TEXT1)
            not))
    (is (-> (app/get-form-fields conn app {:is-preview? true})
            <!!
            :res
            :properties
            (contains? :TEXT1)))
    (let [{:keys [err]} (<!! (app/deploy-app-settings conn [{:app app}] {}))]
      (is (nil? err))
      (wait-app-deploy conn app)
      (is (-> (app/get-form-fields conn app {})
              <!!
              :res
              :properties
              (contains? :TEXT1)))))
  (with-app app
    (<!! (app/add-form-fields conn app {:TEXT1 {:code "TEXT1"
                                                :type "SINGLE_LINE_TEXT"
                                                :label "text"}} {}))
    (is (-> (app/get-form-fields conn app {})
            <!!
            :res
            :properties
            (contains? :TEXT1)
            not))
    (is (-> (app/get-form-fields conn app {:is-preview? true})
            <!!
            :res
            :properties
            (contains? :TEXT1)))
    (let [{:keys [err]} (<!! (app/deploy-app-settings conn [{:app app}] {:revert true}))]
      (is (nil? err))
      (wait-app-deploy conn app)
      (is (-> (app/get-form-fields conn app {:is-preview? true})
              <!!
              :res
              :properties
              (contains? :TEXT1)
              not)))
    ))

(deftest get-app-deploy-status-test
  (with-app app
    (<!! (app/deploy-app-settings conn [{:app app}] {}))
    (let [{:keys [res err]} (<!! (app/get-app-deploy-status conn [app]))]
      (is (nil? err))
      (is (= "PROCESSING"
             (get-in res [:apps 0 :status])))
      (<!! (timeout 10000))                                 ; wait 10 seconds
      (let [{:keys [res err]} (<!! (app/get-app-deploy-status conn [app]))]
        (is (nil? err))
        (is (= "SUCCESS"
               (get-in res [:apps 0 :status])))))))

(deftest get-views-test
  (let [{:keys [res err]} (<!! (app/get-views conn app {}))]
    (is (nil? err))
    (is (some? (:revision res)))
    (is (= {} (:views res)))))

(deftest update-views-test
  (with-app app
    (let [views {:view1 {:index "0"
                         :type "LIST"
                         :name "view1"
                         :fields ["作成者"]
                         :filterCond "更新日時 > \"2012-02-03T09:00:00Z\""}
                 :cal {:index "1"
                       :type "CALENDAR"
                       :name "cal"}}
          {:keys [err]} (<!! (app/update-views conn app views {}))]
      (is (nil? err))
      (let [{:keys [res]} (<!! (app/get-views conn app {:is-preview? true}))]
        (is (map-includes? (get-in res [:views :view1]) (:view1 views)))
        (is (map-includes? (get-in res [:views :cal]) (:cal views)))))))

(deftest get-general-settings-test
  (let [{:keys [res err]} (<!! (app/get-general-settings conn app {}))
        {:keys [name description icon theme revision]} res]
    (is (nil? err))
    (is (some? revision))
    (is (= "kintone-cljファイルアップロードテスト" name))
    (is (= "<div>kintone-clj-test</div>" description))
    (is (= {:type "PRESET" :key "APP39"} icon))
    (is (= "WHITE" theme))))

(deftest update-general-settings-test
  (with-app app
    (let [new-setting {:name "new-NAME"
                       :description "<h2>new-DESCRIPTION</h2>"
                       :icon {:type "PRESET"
                              :key "APP65"}
                       :theme "RED"}
          {:keys [err]} (<!! (app/update-general-settings conn app new-setting {}))]
      (is (nil? err))
      (is (= new-setting
             (-> (<!! (app/get-general-settings conn app {:is-preview? true}))
                 :res
                 (select-keys (keys new-setting))))))))

(deftest get-status-test
  (let [{:keys [res err]} (<!! (app/get-status conn app {}))
        {:keys [enable states actions revision]} res]
    (is (nil? err))
    (is (some? revision))
    (is (false? enable))
    (is (nil? states))
    (is (nil? actions))))

(deftest update-status-test
  (with-app app
    (let [new-status {:enable true
                      :states {:s1 {:name "s1"
                                    :index "0"}
                               :s2 {:name "s2"
                                    :index "1"}
                               :s3 {:name "s3"
                                    :index "2"}}
                      :actions [{:name "a1"
                                 :from "s1"
                                 :to "s2"}
                                {:name "a2"
                                 :from "s2"
                                 :to "s3"}
                                {:name "a3"
                                 :from "s1"
                                 :to "s3"}]}
          {:keys [err]} (<!! (app/update-status conn app new-status {}))]
      (is (nil? err))
      (let [{:keys [res]} (<!! (app/get-status conn app {:is-preview? true}))]
        (is (= true (:enable res)))
        (is (map-includes? (get-in res [:states :s1]) (get-in new-status [:states :s1])))
        (is (map-includes? (get-in res [:states :s2]) (get-in new-status [:states :s2])))
        (is (map-includes? (get-in res [:states :s3]) (get-in new-status [:states :s3])))
        (is (= (->> res
                    :actions
                    (map #(select-keys % [:name :from :to]))
                    (sort-by :name))
               (:actions new-status)))))))

(deftest get-customize-test
  (let [{:keys [res err]} (<!! (app/get-customize conn app {}))
        {:keys [scope desktop mobile revision]} res]
    (is (nil? err))
    (is (some? revision))
    (is (= "ALL" scope))
    (is (= {:js [] :css []} desktop))
    (is (= {:js [] :css []} mobile))))

(deftest update-customize-test
  (with-app app
    (let [file-key1 (-> (<!! (record/file-upload conn (io/file "dev-resources/abc.js")))
                        :res
                        :fileKey)
          file-key2 (-> (<!! (record/file-upload conn (io/file "dev-resources/abc.js")))
                        :res
                        :fileKey)
          customize {:scope "ADMIN"
                     :desktop {:js [{:type "URL"
                                     :url "https://example.com/app.js"}
                                    {:type "FILE"
                                     :file {:fileKey file-key1}}]
                               :css [{:type "URL"
                                      :url "https://example.net/style.css"}]}
                     :mobile {:js [{:type "FILE"
                                    :file {:fileKey file-key2}}]
                              :css [{:type "URL"
                                     :url "https://example.net/mobile.css"}]}}
          {:keys [err]} (<!! (app/update-customize conn app customize {}))]
      (is (nil? err))
      (let [{:keys [res]} (<!! (app/get-customize conn app {:is-preview? true}))
            {:keys [scope desktop mobile]} res]
        (is (= "ADMIN" scope))
        (is (= {:type "URL"
                :url "https://example.com/app.js"}
               (get-in desktop [:js 0])))
        (is (= "FILE"
               (get-in desktop [:js 1 :type])))
        (is (= [{:type "URL"
                 :url "https://example.net/style.css"}]
               (:css desktop)))
        (is (= "FILE"
               (get-in mobile [:js 0 :type])))
        (is (= [{:type "URL"
                 :url "https://example.net/mobile.css"}]
               (:css mobile)))))))

(deftest get-acl-test
  (let [{:keys [res err]} (<!! (app/get-acl conn app {}))]
    (is (nil? err))
    (is (some? (:revision res)))
    (is (= [{:recordViewable true,
             :appEditable true,
             :recordDeletable true,
             :recordImportable true,
             :recordExportable true,
             :includeSubs false,
             :entity {:type "CREATOR", :code nil},
             :recordAddable true,
             :recordEditable true}
            {:recordViewable true,
             :appEditable false,
             :recordDeletable true,
             :recordImportable false,
             :recordExportable false,
             :includeSubs false,
             :entity {:type "GROUP", :code "everyone"},
             :recordAddable true,
             :recordEditable true}]
           (:rights res)))
    ))

(deftest update-acl-test
  (with-app app
    (let [rights [{:entity {:type "GROUP"
                            :code "Administrators"}
                   :appEditable true
                   :recordViewable false
                   :recordAddable false}
                  {:entity {:type "CREATOR"}
                   :appEditable true
                   :recordViewable true
                   :recordAddable true
                   :recordEditable true
                   :recordDeletable true
                   :recordImportable true
                   :recordExportable true}]
          {:keys [err]} (<!! (app/update-acl conn app rights {}))]
      (is (nil? err))
      (let [{:keys [res]} (<!! (app/get-acl conn app {:is-preview? true}))]
        (is (map-includes? (get-in res [:rights 0]) (get rights 0)))
        (is (map-includes? (get-in res [:rights 1]) (-> rights
                                                        (get 1)
                                                        (assoc-in [:entity :code] nil))))))))

(deftest get-field-acl-test
  (let [{:keys [res err]} (<!! (app/get-field-acl conn app {}))]
    (is (nil? err))
    (is (= [] (:rights res)))))

(deftest update-field-acl-test
  (with-app app
    (<!! (app/add-form-fields conn app {:TEXT1 {:type "SINGLE_LINE_TEXT"
                                                :code "TEXT1"
                                                :label "文字列1行"}
                                        :FILE1 {:type "FILE"
                                                :code "FILE1"
                                                :label "ファイル"}} {}))
    (let [rights [{:code "TEXT1"
                   :entities [{:accessibility "READ"
                               :entity {:type "GROUP"
                                        :code "Administrators"}}
                              {:accessibility "NONE"
                               :entity {:type "GROUP"
                                        :code "everyone"}}]}
                  {:code "作成者"
                   :entities [{:accessibility "READ"
                               :entity {:type "GROUP"
                                        :code "Administrators"}}]}]
          {:keys [err]} (<!! (app/update-field-acl conn app rights {}))]
      (is (= err nil))
      (let [{:keys [res]} (<!! (app/get-field-acl conn app {:is-preview? true}))]
        (is (= [{:code "TEXT1"
                 :entities [{:entity {:type "GROUP"
                                      :code "Administrators"}
                             :accessibility "READ"
                             :includeSubs false}
                            {:entity {:type "GROUP"
                                      :code "everyone"}
                             :accessibility "NONE"
                             :includeSubs false}]}
                {:code "作成者"
                 :entities [{:entity {:type "GROUP"
                                      :code "Administrators"}
                             :accessibility "READ"
                             :includeSubs false}
                            {:entity {:type "GROUP"
                                      :code "everyone"}
                             :accessibility "NONE"
                             :includeSubs false}]}]
               (:rights res)))))))

(deftest update-live-field-acl-test
  (with-app app
    (<!! (app/add-form-fields conn app {:TEXT1 {:type "SINGLE_LINE_TEXT"
                                                :code "TEXT1"
                                                :label "文字列1行"}
                                        :FILE1 {:type "FILE"
                                                :code "FILE1"
                                                :label "ファイル"}} {}))
    (let [rights [{:code "TEXT1"
                   :entities [{:accessibility "READ"
                               :entity {:type "GROUP"
                                        :code "Administrators"}}
                              {:accessibility "NONE"
                               :entity {:type "GROUP"
                                        :code "everyone"}}]}
                  {:code "作成者"
                   :entities [{:accessibility "READ"
                               :entity {:type "GROUP"
                                        :code "Administrators"}}]}]
          {:keys [err]} (<!! (app/update-live-field-acl conn app rights {}))]
      (is (= err nil))
      (let [{:keys [res]} (<!! (app/get-field-acl conn app {}))]
        (is (= [{:code "TEXT1"
                 :entities [{:entity {:type "GROUP"
                                      :code "Administrators"}
                             :accessibility "READ"
                             :includeSubs false}
                            {:entity {:type "GROUP"
                                      :code "everyone"}
                             :accessibility "NONE"
                             :includeSubs false}]}
                {:code "作成者"
                 :entities [{:entity {:type "GROUP"
                                      :code "Administrators"}
                             :accessibility "READ"
                             :includeSubs false}
                            {:entity {:type "GROUP"
                                      :code "everyone"}
                             :accessibility "NONE"
                             :includeSubs false}]}]
               (:rights res)))))))

(comment
 (run-tests 'test))

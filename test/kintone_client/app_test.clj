(ns kintone-client.app-test
  (:require [clojure.core.async :refer [<!!]]
            [clojure.test :refer :all]
            [kintone-client.app :as app]
            [kintone-client.test-helper :as h]
            [kintone-client.types :as t]))

(def ^:private app (rand-int 100))

(deftest get-app-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/app.json"
                               :req {:params {:id app}}}
                              nil)
         (<!! (app/get-app h/fake-conn app)))))

(deftest get-form-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/form.json"
                               :req {:params {:app app}}}
                              nil)
         (<!! (app/get-form h/fake-conn app)))))

(deftest get-apps-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/apps.json"
                               :req {:params {}}}
                              nil)
         (<!! (app/get-apps h/fake-conn {}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/apps.json"
                               :req {:params {:offset 10
                                              :limit 20
                                              :codes ["code1" "code2"]
                                              :name "app-name"
                                              :ids [100 200]
                                              :spaceIds [1 2]}}}
                              nil)
         (<!! (app/get-apps h/fake-conn {:offset 10
                                         :limit 20
                                         :codes ["code1" "code2"]
                                         :name "app-name"
                                         :ids [100 200]
                                         :space-ids [1 2]})))))

(deftest get-form-layout-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/app/form/layout.json"
                               :req {:params {:app 100}}}
                              nil)
         (<!! (app/get-form-layout h/fake-conn 100 {}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app/form/layout.json"
                               :req {:params {:app 100}}}
                              nil)
         (<!! (app/get-form-layout h/fake-conn 100 {:preview? true})))))


(deftest update-form-layout-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app/form/layout.json"
                               :req {:params {:app 100
                                              :layout [{:type "ROW" :fields [{:type "SINGLE_LINE_TEXT"
                                                                              :code "TEXT1"
                                                                              :size {:width "100"}}]}
                                                       {:type "ROW" :fields [{:type "FILE"
                                                                              :code "FILE1"
                                                                              :size {:width "200"}}
                                                                             {:type "DATE"
                                                                              :code "DATE1"
                                                                              :size {:width "300"}}]}]}}}
                              nil)
         (<!! (app/update-form-layout h/fake-conn
                                      100
                                      [{:type "ROW" :fields [{:type "SINGLE_LINE_TEXT"
                                                              :code "TEXT1"
                                                              :size {:width "100"}}]}
                                       {:type "ROW" :fields [{:type "FILE"
                                                              :code "FILE1"
                                                              :size {:width "200"}}
                                                             {:type "DATE"
                                                              :code "DATE1"
                                                              :size {:width "300"}}]}]
                                      {}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app/form/layout.json"
                               :req {:params {:app 100
                                              :layout [{:type "ROW" :fields [{:type "SINGLE_LINE_TEXT"
                                                                              :code "TEXT1"
                                                                              :size {:width "100"}}]}
                                                       {:type "ROW" :fields [{:type "FILE"
                                                                              :code "FILE1"
                                                                              :size {:width "200"}}
                                                                             {:type "DATE"
                                                                              :code "DATE1"
                                                                              :size {:width "300"}}]}]
                                              :revision 5}}}
                              nil)
         (<!! (app/update-form-layout h/fake-conn
                                      100
                                      [{:type "ROW" :fields [{:type "SINGLE_LINE_TEXT"
                                                              :code "TEXT1"
                                                              :size {:width "100"}}]}
                                       {:type "ROW" :fields [{:type "FILE"
                                                              :code "FILE1"
                                                              :size {:width "200"}}
                                                             {:type "DATE"
                                                              :code "DATE1"
                                                              :size {:width "300"}}]}]
                                      {:revision 5})))))


(deftest get-form-fields-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/app/form/fields.json"
                               :req {:params {:app 100}}}
                              nil)
         (<!! (app/get-form-fields h/fake-conn 100 {})))))


(deftest add-form-fields-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app/form/fields.json"
                               :req {:params {:app 100
                                              :properties {:TEXT1 {:type "SINGLE_LINE_TEXT"
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
                                                                      :defaultValue ["A"]}}}}}
                              nil)
         (<!! (app/add-form-fields h/fake-conn
                                   100
                                   {:TEXT1 {:type "SINGLE_LINE_TEXT"
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
                                   {}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app/form/fields.json"
                               :req {:params {:app 100
                                              :properties {:TEXT1 {:type "SINGLE_LINE_TEXT"
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
                                              :revision 5}}}
                              nil)
         (<!! (app/add-form-fields h/fake-conn
                                   100
                                   {:TEXT1 {:type "SINGLE_LINE_TEXT"
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
                                   {:revision 5})))))


(deftest update-form-fields-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app/form/fields.json"
                               :req {:params {:app 100
                                              :properties {:TEXT1 {:type "SINGLE_LINE_TEXT"
                                                                   :code "TEXT1"
                                                                   :label "変更後"}}}}}
                              nil)
         (<!! (app/update-form-fields h/fake-conn 100 {:TEXT1 {:type "SINGLE_LINE_TEXT"
                                                               :code "TEXT1"
                                                               :label "変更後"}} {}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app/form/fields.json"
                               :req {:params {:app 100
                                              :properties {:TEXT1 {:type "SINGLE_LINE_TEXT"
                                                                   :code "TEXT1"
                                                                   :label "変更後"}}
                                              :revision 5}}}
                              nil)
         (<!! (app/update-form-fields h/fake-conn 100 {:TEXT1 {:type "SINGLE_LINE_TEXT"
                                                               :code "TEXT1"
                                                               :label "変更後"}} {:revision 5})))))


(deftest delete-form-fields-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app/form/fields.json"
                               :req {:params {:app 100
                                              :fields ["code1" "code_2"]}}}
                              nil)
         (<!! (app/delete-form-fields h/fake-conn 100 ["code1" "code_2"] {}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app/form/fields.json"
                               :req {:params {:app 100
                                              :fields ["code1" "code_2"]
                                              :revision 5}}}
                              nil)
         (<!! (app/delete-form-fields h/fake-conn 100 ["code1" "code_2"] {:revision 5})))))

(deftest add-preview-app-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app.json"
                               :req {:params {:name "TEST-APP"}}}
                              nil)
         (<!! (app/add-preview-app h/fake-conn {:name "TEST-APP"}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app.json"
                               :req {:params {:name "TEST-APP"
                                              :thread 50
                                              :space 51}}}
                              nil)
         (<!! (app/add-preview-app h/fake-conn {:name "TEST-APP" :thread 50 :space 51})))))


(deftest deploy-app-settings-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app/deploy.json"
                               :req {:params {:apps [{:app 100}
                                                     {:app 101
                                                      :revision 5}]}}}
                              nil)
         (<!! (app/deploy-app-settings h/fake-conn [{:app 100}
                                                    {:app 101
                                                     :revision 5}] {}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app/deploy.json"
                               :req {:params {:apps [{:app 100}
                                                     {:app 101
                                                      :revision 5}]
                                              :revert true}}}
                              nil)
         (<!! (app/deploy-app-settings h/fake-conn [{:app 100}
                                                    {:app 101
                                                     :revision 5}] {:revert true})))))


(deftest get-app-deploy-status-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app/deploy.json"
                               :req {:params {:apps [100 101]}}}
                              nil)
         (<!! (app/get-app-deploy-status h/fake-conn [100 101])))))


(deftest get-views-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/app/views.json"
                               :req {:params {:app 100}}}
                              nil)
         (<!! (app/get-views h/fake-conn 100 {}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app/views.json"
                               :req {:params {:app 100
                                              :lang "en"}}}
                              nil)
         (<!! (app/get-views h/fake-conn 100 {:lang "en" :preview? true})))))


(deftest update-views-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app/views.json"
                               :req {:params {:app 100
                                              :views {:view1 {:index "0"
                                                              :type "LIST"
                                                              :name "view1"
                                                              :fields ["作成者"]
                                                              :filterCond "更新日時 > \"2012-02-03T09:00:00Z\""}
                                                      :cal {:index "1"
                                                            :type "CALENDAR"
                                                            :name "cal"}}}}}
                              nil)
         (<!! (app/update-views h/fake-conn 100 {:view1 {:index "0"
                                                         :type "LIST"
                                                         :name "view1"
                                                         :fields ["作成者"]
                                                         :filterCond "更新日時 > \"2012-02-03T09:00:00Z\""}
                                                 :cal {:index "1"
                                                       :type "CALENDAR"
                                                       :name "cal"}} {})))))


(deftest get-general-settings-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/app/settings.json"
                               :req {:params {:app 100}}}
                              nil)
         (<!! (app/get-general-settings h/fake-conn 100 {}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app/settings.json"
                               :req {:params {:app 100
                                              :lang "en"}}}
                              nil)
         (<!! (app/get-general-settings h/fake-conn 100 {:lang "en" :preview? true})))))


(deftest update-general-settings-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app/settings.json"
                               :req {:params {:app 100
                                              :name "new-NAME"
                                              :description "<h2>new-DESCRIPTION</h2>"
                                              :icon {:type "PRESET"
                                                     :key "APP65"}
                                              :theme "RED"}}}
                              nil)
         (<!! (app/update-general-settings h/fake-conn 100 {:name "new-NAME"
                                                            :description "<h2>new-DESCRIPTION</h2>"
                                                            :icon {:type "PRESET"
                                                                   :key "APP65"}
                                                            :theme "RED"} {}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app/settings.json"
                               :req {:params {:app 100
                                              :name "new-NAME"
                                              :description "<h2>new-DESCRIPTION</h2>"
                                              :icon {:type "PRESET"
                                                     :key "APP65"}
                                              :theme "RED"
                                              :revision 5}}}
                              nil)
         (<!! (app/update-general-settings h/fake-conn 100 {:name "new-NAME"
                                                            :description "<h2>new-DESCRIPTION</h2>"
                                                            :icon {:type "PRESET"
                                                                   :key "APP65"}
                                                            :theme "RED"} {:revision 5})))))


(deftest get-status-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/app/status.json"
                               :req {:params {:app 100}}}
                              nil)
         (<!! (app/get-status h/fake-conn 100 {}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app/status.json"
                               :req {:params {:app 100
                                              :lang "en"}}}
                              nil)
         (<!! (app/get-status h/fake-conn 100 {:lang "en" :preview? true})))))


(deftest update-status-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app/status.json"
                               :req {:params {:app 100
                                              :enable true
                                              :states {:s1 {:name "s1"
                                                            :index "0"}
                                                       :s2 {:name "s2"
                                                            :index "1"}}
                                              :actions [{:name "a1"
                                                         :from "s1"
                                                         :to "s2"}]}}}
                              nil)
         (<!! (app/update-status h/fake-conn 100 {:enable true
                                                  :states {:s1 {:name "s1"
                                                                :index "0"}
                                                           :s2 {:name "s2"
                                                                :index "1"}}
                                                  :actions [{:name "a1"
                                                             :from "s1"
                                                             :to "s2"}]} {}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app/status.json"
                               :req {:params {:app 100
                                              :enable true
                                              :states {:s1 {:name "s1"
                                                            :index "0"}
                                                       :s2 {:name "s2"
                                                            :index "1"}}
                                              :actions [{:name "a1"
                                                         :from "s1"
                                                         :to "s2"}]
                                              :revision 5}}}
                              nil)
         (<!! (app/update-status h/fake-conn 100 {:enable true
                                                  :states {:s1 {:name "s1"
                                                                :index "0"}
                                                           :s2 {:name "s2"
                                                                :index "1"}}
                                                  :actions [{:name "a1"
                                                             :from "s1"
                                                             :to "s2"}]} {:revision 5})))))


(deftest get-customize-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/app/customize.json"
                               :req {:params {:app 100}}}
                              nil)
         (<!! (app/get-customize h/fake-conn 100 {}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app/customize.json"
                               :req {:params {:app 100}}}
                              nil)
         (<!! (app/get-customize h/fake-conn 100 {:preview? true})))))


(deftest update-customize-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app/customize.json"
                               :req {:params {:app 100
                                              :scope "ADMIN"
                                              :desktop {:js [{:type "URL"
                                                              :url "https://example.com/app.js"}
                                                             {:type "FILE"
                                                              :file {:fileKey "key1"}}]
                                                        :css [{:type "URL"
                                                               :url "https://example.net/style.css"}]}
                                              :mobile {:js [{:type "FILE"
                                                             :file {:fileKey "key2"}}]
                                                       :css [{:type "URL"
                                                              :url "https://example.net/mobile.css"}]}}}}
                              nil)
         (<!! (app/update-customize h/fake-conn
                                    100
                                    {:scope "ADMIN"
                                     :desktop {:js [{:type "URL"
                                                     :url "https://example.com/app.js"}
                                                    {:type "FILE"
                                                     :file {:fileKey "key1"}}]
                                               :css [{:type "URL"
                                                      :url "https://example.net/style.css"}]}
                                     :mobile {:js [{:type "FILE"
                                                    :file {:fileKey "key2"}}]
                                              :css [{:type "URL"
                                                     :url "https://example.net/mobile.css"}]}}
                                    {}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app/customize.json"
                               :req {:params {:app 100
                                              :scope "ADMIN"
                                              :desktop {:js [{:type "URL"
                                                              :url "https://example.com/app.js"}
                                                             {:type "FILE"
                                                              :file {:fileKey "key1"}}]
                                                        :css [{:type "URL"
                                                               :url "https://example.net/style.css"}]}
                                              :mobile {:js [{:type "FILE"
                                                             :file {:fileKey "key2"}}]
                                                       :css [{:type "URL"
                                                              :url "https://example.net/mobile.css"}]}
                                              :revision 5}}}
                              nil)
         (<!! (app/update-customize h/fake-conn
                                    100
                                    {:scope "ADMIN"
                                     :desktop {:js [{:type "URL"
                                                     :url "https://example.com/app.js"}
                                                    {:type "FILE"
                                                     :file {:fileKey "key1"}}]
                                               :css [{:type "URL"
                                                      :url "https://example.net/style.css"}]}
                                     :mobile {:js [{:type "FILE"
                                                    :file {:fileKey "key2"}}]
                                              :css [{:type "URL"
                                                     :url "https://example.net/mobile.css"}]}}
                                    {:revision 5})))))


(deftest get-acl-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/app/acl.json"
                               :req {:params {:app 100}}}
                              nil)
         (<!! (app/get-acl h/fake-conn 100 {}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app/acl.json"
                               :req {:params {:app 100}}}
                              nil)
         (<!! (app/get-acl h/fake-conn 100 {:preview? true})))))


(deftest update-acl-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app/acl.json"
                               :req {:params {:app 100
                                              :rights [{:entity {:type "GROUP"
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
                                                        :recordExportable true}]}}}
                              nil)
         (<!! (app/update-acl h/fake-conn 100 [{:entity {:type "GROUP"
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
                                                :recordExportable true}] {}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/app/acl.json"
                               :req {:params {:app 100
                                              :rights [{:entity {:type "GROUP"
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
                                              :revision 5}}}
                              nil)
         (<!! (app/update-acl h/fake-conn 100 [{:entity {:type "GROUP"
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
                                                :recordExportable true}] {:revision 5})))))


(deftest get-field-acl-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/field/acl.json"
                               :req {:params {:app 100}}}
                              nil)
         (<!! (app/get-field-acl h/fake-conn 100 {}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/field/acl.json"
                               :req {:params {:app 100}}}
                              nil)
         (<!! (app/get-field-acl h/fake-conn 100 {:preview? true})))))


(deftest update-field-acl-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/field/acl.json"
                               :req {:params {:app 100
                                              :rights [{:code "TEXT1"
                                                        :entities [{:accessibility "READ"
                                                                    :entity {:type "GROUP"
                                                                             :code "Administrators"}}
                                                                   {:accessibility "NONE"
                                                                    :entity {:type "GROUP"
                                                                             :code "everyone"}}]}
                                                       {:code "作成者"
                                                        :entities [{:accessibility "READ"
                                                                    :entity {:type "GROUP"
                                                                             :code "Administrators"}}]}]}}}
                              nil)
         (<!! (app/update-field-acl h/fake-conn
                                    100
                                    [{:code "TEXT1"
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
                                    {}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/preview/field/acl.json"
                               :req {:params {:app 100
                                              :rights [{:code "TEXT1"
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
                                              :revision 5}}}
                              nil)
         (<!! (app/update-field-acl h/fake-conn
                                    100
                                    [{:code "TEXT1"
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
                                    {:revision 5})))))

(deftest update-live-field-acl-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/field/acl.json"
                               :req {:params {:app 100
                                              :rights [{:code "TEXT1"
                                                        :entities [{:accessibility "READ"
                                                                    :entity {:type "GROUP"
                                                                             :code "Administrators"}}
                                                                   {:accessibility "NONE"
                                                                    :entity {:type "GROUP"
                                                                             :code "everyone"}}]}
                                                       {:code "作成者"
                                                        :entities [{:accessibility "READ"
                                                                    :entity {:type "GROUP"
                                                                             :code "Administrators"}}]}]}}}
                              nil)
         (<!! (app/update-live-field-acl h/fake-conn
                                         100
                                         [{:code "TEXT1"
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
                                         {}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/field/acl.json"
                               :req {:params {:app 100
                                              :rights [{:code "TEXT1"
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
                                              :revision 5}}}
                              nil)
         (<!! (app/update-live-field-acl h/fake-conn
                                         100
                                         [{:code "TEXT1"
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
                                         {:revision 5})))))

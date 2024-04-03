(ns kintone-client.record-test
  (:require
   [clojure.core.async :refer [<!! chan put!]]
   [clojure.test :refer [deftest is testing]]
   [kintone-client.record :as r]
   [kintone-client.test-helper :as h]
   [kintone-client.types :as t]))

(def app (rand-int 100))

(def id (rand-int 100))

(deftest get-record-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/record.json"
                               :req {:params {:app app :id id}}}
                              nil)
         (<!! (r/get-record h/fake-conn app id)))))

(deftest get-records-by-query-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/records.json"
                               :req {:params {:app app
                                              :totalCount true}}}
                              nil)
         (<!! (r/get-records-by-query h/fake-conn app))))

  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/records.json"
                               :req {:params {:app app
                                              :totalCount true}}}
                              nil)
         (<!! (r/get-records-by-query h/fake-conn app {}))))

  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/records.json"
                               :req {:params {:app app
                                              :totalCount true
                                              :fields [:$id :name]
                                              :query "order by $id limit 10"}}}
                              nil)
         (<!! (r/get-records-by-query h/fake-conn app {:app app
                                                       :fields [:$id :name]
                                                       :query "order by $id limit 10"})))))

(deftest create-cursor-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/records/cursor.json"
                               :req {:params {:app app :size 100}}}
                              nil)
         (<!! (r/create-cursor h/fake-conn app))))

  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/records/cursor.json"
                               :req {:params {:app app :size 100}}}
                              nil)
         (<!! (r/create-cursor h/fake-conn app {}))))

  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/records/cursor.json"
                               :req {:params {:app app
                                              :fields [:$id :name]
                                              :query "$id > 100"
                                              :size 123}}}
                              nil)
         (<!! (r/create-cursor h/fake-conn app {:fields [:$id :name]
                                                :query "$id > 100"
                                                :size 123})))))

(deftest get-records-by-cursor-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/records/cursor.json"
                               :req {:params {:id "123-456"}}}
                              nil)
         (<!! (r/get-records-by-cursor h/fake-conn {:id "123-456"})))))

(deftest get-all-records-test
  (testing "Fail to create cursor"
    (with-redefs [r/create-cursor
                  (fn [& _]
                    (let [c (chan)]
                      (put! c (t/->KintoneResponse nil {:status 500}))
                      c))]
      (is (= (t/->KintoneResponse nil {:status 500})
             (<!! (r/get-all-records h/fake-conn app))))))

  (testing "Fail to get record on the first request"
    (with-redefs [r/create-cursor
                  (fn [& _]
                    (let [c (chan)]
                      (put! c (t/->KintoneResponse {:id "123-123"} nil))
                      c))
                  r/get-records-by-cursor
                  (fn [& _]
                    (let [c (chan)]
                      (put! c (t/->KintoneResponse nil {:status 400}))
                      c))]
      (is (= (t/->KintoneResponse nil {:status 400})
             (<!! (r/get-all-records h/fake-conn app))))))

  (testing "Fail to get record on the second request"
    (let [ncall (atom 0)]
      (with-redefs [r/create-cursor
                    (fn [& _]
                      (let [c (chan)]
                        (put! c (t/->KintoneResponse {:id "123-123"} nil))
                        c))
                    r/get-records-by-cursor
                    (fn [& _]
                      (let [c (chan)]
                        (if (< @ncall 2)
                          (do (swap! ncall inc)
                              (put! c (t/->KintoneResponse {:records [{:id 1} {:id 2}]
                                                            :next true}
                                                           nil)))
                          (put! c (t/->KintoneResponse nil {:status 400})))
                        c))]
        (is (= (t/->KintoneResponse nil {:status 400})
               (<!! (r/get-all-records h/fake-conn app)))))))

  (testing "Success"
    (let [ncall (atom 0)]
      (with-redefs [r/create-cursor
                    (fn [& _]
                      (let [c (chan)]
                        (put! c (t/->KintoneResponse {:id "123-123"} nil))
                        c))
                    r/get-records-by-cursor
                    (fn [& _]
                      (let [c (chan)]
                        (if (< @ncall 2)
                          (do (swap! ncall inc)
                              (put! c (t/->KintoneResponse {:records [{:id 1} {:id 2}]
                                                            :next true}
                                                           nil)))
                          (put! c (t/->KintoneResponse {:records [{:id 3} {:id 4}]
                                                        :next false}
                                                       nil)))
                        c))]
        (is (= (t/->KintoneResponse {:records [{:id 1}
                                               {:id 2}
                                               {:id 1}
                                               {:id 2}
                                               {:id 3}
                                               {:id 4}]} nil)
               (<!! (r/get-all-records h/fake-conn app))))))))


(deftest delete-cursor-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/records/cursor.json"
                               :req {:params {:id "112-112"}}}
                              nil)
         (<!! (r/delete-cursor h/fake-conn {:id "112-112"})))))

(deftest add-record-test
  (is (= (t/->BulkRequest :POST "/v1/record.json"
                          {:app app
                           :record {:name {:value "foo"}}})
         (r/add-record app {:name {:value "foo"}})))

  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/record.json"
                               :req {:params {:app app}}}
                              nil)
         (<!! (r/add-record h/fake-conn app nil))))

  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/record.json"
                               :req {:params {:app app
                                              :record {:name {:value "foo"}}}}}
                              nil)
         (<!! (r/add-record h/fake-conn app {:name {:value "foo"}})))))

(deftest add-records-test
  (is (= (t/->BulkRequest :POST "/v1/records.json"
                          {:app app
                           :records [{:name {:value "foo"}}
                                     {:name {:value "bar"}}]})
         (r/add-records app [{:name {:value "foo"}}
                             {:name {:value "bar"}}])))

  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/records.json"
                               :req {:params {:app app
                                              :records [{:name {:value "foo"}}
                                                        {:name {:value "bar"}}]}}}
                              nil)
         (<!! (r/add-records h/fake-conn app [{:name {:value "foo"}}
                                              {:name {:value "bar"}}])))))

(deftest add-all-records-test
  (testing "Fail to add on the first request"
    (with-redefs [r/add-records
                  (fn [& _]
                    (let [c (chan)]
                      (put! c (t/->KintoneResponse nil {:status 400}))
                      c))]
      (is (= (t/->KintoneResponse nil {:status 400})
             (<!! (r/add-all-records h/fake-conn app [{:name {:value "foo"}}
                                                      {:name {:value "bar"}}]))))))

  (testing "Fail to add on the second request"
    (let [ncall (atom 0)]
      (with-redefs [r/add-records
                    (fn [& _]
                      (let [c (chan)]
                        (if (< @ncall 1)
                          (do (swap! ncall inc)
                              (put! c (t/->KintoneResponse {:ids ["1" "2"]
                                                            :revisions ["1" "1"]}
                                                           nil)))
                          (put! c (t/->KintoneResponse nil {:status 500})))
                        c))]
        (is (= (t/->KintoneResponse {:ids ["1" "2"]
                                     :revisions ["1" "1"]}
                                    nil)
               (<!! (r/add-all-records h/fake-conn app [{:name {:value "foo"}}
                                                        {:name {:value "bar"}}]))))

        ;; Clear state
        (reset! ncall 0)
        (is (= (t/->KintoneResponse {:ids ["1" "2"]
                                     :revisions ["1" "1"]}
                                    {:status 500})
               (->> (range 110)
                    (mapv (fn [i] {:name {:value (str i)}}))
                    (r/add-all-records h/fake-conn app)
                    <!!)))))))

(deftest update-record-test
  (is (= (t/->BulkRequest :PUT "/v1/record.json"
                          {:app app
                           :id 1
                           :record {:name {:value "foo"}}})
         (r/update-record app {:id 1
                               :record {:name {:value "foo"}}})))

  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/record.json"
                               :req {:params {:app app
                                              :record {:name {:value "foo"}}
                                              :updateKey {:field "XYZ"
                                                          :value "123"}}}}
                              nil)
         (<!! (r/update-record h/fake-conn app {:update-key {:field "XYZ"
                                                             :value "123"}
                                                :record {:name {:value "foo"}}})))))


(deftest update-records-test
  (is (= (t/->BulkRequest :PUT "/v1/records.json"
                          {:app app
                           :records [{:id 1
                                      :record {:name {:value "foo"}}}
                                     {:id 2
                                      :record {:name {:value "bar"}}}]})
         (r/update-records app [{:id 1
                                 :record {:name {:value "foo"}}}
                                {:id 2
                                 :record {:name {:value "bar"}}}])))

  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/records.json"
                               :req {:params {:app app
                                              :records [{:id 1
                                                         :record {:name {:value "foo"}}}
                                                        {:id 2
                                                         :record {:name {:value "bar"}}}]}}}
                              nil)
         (<!! (r/update-records h/fake-conn app [{:id 1
                                                  :record {:name {:value "foo"}}}
                                                 {:id 2
                                                  :record {:name {:value "bar"}}}])))))

(deftest update-all-records-test
  (testing "Fail to update on the first request"
    (with-redefs [r/update-records
                  (fn [& _]
                    (let [c (chan)]
                      (put! c (t/->KintoneResponse nil {:status 400}))
                      c))]
      (is (= (t/->KintoneResponse nil {:status 400})
             (<!! (r/update-all-records h/fake-conn app [{:id 1
                                                          :record {:name {:value "foo"}}}
                                                         {:id 2
                                                          :record {:name {:value "bar"}}}]))))))

  (testing "Fail to update on the second request"
    (let [ncall (atom 0)]
      (with-redefs [r/update-records
                    (fn [& _]
                      (let [c (chan)]
                        (if (< @ncall 1)
                          (do (swap! ncall inc)
                              (put! c (t/->KintoneResponse {:records [{:id "1"
                                                                       :revision "2"}
                                                                      {:id "2"
                                                                       :revision "1"}]}
                                                           nil)))
                          (put! c (t/->KintoneResponse nil {:status 500})))
                        c))]
        (is (= (t/->KintoneResponse {:records [{:id "1"
                                                :revision "2"}
                                               {:id "2"
                                                :revision "1"}]}
                                    nil)
               (<!! (r/update-all-records h/fake-conn app [{:id 1
                                                            :record {:name {:value "foo"}}}
                                                           {:id 2
                                                            :record {:name {:value "bar"}}}]))))

        ;; Clear state
        (reset! ncall 0)
        (is (= (t/->KintoneResponse {:records [{:id "1"
                                                :revision "2"}
                                               {:id "2"
                                                :revision "1"}]}
                                    {:status 500})
               (->> (range 110)
                    (mapv (fn [i] {:name {:value (str i)}}))
                    (r/update-all-records h/fake-conn app)
                    <!!)))))))

(deftest delete-records-test
  (is (= (t/->BulkRequest :DELETE "/v1/records.json"
                          {:app app
                           :ids [1 2 3]})
         (r/delete-records app [1 2 3])))

  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/records.json"
                               :req {:params {:app app
                                              :ids [1 2 3]}}}
                              nil)
         (<!! (r/delete-records h/fake-conn app [1 2 3])))))

(deftest delete-records-with-revision-test
  (is (= (t/->BulkRequest :DELETE "/v1/records.json"
                          {:app app
                           :ids [1 2]
                           :revisions [1 1]})
         (r/delete-records-with-revision app [{:id 1 :revision 1}
                                              {:id 2 :revision 1}])))

  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/records.json"
                               :req {:params {:app app
                                              :ids [1 2]
                                              :revisions [1 1]}}}
                              nil)
         (<!! (r/delete-records-with-revision h/fake-conn app [{:id 1 :revision 1}
                                                               {:id 2 :revision 1}])))))

(deftest delete-all-records-by-query-test
  (testing "Fail to create cursor"
    (with-redefs [r/create-cursor
                  (fn [& _]
                    (let [c (chan)]
                      (put! c (t/->KintoneResponse nil {:status 500}))
                      c))]
      (is (= (t/->KintoneResponse nil {:status 500})
             (<!! (r/delete-all-records-by-query h/fake-conn app "order by $id"))))))

  (testing "Fail to get record on the first request"
    (with-redefs [r/create-cursor
                  (fn [& _]
                    (let [c (chan)]
                      (put! c (t/->KintoneResponse {:id "123-123"} nil))
                      c))
                  r/get-records-by-cursor
                  (fn [& _]
                    (let [c (chan)]
                      (put! c (t/->KintoneResponse nil {:status 400}))
                      c))]
      (is (= (t/->KintoneResponse nil {:status 400})
             (<!! (r/delete-all-records-by-query h/fake-conn app "order by $id"))))))

  (testing "Fail to get record on the second request"
    (let [ncall (atom 0)]
      (with-redefs [r/create-cursor
                    (fn [& _]
                      (let [c (chan)]
                        (put! c (t/->KintoneResponse {:id "123-123"} nil))
                        c))
                    r/get-records-by-cursor
                    (fn [& _]
                      (let [c (chan)]
                        (if (< @ncall 2)
                          (do (swap! ncall inc)
                              (put! c (t/->KintoneResponse {:records [{:id 1} {:id 2}]
                                                            :next true}
                                                           nil)))
                          (put! c (t/->KintoneResponse nil {:status 400})))
                        c))]
        (is (= (t/->KintoneResponse nil {:status 400})
               (<!! (r/delete-all-records-by-query h/fake-conn app "order by $id")))))))

  (testing "Get empty records"
    (with-redefs [r/create-cursor
                  (fn [& _]
                    (let [c (chan)]
                      (put! c (t/->KintoneResponse {:id "123-123"} nil))
                      c))
                  r/get-records-by-cursor
                  (fn [& _]
                    (let [c (chan)]
                      (put! c (t/->KintoneResponse {:records []} nil))
                      c))]
      (is (= (t/->KintoneResponse {} nil)
             (<!! (r/delete-all-records-by-query h/fake-conn app "order by $id"))))))

  (testing "Success"
    (let [ncall (atom 0)]
      (with-redefs [r/create-cursor
                    (fn [& _]
                      (let [c (chan)]
                        (put! c (t/->KintoneResponse {:id "123-123"} nil))
                        c))
                    r/get-records-by-cursor
                    (fn [& _]
                      (let [c (chan)]
                        (if (< @ncall 2)
                          (do (swap! ncall inc)
                              (put! c (t/->KintoneResponse {:records [{:$id {:value "1"}}
                                                                      {:$id {:value "2"}}]
                                                            :next true}
                                                           nil)))
                          (put! c (t/->KintoneResponse {:records [{:$id {:value "3"}}
                                                                  {:$id {:value "4"}}]
                                                        :next false}
                                                       nil)))
                        c))]
        (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/records.json"
                                     :req {:params {:app app
                                                    :ids [3 4]}}}
                                    nil)
               (<!! (r/delete-all-records-by-query h/fake-conn app "order by $id"))))))))

(deftest get-comments-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/record/comments.json"
                               :req {:params {:app app
                                              :record id
                                              :order "desc"
                                              :offset 0
                                              :limit 10}}}
                              nil)
         (<!! (r/get-comments h/fake-conn app id))))

  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/record/comments.json"
                               :req {:params {:app app
                                              :record id
                                              :order "asc"
                                              :offset 20
                                              :limit 50}}}
                              nil)
         (<!! (r/get-comments h/fake-conn app id {:order "asc"
                                                  :offset 20
                                                  :limit 50})))))

(deftest add-comment-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/record/comment.json"
                               :req {:params {:app app
                                              :record id
                                              :comment {:text "test comment"
                                                        :mentions nil}}}}
                              nil)
         (<!! (r/add-comment h/fake-conn app id {:text "test comment"}))))

  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/record/comment.json"
                               :req {:params {:app app
                                              :record id
                                              :comment {:text "test comment"
                                                        :mentions [{:code "foo"
                                                                    :type :USER}]}}}}
                              nil)
         (<!! (r/add-comment h/fake-conn app id {:text "test comment"
                                                 :mentions [{:code "foo"
                                                             :type :USER}]})))))

(deftest delete-comment-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/record/comment.json"
                               :req {:params {:app app
                                              :record id
                                              :comment 2}}}
                              nil)
         (<!! (r/delete-comment h/fake-conn app id 2)))))

(deftest update-record-assignees-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/record/assignees.json"
                               :req {:params {:app app
                                              :record id
                                              :assignees "foo"}}}
                              nil)
         (<!! (r/update-record-assignees h/fake-conn app id "foo" nil))))

  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/record/assignees.json"
                               :req {:params {:app app
                                              :record id
                                              :assignees "foo"
                                              :revision 11}}}
                              nil)
         (<!! (r/update-record-assignees h/fake-conn app id "foo" 11)))))

(deftest update-record-status-test
  (is (= (t/->BulkRequest :PUT "/v1/record/status.json"
                          {:app app
                           :id id
                           :action "accept"
                           :assignee ["foo" "bar"]
                           :revision 11})
         (r/update-record-status app {:id id
                                      :action "accept"
                                      :assignee ["foo" "bar"]
                                      :revision 11})))

  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/record/status.json"
                               :req {:params {:app app
                                              :id id
                                              :action "accept"
                                              :assignee ["foo" "bar"]
                                              :revision 11}}}
                              nil)
         (<!! (r/update-record-status h/fake-conn app {:id id
                                                       :action "accept"
                                                       :assignee ["foo" "bar"]
                                                       :revision 11})))))

(deftest update-records-status-test
  (is (= (t/->BulkRequest :PUT "/v1/records/status.json"
                          {:app app
                           :records [{:id id
                                      :action "accept"
                                      :assignee ["foo" "bar"]
                                      :revision 11}
                                     {:id (inc id)
                                      :action "reject"
                                      :assignee ["baz"]}]})
         (r/update-records-status app [{:id id
                                        :action "accept"
                                        :assignee ["foo" "bar"]
                                        :revision 11}
                                       {:id (inc id)
                                        :action "reject"
                                        :assignee ["baz"]
                                        :revision nil}])))

  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/records/status.json"
                               :req {:params {:app app
                                              :records [{:id id
                                                         :action "accept"
                                                         :assignee ["foo" "bar"]
                                                         :revision 11}
                                                        {:id (inc id)
                                                         :action "reject"
                                                         :assignee ["baz"]}]}}}
                              nil)
         (<!! (r/update-records-status h/fake-conn app [{:id id
                                                         :action "accept"
                                                         :assignee ["foo" "bar"]
                                                         :revision 11}
                                                        {:id (inc id)
                                                         :action "reject"
                                                         :assignee ["baz"]
                                                         :revision nil}])))))

(deftest file-upload-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/file.json"
                               :req {:multipart [{:name "file"
                                                  :content "a file"}]}}
                              nil)
         (<!! (r/file-upload h/fake-conn "a file")))))


(deftest file-download-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/k/v1/file.json"
                               :req {:params {:fileKey "a file key"}}}
                              nil)
         (<!! (r/file-download h/fake-conn "a file key")))))

(deftest bulk-request-test
  (is (= (t/->KintoneResponse
          {:url "https://test.kintone.com/k/v1/bulkRequest.json"
           :req {:params {:requests [{:api "/k/v1/record.json"
                                      :method :POST
                                      :payload {:app app
                                                :record {:name {:value "foo"}}}}
                                     {:api "/k/v1/record.json"
                                      :method :PUT
                                      :payload {:app app
                                                :id id
                                                :record {:name {:value "foo"}}}}
                                     {:api "/k/v1/records.json"
                                      :method :DELETE
                                      :payload {:app app
                                                :ids [1 2 3]}}]}}}
          nil)
         (<!! (r/bulk-request
               h/fake-conn
               [(r/add-record app {:name {:value "foo"}})
                (r/update-record app
                                 {:id id
                                  :record {:name {:value "foo"}}})
                (r/delete-records app [1 2 3])])))))

(ns kintone-client.user-test
  (:require [clojure.core.async :refer [<!!]]
            [clojure.test :refer :all]
            [kintone-client.user :as user]
            [kintone-client.test-helper :as h]
            [kintone-client.types :as t]))

(deftest get-users-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/v1/users.json"
                               :req {:params {}}}
                              nil)
         (<!! (user/get-users h/fake-conn {}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/v1/users.json"
                               :req {:params {:ids [1 2 3]
                                              :offset 2
                                              :size 10}}}
                              nil)
         (<!! (user/get-users h/fake-conn {:ids [1 2 3]
                                           :offset 2
                                           :size 10}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/v1/users.json"
                               :req {:params {:codes ["a" "b"]
                                              :offset 2
                                              :size 10}}}
                              nil)
         (<!! (user/get-users h/fake-conn {:codes ["a" "b"]
                                           :offset 2
                                           :size 10})))))

(deftest add-users-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/v1/users.json"
                               :req {:params {:users [{:code "user1"
                                                       :name "first"
                                                       :password "PASSWORD"}
                                                      {:code "user2"
                                                       :name "second"
                                                       :password "SECRET"
                                                       :valid false
                                                       :description ""}]}}}
                              nil)
         (<!! (user/add-users h/fake-conn [{:code "user1"
                                            :name "first"
                                            :password "PASSWORD"}
                                           {:code "user2"
                                            :name "second"
                                            :password "SECRET"
                                            :valid false
                                            :description ""}])))))

(deftest update-user-test
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/v1/users.json"
                               :req {:params {:users [{:code "user1"
                                                       :password "PASSWORD"}
                                                      {:code "user2"
                                                       :name "second"
                                                       :valid false
                                                       :description ""}]}}}
                              nil)
         (<!! (user/update-users h/fake-conn [{:code "user1"
                                               :password "PASSWORD"}
                                              {:code "user2"
                                               :name "second"
                                               :valid false
                                               :description ""}])))))

(deftest delete-users
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/v1/users.json"
                               :req {:params {:codes ["user1" "code"]}}}
                              nil)
         (<!! (user/delete-users h/fake-conn ["user1" "code"])))))

(deftest update-user-codes
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/v1/users/codes.json"
                               :req {:params {:codes [{:currentCode "old"
                                                       :newCode "new"}
                                                      {:currentCode "code1"
                                                       :newCode "code2"}]}}}
                              nil)
         (<!! (user/update-user-codes h/fake-conn [{:currentCode "old"
                                                    :newCode "new"}
                                                   {:currentCode "code1"
                                                    :newCode "code2"}])))))

(deftest get-organizations
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/v1/organizations.json"
                               :req {:params {}}}
                              nil)
         (<!! (user/get-organizations h/fake-conn {}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/v1/organizations.json"
                               :req {:params {:ids [1 10 20]
                                              :offset 2
                                              :size 5}}}
                              nil)
         (<!! (user/get-organizations h/fake-conn {:ids [1 10 20]
                                                   :offset 2
                                                   :size 5}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/v1/organizations.json"
                               :req {:params {:codes ["code1" "foo"]
                                              :offset 2
                                              :size 5}}}
                              nil)
         (<!! (user/get-organizations h/fake-conn {:codes ["code1" "foo"]
                                                   :offset 2
                                                   :size 5})))))

(deftest add-organizations
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/v1/organizations.json"
                               :req {:params {:organizations [{:code "org1"
                                                               :name "ORG-A"}
                                                              {:code "new"
                                                               :name "new-organization"
                                                               :parentCode "org1"
                                                               :description ""}
                                                              {:code "ABC"
                                                               :name "abc"}]}}}
                              nil)
         (<!! (user/add-organizations h/fake-conn [{:code "org1"
                                                    :name "ORG-A"}
                                                   {:code "new"
                                                    :name "new-organization"
                                                    :parentCode "org1"
                                                    :description ""}
                                                   {:code "ABC"
                                                    :name "abc"}])))))

(deftest update-organizations
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/v1/organizations.json"
                               :req {:params {:organizations [{:code "org1"
                                                               :name "ORG-A"}
                                                              {:code "new"
                                                               :name "new-organization"
                                                               :parentCode "org1"
                                                               :description ""}
                                                              {:code "ABC"
                                                               :description "abc"}]}}}
                              nil)
         (<!! (user/update-organizations h/fake-conn [{:code "org1"
                                                       :name "ORG-A"}
                                                      {:code "new"
                                                       :name "new-organization"
                                                       :parentCode "org1"
                                                       :description ""}
                                                      {:code "ABC"
                                                       :description "abc"}])))))

(deftest delete-organizations
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/v1/organizations.json"
                               :req {:params {:codes ["code1" "code2"]}}}
                              nil)
         (<!! (user/delete-organizations h/fake-conn ["code1" "code2"])))))

(deftest update-org-codes
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/v1/organizations/codes.json"
                               :req {:params {:codes [{:currentCode "old"
                                                       :newCode "new"}
                                                      {:currentCode "code1"
                                                       :newCode "code2"}]}}}
                              nil)
         (<!! (user/update-organization-codes h/fake-conn [{:currentCode "old"
                                                   :newCode "new"}
                                                  {:currentCode "code1"
                                                   :newCode "code2"}])))))

(deftest update-user-services
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/v1/users/services.json"
                               :req {:params {:users [{:code "user1"
                                                       :services ["kintone"
                                                                  "garoon"
                                                                  "office"
                                                                  "mailwise"
                                                                  "secure_access"]}
                                                      {:code "user2"
                                                       :services []}
                                                      {:code "user3"
                                                       :services ["kintone"]}]}}}
                              nil)
         (<!! (user/update-user-services h/fake-conn [{:code "user1"
                                                       :services ["kintone"
                                                                  "garoon"
                                                                  "office"
                                                                  "mailwise"
                                                                  "secure_access"]}
                                                      {:code "user2"
                                                       :services []}
                                                      {:code "user3"
                                                       :services ["kintone"]}])))))

(deftest get-groups
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/v1/groups.json"
                               :req {:params {}}}
                              nil)
         (<!! (user/get-groups h/fake-conn {}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/v1/groups.json"
                               :req {:params {:ids [1 2 3]
                                              :offset 2
                                              :size 10}}}
                              nil)
         (<!! (user/get-groups h/fake-conn {:ids [1 2 3]
                                            :offset 2
                                            :size 10}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/v1/groups.json"
                               :req {:params {:codes ["a" "b"]
                                              :offset 2
                                              :size 10}}}
                              nil)
         (<!! (user/get-groups h/fake-conn {:codes ["a" "b"]
                                            :offset 2
                                            :size 10})))))

(deftest get-user-organizations
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/v1/user/organizations.json"
                               :req {:params {:code "user1"}}}
                              nil)
         (<!! (user/get-user-organizations h/fake-conn "user1")))))

(deftest get-user-groups
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/v1/user/groups.json"
                               :req {:params {:code "user1"}}}
                              nil)
         (<!! (user/get-user-groups h/fake-conn "user1")))))

(deftest get-organization-users
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/v1/organization/users.json"
                               :req {:params {:code "org1"}}}
                              nil)
         (<!! (user/get-organization-users h/fake-conn "org1" {}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/v1/organization/users.json"
                               :req {:params {:code "org1"
                                              :offset 10
                                              :size 5}}}
                              nil)
         (<!! (user/get-organization-users h/fake-conn "org1" {:offset 10 :size 5})))))

(deftest get-group-users
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/v1/group/users.json"
                               :req {:params {:code "group1"}}}
                              nil)
         (<!! (user/get-group-users h/fake-conn "group1" {}))))
  (is (= (t/->KintoneResponse {:url "https://test.kintone.com/v1/group/users.json"
                               :req {:params {:code "group1"
                                              :offset 10
                                              :size 5}}}
                              nil)
         (<!! (user/get-group-users h/fake-conn "group1" {:offset 10 :size 5})))))


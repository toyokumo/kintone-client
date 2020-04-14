(ns kintone.connection-test
  (:require #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest is testing async]])
            #?(:clj [clj-http.client :as client]
               :cljs [ajax.core :as ajax])
            #?(:clj [clojure.core.async :refer [<!!]]
               :cljs [cljs.core.async :refer [<!] :refer-macros [go]])
            [kintone.authentication :as auth]
            [kintone.connection :refer [new-connection]]
            [kintone.protocols :as pt]
            [kintone.types :as t])
  #?(:clj
     (:import (org.apache.http.entity.mime HttpMultipartMode))))

(def ^:private auth
  (auth/new-auth {:api-token "TestApiToken"}))

(deftest new-connection-test
  (is (= {:auth auth
          :domain "sample.kintone.com",
          :guest-space-id nil,
          :connection-timeout nil,
          :socket-timeout nil,
          :timeout nil,
          :headers nil}
         (into {} (new-connection {:auth auth
                                   :domain "sample.kintone.com"})))))

(deftest -path-test
  (is (= "/k/mypath"
         (pt/-path (new-connection {:auth auth
                                    :domain "sample.kintone.com"})
                   "/mypath"))
      "Not in guest space")

  (is (= "/k/guest/2/mypath"
         (pt/-path (new-connection {:auth auth
                                    :domain "sample.kintone.com"
                                    :guest-space-id 2})
                   "/mypath"))
      "In guest space"))

(deftest -url-test
  (is (= "https://sample.kintone.com/k/mypath"
         (pt/-url (new-connection {:auth auth
                                   :domain "sample.kintone.com"})
                  "/mypath"))
      "Not in guest space")

  (is (= "https://guest.kintone.com/k/guest/2/mypath"
         (pt/-url (new-connection {:auth auth
                                   :domain "guest.kintone.com"
                                   :guest-space-id 2})
                  "/mypath"))
      "In guest space"))

(def ^:private conn
  (new-connection {:auth auth
                   :domain "sample.kintone.com"}))

(def ^:private url
  (pt/-url conn "/mypath"))

#?(:clj
   (deftest -get-test
     (testing "Positive"
       (with-redefs [client/post (fn [url req h eh]
                                   (h {:body req}))]
         (is (= (t/->KintoneResponse
                 {:headers {"X-Cybozu-API-Token" "TestApiToken"
                            "X-HTTP-Method-Override" "GET"}
                  :accept :json
                  :content-type :json
                  :as :json
                  :async? true
                  :coerce :always
                  :connection-timeout 10000
                  :socket-timeout 30000
                  :form-params {:id 1}}
                 nil)
                (<!! (pt/-get conn url {:params {:id 1}}))))))

     (testing "Negative"
       (testing "ExceptionInfo"
         (with-redefs [client/post
                       (fn [url req h eh]
                         (eh (ex-info "Test error"
                                      {:status 400
                                       :body {:message "Something bad happen"}})))]
           (is (= (t/->KintoneResponse
                   nil
                   {:status 400
                    :status-text "400"
                    :response {:message "Something bad happen"}})
                  (<!! (pt/-get conn url {:params {:id 1}}))))))

       (testing "ExceptionInfo"
         (with-redefs [client/post
                       (fn [url req h eh]
                         (eh (Exception. "Test error")))]
           (let [{:keys [status status-text response]}
                 (:err (<!! (pt/-get conn url {:params {:id 1}})))]
             (is (= status -1))
             (is (= status-text "Test error"))
             (is (= (type response) Exception))))))))

#?(:cljs
   (deftest -get-test-positive
     (async done
       (go
        (with-redefs [ajax.easy/easy-ajax-request
                      (fn [uri method opts]
                        (is (= method "POST"))
                        ((:handler opts)
                         (dissoc opts
                                 :handler
                                 :error-handler)))]
          (is (= (t/->KintoneResponse
                  {:headers {"X-Cybozu-API-Token" "TestApiToken"
                             "X-HTTP-Method-Override" "GET"}
                   :format :json
                   :response-format :json
                   :keywords? true
                   :timeout 30000
                   :params {:id 1}}
                  nil)
                 (<! (pt/-get conn url {:params {:id 1}})))))
        (done)))))

#?(:cljs
   (deftest -get-test-negative
     (async done
       (go
        (with-redefs [ajax.easy/easy-ajax-request
                      (fn [uri method opts]
                        (is (= method "POST"))
                        ((:error-handler opts)
                         {:status 400
                          :status-text "400"
                          :response {:message "Something bad happen"}}))]
          (is (= (t/->KintoneResponse
                  nil
                  {:status 400
                   :status-text "400"
                   :response {:message "Something bad happen"}})
                 (<! (pt/-get conn url {:params {:id 1}})))))
        (done)))))

#?(:clj
   (deftest -post-test
     (testing "Positive"
       (with-redefs [client/post (fn [url req h eh]
                                   (h {:body req}))]
         (is (= (t/->KintoneResponse
                 {:headers {"X-Cybozu-API-Token" "TestApiToken"}
                  :accept :json
                  :content-type :json
                  :as :json
                  :async? true
                  :coerce :always
                  :connection-timeout 10000
                  :socket-timeout 30000
                  :form-params {:id 1}}
                 nil)
                (<!! (pt/-post conn url {:params {:id 1}}))))))

     (testing "Negative"
       (testing "ExceptionInfo"
         (with-redefs [client/post
                       (fn [url req h eh]
                         (eh (ex-info "Test error"
                                      {:status 400
                                       :body {:message "Something bad happen"}})))]
           (is (= (t/->KintoneResponse
                   nil
                   {:status 400
                    :status-text "400"
                    :response {:message "Something bad happen"}})
                  (<!! (pt/-post conn url {:params {:id 1}}))))))

       (testing "ExceptionInfo"
         (with-redefs [client/post
                       (fn [url req h eh]
                         (eh (Exception. "Test error")))]
           (let [{:keys [status status-text response]}
                 (:err (<!! (pt/-post conn url {:params {:id 1}})))]
             (is (= status -1))
             (is (= status-text "Test error"))
             (is (= (type response) Exception))))))))

#?(:cljs
   (deftest -post-test-positive
     (async done
       (go
        (with-redefs [ajax.easy/easy-ajax-request
                      (fn [uri method opts]
                        (is (= method "POST"))
                        ((:handler opts)
                         (dissoc opts
                                 :handler
                                 :error-handler)))]
          (is (= (t/->KintoneResponse
                  {:headers {"X-Cybozu-API-Token" "TestApiToken"}
                   :format :json
                   :response-format :json
                   :keywords? true
                   :timeout 30000
                   :params {:id 1}}
                  nil)
                 (<! (pt/-post conn url {:params {:id 1}})))))
        (done)))))

#?(:cljs
   (deftest -post-test-negative
     (async done
       (go
        (with-redefs [ajax.easy/easy-ajax-request
                      (fn [uri method opts]
                        ((:error-handler opts)
                         {:status 400
                          :status-text "400"
                          :response {:message "Something bad happen"}}))]
          (is (= (t/->KintoneResponse
                  nil
                  {:status 400
                   :status-text "400"
                   :response {:message "Something bad happen"}})
                 (<! (pt/-post conn url {:params {:id 1}})))))
        (done)))))

#?(:clj
   (deftest -put-test
     (testing "Positive"
       (with-redefs [client/put (fn [url req h eh]
                                  (h {:body req}))]
         (is (= (t/->KintoneResponse
                 {:headers {"X-Cybozu-API-Token" "TestApiToken"}
                  :accept :json
                  :content-type :json
                  :as :json
                  :async? true
                  :coerce :always
                  :connection-timeout 10000
                  :socket-timeout 30000
                  :form-params {:id 1}}
                 nil)
                (<!! (pt/-put conn url {:params {:id 1}}))))))

     (testing "Negative"
       (testing "ExceptionInfo"
         (with-redefs [client/put
                       (fn [url req h eh]
                         (eh (ex-info "Test error"
                                      {:status 400
                                       :body {:message "Something bad happen"}})))]
           (is (= (t/->KintoneResponse
                   nil
                   {:status 400
                    :status-text "400"
                    :response {:message "Something bad happen"}})
                  (<!! (pt/-put conn url {:params {:id 1}}))))))

       (testing "ExceptionInfo"
         (with-redefs [client/put
                       (fn [url req h eh]
                         (eh (Exception. "Test error")))]
           (let [{:keys [status status-text response]}
                 (:err (<!! (pt/-put conn url {:params {:id 1}})))]
             (is (= status -1))
             (is (= status-text "Test error"))
             (is (= (type response) Exception))))))))

#?(:cljs
   (deftest -put-test-positive
     (async done
       (go
        (with-redefs [ajax.easy/easy-ajax-request
                      (fn [uri method opts]
                        (is (= method "PUT"))
                        ((:handler opts)
                         (dissoc opts
                                 :handler
                                 :error-handler)))]
          (is (= (t/->KintoneResponse
                  {:headers {"X-Cybozu-API-Token" "TestApiToken"}
                   :format :json
                   :response-format :json
                   :keywords? true
                   :timeout 30000
                   :params {:id 1}}
                  nil)
                 (<! (pt/-put conn url {:params {:id 1}})))))
        (done)))))

#?(:cljs
   (deftest -put-test-negative
     (async done
       (go
        (with-redefs [ajax.easy/easy-ajax-request
                      (fn [uri method opts]
                        ((:error-handler opts)
                         {:status 400
                          :status-text "400"
                          :response {:message "Something bad happen"}}))]
          (is (= (t/->KintoneResponse
                  nil
                  {:status 400
                   :status-text "400"
                   :response {:message "Something bad happen"}})
                 (<! (pt/-put conn url {:params {:id 1}})))))
        (done)))))

#?(:clj
   (deftest -delete-test
     (testing "Positive"
       (with-redefs [client/delete (fn [url req h eh]
                                     (h {:body req}))]
         (is (= (t/->KintoneResponse
                 {:headers {"X-Cybozu-API-Token" "TestApiToken"}
                  :accept :json
                  :content-type :json
                  :as :json
                  :async? true
                  :coerce :always
                  :connection-timeout 10000
                  :socket-timeout 30000
                  :form-params {:id 1}}
                 nil)
                (<!! (pt/-delete conn url {:params {:id 1}}))))))

     (testing "Negative"
       (testing "ExceptionInfo"
         (with-redefs [client/delete
                       (fn [url req h eh]
                         (eh (ex-info "Test error"
                                      {:status 400
                                       :body {:message "Something bad happen"}})))]
           (is (= (t/->KintoneResponse
                   nil
                   {:status 400
                    :status-text "400"
                    :response {:message "Something bad happen"}})
                  (<!! (pt/-delete conn url {:params {:id 1}}))))))

       (testing "ExceptionInfo"
         (with-redefs [client/delete
                       (fn [url req h eh]
                         (eh (Exception. "Test error")))]
           (let [{:keys [status status-text response]}
                 (:err (<!! (pt/-delete conn url {:params {:id 1}})))]
             (is (= status -1))
             (is (= status-text "Test error"))
             (is (= (type response) Exception))))))))

#?(:cljs
   (deftest -delete-test-positive
     (async done
       (go
        (with-redefs [ajax.easy/easy-ajax-request
                      (fn [uri method opts]
                        (is (= method "DELETE"))
                        ((:handler opts)
                         (dissoc opts
                                 :handler
                                 :error-handler)))]
          (is (= (t/->KintoneResponse
                  {:headers {"X-Cybozu-API-Token" "TestApiToken"}
                   :format :json
                   :response-format :json
                   :keywords? true
                   :timeout 30000
                   :params {:id 1}}
                  nil)
                 (<! (pt/-delete conn url {:params {:id 1}})))))
        (done)))))

#?(:cljs
   (deftest -delete-test-negative
     (async done
       (go
        (with-redefs [ajax.easy/easy-ajax-request
                      (fn [uri method opts]
                        ((:error-handler opts)
                         {:status 400
                          :status-text "400"
                          :response {:message "Something bad happen"}}))]
          (is (= (t/->KintoneResponse
                  nil
                  {:status 400
                   :status-text "400"
                   :response {:message "Something bad happen"}})
                 (<! (pt/-delete conn url {:params {:id 1}})))))
        (done)))))

#?(:clj
   (deftest -get-blob-test
     (testing "Positive"
       (with-redefs [client/post (fn [url req h eh]
                                   (h {:body req}))]
         (is (= (t/->KintoneResponse
                 {:headers {"X-Cybozu-API-Token" "TestApiToken"
                            "X-HTTP-Method-Override" "GET"}
                  :content-type :json
                  :async? true
                  :connection-timeout 10000
                  :socket-timeout 30000
                  :form-params {:id 1}}
                 nil)
                (<!! (pt/-get-blob conn url {:params {:id 1}}))))))

     (testing "Negative"
       (testing "ExceptionInfo"
         (with-redefs [client/post
                       (fn [url req h eh]
                         (eh (ex-info "Test error"
                                      {:status 400
                                       :body {:message "Something bad happen"}})))]
           (is (= (t/->KintoneResponse
                   nil
                   {:status 400
                    :status-text "400"
                    :response {:message "Something bad happen"}})
                  (<!! (pt/-get-blob conn url {:params {:id 1}}))))))

       (testing "ExceptionInfo"
         (with-redefs [client/post
                       (fn [url req h eh]
                         (eh (Exception. "Test error")))]
           (let [{:keys [status status-text response]}
                 (:err (<!! (pt/-get-blob conn url {:params {:id 1}})))]
             (is (= status -1))
             (is (= status-text "Test error"))
             (is (= (type response) Exception))))))))

#?(:cljs
   (deftest -get-blob-test-positive
     (async done
       (go
        (with-redefs [ajax.easy/easy-ajax-request
                      (fn [uri method opts]
                        (is (= method "POST"))
                        ((:handler opts)
                         (dissoc opts
                                 :handler
                                 :error-handler)))]
          (let [{:keys [res err]} (<! (pt/-get-blob conn url {:params {:id 1}}))]
            (is (= {:headers {"X-Cybozu-API-Token" "TestApiToken"
                              "X-HTTP-Method-Override" "GET"}
                    :keywords? true
                    :timeout 30000
                    :params {:id 1}}
                   (dissoc res :response-format)))
            (is (= err nil))
            (is (record? (:response-format res)))))
        (done)))))

#?(:cljs
   (deftest -get-blob-test-negative
     (async done
       (go
        (with-redefs [ajax.easy/easy-ajax-request
                      (fn [uri method opts]
                        ((:error-handler opts)
                         {:status 400
                          :status-text "400"
                          :response {:message "Something bad happen"}}))]
          (is (= (t/->KintoneResponse
                  nil
                  {:status 400
                   :status-text "400"
                   :response {:message "Something bad happen"}})
                 (<! (pt/-get-blob conn url {:params {:id 1}})))))
        (done)))))

#?(:clj
   (deftest -multipart-test
     (testing "Positive"
       (with-redefs [client/post (fn [url req]
                                   {:body req})]
         (is (= (t/->KintoneResponse
                 {:headers {"X-Cybozu-API-Token" "TestApiToken"}
                  :accept :json
                  :as :json
                  :coerce :always
                  :connection-timeout 10000
                  :socket-timeout 30000
                  :multipart [{:id 1}]
                  :multipart-charset "UTF-8"
                  :multipart-mode HttpMultipartMode/BROWSER_COMPATIBLE}
                 nil)
                (<!! (pt/-multipart-post conn url {:multipart [{:id 1}]}))))))

     (testing "Negative"
       (testing "ExceptionInfo"
         (with-redefs [client/post
                       (fn [url req]
                         (throw
                          (ex-info "Test error"
                                   {:status 400
                                    :body {:message "Something bad happen"}})))]
           (is (= (t/->KintoneResponse
                   nil
                   {:status 400
                    :status-text "400"
                    :response {:message "Something bad happen"}})
                  (<!! (pt/-multipart-post conn url {:multipart [{:id 1}]}))))))

       (testing "ExceptionInfo"
         (with-redefs [client/post
                       (fn [url req]
                         (throw (Exception. "Test error")))]
           (let [{:keys [status status-text response]}
                 (:err (<!! (pt/-multipart-post conn url {:multipart [{:id 1}]})))]
             (is (= status -1))
             (is (= status-text "Test error"))
             (is (= (type response) Exception))))))))

#?(:cljs
   (deftest -multipart-post-test-positive
     (async done
       (go
        (with-redefs [ajax.easy/easy-ajax-request
                      (fn [uri method opts]
                        (is (= method "POST"))
                        ((:handler opts)
                         (dissoc opts
                                 :handler
                                 :error-handler)))]
          (let [{:keys [res err]} (<! (pt/-multipart-post conn url {:multipart [{:id 1}]}))]
            (is (= {:headers {"X-Cybozu-API-Token" "TestApiToken"}
                    :response-format :json
                    :keywords? true
                    :timeout 30000
                    :body [{:id 1}]}
                   res))
            (is (= err nil))))
        (done)))))

#?(:cljs
   (deftest -multipart-post-test-negative
     (async done
       (go
        (with-redefs [ajax.easy/easy-ajax-request
                      (fn [uri method opts]
                        ((:error-handler opts)
                         {:status 400
                          :status-text "400"
                          :response {:message "Something bad happen"}}))]
          (is (= (t/->KintoneResponse
                  nil
                  {:status 400
                   :status-text "400"
                   :response {:message "Something bad happen"}})
                 (<! (pt/-multipart-post conn url {:multipart [{:id 1}]})))))
        (done)))))


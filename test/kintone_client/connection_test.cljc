(ns kintone-client.connection-test
  (:require #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest is testing async]])
            #?(:clj [clj-http.client :as client]
               :cljs [ajax.core :as ajax])
            #?(:clj [clojure.core.async :refer [<!!]]
               :cljs [cljs.core.async :refer [<!] :refer-macros [go]])
            [kintone-client.authentication :as auth]
            [kintone-client.connection :refer [new-connection]]
            [kintone-client.protocols :as pt]
            [kintone-client.types :as t])
  #?(:clj
     (:import (org.apache.http.entity.mime HttpMultipartMode))))

(def ^:private auth
  (auth/new-auth {:api-token "TestApiToken"}))

(deftest new-connection-test
  (is (= {:auth auth
          :domain "sample.kintone.com"
          :guest-space-id nil
          :handler nil
          :error-handler nil
          :connection-timeout nil
          :socket-timeout nil
          :timeout nil,
          :headers nil}
         (into {} (new-connection {:auth auth
                                   :domain "sample.kintone.com"}))))

  (is (= {:auth auth
          :domain "sample.kintone.com"
          :guest-space-id nil
          :handler println
          :error-handler prn
          :connection-timeout nil
          :socket-timeout nil
          :timeout nil
          :headers nil}
         (into {} (new-connection {:auth auth
                                   :domain "sample.kintone.com"
                                   :handler println
                                   :error-handler prn})))))

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

(deftest -user-api-url-test
  (is (= "https://sample.kintone.com/mypath"
         (pt/-user-api-url (new-connection {:auth auth
                                            :domain "sample.kintone.com"})
                           "/mypath"))
      "Not in guest space")

  (is (= "https://guest.kintone.com/mypath"
         (pt/-user-api-url (new-connection {:auth auth
                                            :domain "guest.kintone.com"
                                            :guest-space-id 2})
                           "/mypath"))
      "In guest space"))

(def ^:private conn
  (new-connection {:auth auth
                   :domain "sample.kintone.com"}))

(def ^:private url
  (pt/-url conn "/mypath"))

(def ^:private user-api-url
  (pt/-user-api-url conn "/mypath"))

#?(:clj
   (deftest -get-test
     (testing "Positive"
       (testing "default-handler"
         (with-redefs [client/post (fn [url req h eh]
                                     (h {:body req}))]
           (is (= (t/->KintoneResponse
                   {:headers {"X-Cybozu-API-Token" "TestApiToken"
                              "X-HTTP-Method-Override" "GET"}
                    :accept :json
                    :content-type :json
                    :as :json
                    :async? true
                    :coerce :unexceptional
                    :connection-timeout 10000
                    :socket-timeout 30000
                    :form-params {:id 1}}
                   nil)
                  (<!! (pt/-get conn url {:params {:id 1}}))))))

       (testing "default-handler-user-api"
         (with-redefs [client/get (fn [url req h eh]
                                    (h {:body req}))]
           (is (= (t/->KintoneResponse
                   {:headers {"X-Cybozu-API-Token" "TestApiToken"}
                    :accept :json
                    :as :json
                    :async? true
                    :coerce :unexceptional
                    :connection-timeout 10000
                    :socket-timeout 30000
                    :query-params {:id 1}}
                   nil)
                  (<!! (pt/-get conn user-api-url {:params {:id 1}}))))))

       (testing "custom handler"
         (with-redefs [client/post (fn [url req h eh]
                                     (h {:body req}))]
           (is (= (t/->KintoneResponse
                   {:body {:headers {"X-Cybozu-API-Token" "TestApiToken"
                                     "X-HTTP-Method-Override" "GET"}
                           :accept :json
                           :content-type :json
                           :as :json
                           :async? true
                           :coerce :unexceptional
                           :connection-timeout 10000
                           :socket-timeout 30000
                           :form-params {:id 1}}}
                   nil)
                  (<!! (pt/-get (assoc conn :handler identity)
                                url
                                {:params {:id 1}}))))))

       (testing "custom handler user api"
         (with-redefs [client/get (fn [url req h eh]
                                    (h {:body req}))]
           (is (= (t/->KintoneResponse
                   {:body {:headers {"X-Cybozu-API-Token" "TestApiToken"}
                           :accept :json
                           :as :json
                           :async? true
                           :coerce :unexceptional
                           :connection-timeout 10000
                           :socket-timeout 30000
                           :query-params {:id 1}}}
                   nil)
                  (<!! (pt/-get (assoc conn :handler identity)
                                user-api-url
                                {:params {:id 1}})))))))

     (testing "Negative"
       (testing "ExceptionInfo"
         (testing "JSON response"
           (with-redefs [client/post
                         (fn [url req h eh]
                           (eh (ex-info "Test error"
                                        {:status 400
                                         :headers {"Content-Type" "application/json; charset=utf-8"}
                                         :body "{\"message\":\"Something bad happen\"}"})))]
             (is (= (t/->KintoneResponse
                     nil
                     {:status 400
                      :status-text "400"
                      :response {:message "Something bad happen"}})
                    (<!! (pt/-get conn url {:params {:id 1}}))))))

         (testing "JSON response user api"
           (with-redefs [client/get
                         (fn [url req h eh]
                           (eh (ex-info "Test error"
                                        {:status 400
                                         :headers {"Content-Type" "application/json; charset=utf-8"}
                                         :body "{\"message\":\"Something bad happen\"}"})))]
             (is (= (t/->KintoneResponse
                     nil
                     {:status 400
                      :status-text "400"
                      :response {:message "Something bad happen"}})
                    (<!! (pt/-get conn user-api-url {:params {:id 1}}))))))

         (testing "HTML response"
           (with-redefs [client/post
                         (fn [url req h eh]
                           (eh (ex-info "Test error"
                                        {:status 400
                                         :headers {"Content-Type" "text/html; charset=utf-8"}
                                         :body "<!DOCTYPE html><html></html>"})))]
             (is (= (t/->KintoneResponse
                     nil
                     {:status 400
                      :status-text "400"
                      :response "<!DOCTYPE html><html></html>"})
                    (<!! (pt/-get conn url {:params {:id 1}}))))))

         (testing "HTML response user api"
           (with-redefs [client/get
                         (fn [url req h eh]
                           (eh (ex-info "Test error"
                                        {:status 400
                                         :headers {"Content-Type" "text/html; charset=utf-8"}
                                         :body "<!DOCTYPE html><html></html>"})))]
             (is (= (t/->KintoneResponse
                     nil
                     {:status 400
                      :status-text "400"
                      :response "<!DOCTYPE html><html></html>"})
                    (<!! (pt/-get conn user-api-url {:params {:id 1}}))))))

         (testing "custom error-handler"
           (with-redefs [client/post
                         (fn [url req h eh]
                           (eh (ex-info "Test error"
                                        {:status 400
                                         :headers {"Content-Type" "application/json; charset=utf-8"}
                                         :body "{\"message\":\"Something bad happen\"}"})))]
             (is (= (t/->KintoneResponse
                     nil
                     "{\"message\":\"Something bad happen\"}")
                    (<!! (pt/-get (assoc conn :error-handler (fn [err] (:body (ex-data err))))
                                  url
                                  {:params {:id 1}}))))))

         (testing "custom error-handler user api"
           (with-redefs [client/get
                         (fn [url req h eh]
                           (eh (ex-info "Test error"
                                        {:status 400
                                         :headers {"Content-Type" "application/json; charset=utf-8"}
                                         :body "{\"message\":\"Something bad happen\"}"})))]
             (is (= (t/->KintoneResponse
                     nil
                     "{\"message\":\"Something bad happen\"}")
                    (<!! (pt/-get (assoc conn :error-handler (fn [err] (:body (ex-data err))))
                                  user-api-url
                                  {:params {:id 1}})))))))

       (testing "Exception"
         (with-redefs [client/post
                       (fn [url req h eh]
                         (eh (Exception. "Test error")))]
           (let [{:keys [status status-text response]}
                 (:err (<!! (pt/-get conn url {:params {:id 1}})))]
             (is (= status -1))
             (is (= status-text "Test error"))
             (is (= (type response) Exception)))))

       (testing "Exception user api"
         (with-redefs [client/get
                       (fn [url req h eh]
                         (eh (Exception. "Test error")))]
           (let [{:keys [status status-text response]}
                 (:err (<!! (pt/-get conn user-api-url {:params {:id 1}})))]
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
                             "X-HTTP-Method-Override" "GET"
                             "X-Requested-With" "XMLHttpRequest"}
                   :format :json
                   :response-format :json
                   :keywords? true
                   :timeout 30000
                   :params {:id 1}}
                  nil)
                 (<! (pt/-get conn url {:params {:id 1}})))))
        (done)))))

#?(:cljs
   (deftest -get-test-positive-user-api
     (async done
       (go
        (with-redefs [ajax.easy/easy-ajax-request
                      (fn [uri method opts]
                        (is (= method "GET"))
                        ((:handler opts)
                         (dissoc opts
                                 :handler
                                 :error-handler)))]
          (is (= (t/->KintoneResponse
                  {:headers {"X-Cybozu-API-Token" "TestApiToken"
                             "X-Requested-With" "XMLHttpRequest"}
                   :format :json
                   :response-format :json
                   :keywords? true
                   :timeout 30000
                   :query-params {:id 1}}
                  nil)
                 (<! (pt/-get conn user-api-url {:params {:id 1}})))))
        (done)))))

#?(:cljs
   (deftest -get-test-positive-with-custom-handler
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
                  {:id 1}
                  nil)
                 (<! (pt/-get (assoc conn :handler :params)
                              url
                              {:params {:id 1}})))))
        (done)))))

#?(:cljs
   (deftest -get-test-positive-with-custom-handler-user-api
     (async done
       (go
        (with-redefs [ajax.easy/easy-ajax-request
                      (fn [uri method opts]
                        (is (= method "GET"))
                        ((:handler opts)
                         (dissoc opts
                                 :handler
                                 :error-handler)))]
          (is (= (t/->KintoneResponse
                  {:id 1}
                  nil)
                 (<! (pt/-get (assoc conn :handler :query-params)
                              user-api-url
                              {:params {:id 1}})))))
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

#?(:cljs
   (deftest -get-test-negative-user-api
     (async done
       (go
        (with-redefs [ajax.easy/easy-ajax-request
                      (fn [uri method opts]
                        (is (= method "GET"))
                        ((:error-handler opts)
                         {:status 400
                          :status-text "400"
                          :response {:message "Something bad happen"}}))]
          (is (= (t/->KintoneResponse
                  nil
                  {:status 400 :status-text "400"
                   :response {:message "Something bad happen"}})
                 (<! (pt/-get conn user-api-url {:params {:id 1}})))))
        (done)))))

#?(:cljs
   (deftest -get-test-negative-with-custom-error-handler
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
                  {:message "Something bad happen"})
                 (<! (pt/-get (assoc conn :error-handler :response)
                              url
                              {:params {:id 1}})))))
        (done)))))

#?(:cljs
   (deftest -get-test-negative-with-custom-error-handler-user-api
     (async done
       (go
        (with-redefs [ajax.easy/easy-ajax-request
                      (fn [uri method opts]
                        (is (= method "GET"))
                        ((:error-handler opts)
                         {:status 400
                          :status-text "400"
                          :response {:message "Something bad happen"}}))]
          (is (= (t/->KintoneResponse
                  nil
                  {:message "Something bad happen"})
                 (<! (pt/-get (assoc conn :error-handler :response)
                              user-api-url
                              {:params {:id 1}})))))
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
                  :coerce :unexceptional
                  :connection-timeout 10000
                  :socket-timeout 30000
                  :form-params {:id 1}}
                 nil)
                (<!! (pt/-post conn url {:params {:id 1}}))))))

     (testing "Positive but custom handler"
       (with-redefs [client/post (fn [url req h eh]
                                   (h {:body req}))]
         (is (= (t/->KintoneResponse
                 {:id 1}
                 nil)
                (<!! (pt/-post (assoc conn :handler (comp :form-params :body))
                               url
                               {:params {:id 1}}))))))

     (testing "Negative"
       (testing "ExceptionInfo"
         (testing "JSON response"
           (with-redefs [client/post
                         (fn [url req h eh]
                           (eh (ex-info "Test error"
                                        {:status 400
                                         :headers {"Content-Type" "application/json; charset=utf-8"}
                                         :body "{\"message\":\"Something bad happen\"}"})))]
             (is (= (t/->KintoneResponse
                     nil
                     {:status 400
                      :status-text "400"
                      :response {:message "Something bad happen"}})
                    (<!! (pt/-post conn url {:params {:id 1}}))))))

         (testing "HTML response"
           (with-redefs [client/post
                         (fn [url req h eh]
                           (eh (ex-info "Test error"
                                        {:status 400
                                         :headers {"Content-Type" "text/html; charset=utf-8"}
                                         :body "<!DOCTYPE html><html></html>"})))]
             (is (= (t/->KintoneResponse
                     nil
                     {:status 400
                      :status-text "400"
                      :response "<!DOCTYPE html><html></html>"})
                    (<!! (pt/-post conn url {:params {:id 1}}))))))

         (testing "custom error-handler"
           (with-redefs [client/post
                         (fn [url req h eh]
                           (eh (ex-info "Test error"
                                        {:status 400
                                         :headers {"Content-Type" "text/html; charset=utf-8"}
                                         :body "<!DOCTYPE html><html></html>"})))]
             (is (= (t/->KintoneResponse
                     nil
                     "<!DOCTYPE html><html></html>")
                    (<!! (pt/-post (assoc conn :error-handler (fn [err] (:body (ex-data err))))
                                   url
                                   {:params {:id 1}})))))))

       (testing "Exception"
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
                  {:headers {"X-Cybozu-API-Token" "TestApiToken"
                             "X-Requested-With" "XMLHttpRequest"}
                   :format :json
                   :response-format :json
                   :keywords? true
                   :timeout 30000
                   :params {:id 1}}
                  nil)
                 (<! (pt/-post conn url {:params {:id 1}})))))
        (done)))))

#?(:cljs
   (deftest -post-test-positive-with-custom-handler
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
                  {:id 1}
                  nil)
                 (<! (pt/-post (assoc conn :handler :params)
                               url
                               {:params {:id 1}})))))
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

#?(:cljs
   (deftest -post-test-negative-with-custom-handler
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
                  400)
                 (<! (pt/-post (assoc conn :error-handler :status)
                               url
                               {:params {:id 1}})))))
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
                  :coerce :unexceptional
                  :connection-timeout 10000
                  :socket-timeout 30000
                  :form-params {:id 1}}
                 nil)
                (<!! (pt/-put conn url {:params {:id 1}}))))))

     (testing "Positive but custom handler"
       (with-redefs [client/put (fn [url req h eh]
                                  (h {:body req}))]
         (is (= (t/->KintoneResponse
                 {:id 1}
                 nil)
                (<!! (pt/-put (assoc conn :handler (comp :form-params :body))
                              url
                              {:params {:id 1}}))))))

     (testing "Negative"
       (testing "ExceptionInfo"
         (testing "JSON response"
           (with-redefs [client/put
                         (fn [url req h eh]
                           (eh (ex-info "Test error"
                                        {:status 400
                                         :headers {"Content-Type" "application/json; charset=utf-8"}
                                         :body "{\"message\":\"Something bad happen\"}"})))]
             (is (= (t/->KintoneResponse
                     nil
                     {:status 400
                      :status-text "400"
                      :response {:message "Something bad happen"}})
                    (<!! (pt/-put conn url {:params {:id 1}}))))))

         (testing "HTML response"
           (with-redefs [client/put
                         (fn [url req h eh]
                           (eh (ex-info "Test error"
                                        {:status 400
                                         :headers {"Content-Type" "text/html; charset=utf-8"}
                                         :body "<!DOCTYPE html><html></html>"})))]
             (is (= (t/->KintoneResponse
                     nil
                     {:status 400
                      :status-text "400"
                      :response "<!DOCTYPE html><html></html>"})
                    (<!! (pt/-put conn url {:params {:id 1}}))))))

         (testing "custom error-handler"
           (with-redefs [client/put
                         (fn [url req h eh]
                           (eh (ex-info "Test error"
                                        {:status 400
                                         :headers {"Content-Type" "application/json; charset=utf-8"}
                                         :body "{\"message\":\"Something bad happen\"}"})))]
             (is (= (t/->KintoneResponse
                     nil
                     "{\"message\":\"Something bad happen\"}")
                    (<!! (pt/-put (assoc conn :error-handler (comp :body ex-data))
                                  url
                                  {:params {:id 1}})))))))

       (testing "Exception"
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
                  {:headers {"X-Cybozu-API-Token" "TestApiToken"
                             "X-Requested-With" "XMLHttpRequest"}
                   :format :json
                   :response-format :json
                   :keywords? true
                   :timeout 30000
                   :params {:id 1}}
                  nil)
                 (<! (pt/-put conn url {:params {:id 1}})))))
        (done)))))

#?(:cljs
   (deftest -put-test-positive-with-custom-handler
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
                  {:headers {"X-Cybozu-API-Token" "TestApiToken"
                             "X-Requested-With" "XMLHttpRequest"}
                   :format :json
                   :response-format :json
                   :keywords? true
                   :timeout 30000
                   :params {:id 2}}
                  nil)
                 (<! (pt/-put (assoc conn :handler #(update-in % [:params :id] inc))
                              url
                              {:params {:id 1}})))))
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

#?(:cljs
   (deftest -put-test-negative-with-custom-error-handler
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
                  "Something bad happen")
                 (<! (pt/-put (assoc conn :error-handler #(get-in % [:response :message]))
                              url
                              {:params {:id 1}})))))
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
                  :coerce :unexceptional
                  :connection-timeout 10000
                  :socket-timeout 30000
                  :form-params {:id 1}}
                 nil)
                (<!! (pt/-delete conn url {:params {:id 1}}))))))

     (testing "Positive but custom handler"
       (with-redefs [client/delete (fn [url req h eh]
                                     (h {:body req}))]
         (is (= (t/->KintoneResponse
                 {:id 1}
                 nil)
                (<!! (pt/-delete (assoc conn :handler (comp :form-params :body))
                                 url
                                 {:params {:id 1}}))))))

     (testing "Negative"
       (testing "ExceptionInfo"
         (testing "JSON response"
           (with-redefs [client/delete
                         (fn [url req h eh]
                           (eh (ex-info "Test error"
                                        {:status 400
                                         :headers {"Content-Type" "application/json; charset=utf-8"}
                                         :body "{\"message\":\"Something bad happen\"}"})))]
             (is (= (t/->KintoneResponse
                     nil
                     {:status 400
                      :status-text "400"
                      :response {:message "Something bad happen"}})
                    (<!! (pt/-delete conn url {:params {:id 1}}))))))

         (testing "HTML response"
           (with-redefs [client/delete
                         (fn [url req h eh]
                           (eh (ex-info "Test error"
                                        {:status 400
                                         :headers {"Content-Type" "text/html; charset=utf-8"}
                                         :body "<!DOCTYPE html><html></html>"})))]
             (is (= (t/->KintoneResponse
                     nil
                     {:status 400
                      :status-text "400"
                      :response "<!DOCTYPE html><html></html>"})
                    (<!! (pt/-delete conn url {:params {:id 1}}))))))

         (testing "custom error-handler"
           (with-redefs [client/delete
                         (fn [url req h eh]
                           (eh (ex-info "Test error"
                                        {:status 400
                                         :headers {"Content-Type" "application/json; charset=utf-8"}
                                         :body "{\"message\":\"Something bad happen\"}"})))]
             (is (= (t/->KintoneResponse
                     nil
                     "{\"message\":\"Something bad happen\"}")
                    (<!! (pt/-delete (assoc conn :error-handler (comp :body ex-data))
                                     url
                                     {:params {:id 1}})))))))

       (testing "Exception"
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
                  {:headers {"X-Cybozu-API-Token" "TestApiToken"
                             "X-Requested-With" "XMLHttpRequest"}
                   :format :json
                   :response-format :json
                   :keywords? true
                   :timeout 30000
                   :params {:id 1}}
                  nil)
                 (<! (pt/-delete conn url {:params {:id 1}})))))
        (done)))))

#?(:cljs
   (deftest -delete-test-positive-with-custom-handler
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
                  {:id 1}
                  nil)
                 (<! (pt/-delete (assoc conn :handler :params)
                                 url
                                 {:params {:id 1}})))))
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

#?(:cljs
   (deftest -delete-test-negative-with-custom-error-handler
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
                  {:message "Something bad happen"})
                 (<! (pt/-delete (assoc conn :error-handler :response)
                                 url
                                 {:params {:id 1}})))))
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
                  :form-params {:id 1}
                  :as :byte-array}
                 nil)
                (<!! (pt/-get-blob conn url {:params {:id 1}}))))

         (is (= (t/->KintoneResponse
                 {:headers {"X-Cybozu-API-Token" "TestApiToken"
                            "X-HTTP-Method-Override" "GET"}
                  :content-type :json
                  :async? true
                  :connection-timeout 10000
                  :socket-timeout 30000
                  :form-params {:id 1}
                  :as :stream}
                 nil)
                (<!! (pt/-get-blob conn url {:params {:id 1}
                                             :as :stream}))))))

     (testing "Positive but custom handler"
       (with-redefs [client/post (fn [url req h eh]
                                   (h {:body req}))]
         (is (= (t/->KintoneResponse
                 {:id 1}
                 nil)
                (<!! (pt/-get-blob (assoc conn :handler (comp :form-params :body))
                                   url
                                   {:params {:id 1}}))))))

     (testing "Negative"
       (testing "ExceptionInfo"
         (testing "JSON response"
           (with-redefs [client/post
                         (fn [url req h eh]
                           (eh (ex-info "Test error"
                                        {:status 400
                                         :headers {"Content-Type" "application/json; charset=utf-8"}
                                         :body "{\"message\":\"Something bad happen\"}"})))]
             (is (= (t/->KintoneResponse
                     nil
                     {:status 400
                      :status-text "400"
                      :response {:message "Something bad happen"}})
                    (<!! (pt/-get-blob conn url {:params {:id 1}}))))))

         (testing "HTML response"
           (with-redefs [client/post
                         (fn [url req h eh]
                           (eh (ex-info "Test error"
                                        {:status 400
                                         :headers {"Content-Type" "text/html; charset=utf-8"}
                                         :body "<!DOCTYPE html><html></html>"})))]
             (is (= (t/->KintoneResponse
                     nil
                     {:status 400
                      :status-text "400"
                      :response "<!DOCTYPE html><html></html>"})
                    (<!! (pt/-get-blob conn url {:params {:id 1}}))))))

         (testing "custom error-handler"
           (with-redefs [client/post
                         (fn [url req h eh]
                           (eh (ex-info "Test error"
                                        {:status 400
                                         :headers {"Content-Type" "application/json; charset=utf-8"}
                                         :body "{\"message\":\"Something bad happen\"}"})))]
             (is (= (t/->KintoneResponse
                     nil
                     "{\"message\":\"Something bad happen\"}")
                    (<!! (pt/-get-blob (assoc conn :error-handler (comp :body ex-data))
                                       url
                                       {:params {:id 1}})))))))

       (testing "Exception"
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
                              "X-HTTP-Method-Override" "GET"
                              "X-Requested-With" "XMLHttpRequest"}
                    :keywords? true
                    :timeout 30000
                    :params {:id 1}}
                   (dissoc res :response-format)))
            (is (= err nil))
            (is (record? (:response-format res)))))
        (done)))))

#?(:cljs
   (deftest -get-blob-test-positive-with-custom-handler
     (async done
       (go
        (with-redefs [ajax.easy/easy-ajax-request
                      (fn [uri method opts]
                        (is (= method "POST"))
                        ((:handler opts)
                         (dissoc opts
                                 :handler
                                 :error-handler)))]
          (let [{:keys [res err]} (<! (pt/-get-blob (assoc conn :handler :params)
                                                    url
                                                    {:params {:id 1}}))]
            (is (= {:id 1}
                   res))
            (is (= err nil))))
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

#?(:cljs
   (deftest -get-blob-test-negative-with-custom-error-handler
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
                  {:message "Something bad happen"})
                 (<! (pt/-get-blob (assoc conn :error-handler :response)
                                   url
                                   {:params {:id 1}})))))
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
                  :coerce :unexceptional
                  :connection-timeout 10000
                  :socket-timeout 30000
                  :multipart [{:id 1}]
                  :multipart-charset "UTF-8"
                  :multipart-mode HttpMultipartMode/BROWSER_COMPATIBLE}
                 nil)
                (<!! (pt/-multipart-post conn url {:multipart [{:id 1}]}))))))

     (testing "Positive but custom handler"
       (with-redefs [client/post (fn [url req]
                                   {:body req})]
         (is (= (t/->KintoneResponse
                 [{:id 1}]
                 nil)
                (<!! (pt/-multipart-post (assoc conn :handler (comp :multipart :body))
                                         url
                                         {:multipart [{:id 1}]}))))))

     (testing "Negative"
       (testing "ExceptionInfo"
         (testing "JSON response"
           (with-redefs [client/post
                         (fn [url req]
                           (throw
                            (ex-info "Test error"
                                     {:status 400
                                      :headers {"Content-Type" "application/json; charset=utf-8"}
                                      :body "{\"message\":\"Something bad happen\"}"})))]
             (is (= (t/->KintoneResponse
                     nil
                     {:status 400
                      :status-text "400"
                      :response {:message "Something bad happen"}})
                    (<!! (pt/-multipart-post conn url {:multipart [{:id 1}]}))))))

         (testing "HTML response"
           (with-redefs [client/post
                         (fn [url req]
                           (throw
                            (ex-info "Test error"
                                     {:status 400
                                      :headers {"Content-Type" "text/html; charset=utf-8"}
                                      :body "<!DOCTYPE html><html></html>"})))]
             (is (= (t/->KintoneResponse
                     nil
                     {:status 400
                      :status-text "400"
                      :response "<!DOCTYPE html><html></html>"})
                    (<!! (pt/-multipart-post conn url {:multipart [{:id 1}]}))))))

         (testing "custom error-handler"
           (with-redefs [client/post
                         (fn [url req]
                           (throw
                            (ex-info "Test error"
                                     {:status 400
                                      :headers {"Content-Type" "application/json; charset=utf-8"}
                                      :body "{\"message\":\"Something bad happen\"}"})))]
             (is (= (t/->KintoneResponse
                     nil
                     "{\"message\":\"Something bad happen\"}")
                    (<!! (pt/-multipart-post (assoc conn :error-handler (comp :body ex-data))
                                             url
                                             {:multipart [{:id 1}]})))))))

       (testing "Exception"
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
            (is (= {:headers {"X-Cybozu-API-Token" "TestApiToken"
                              "X-Requested-With" "XMLHttpRequest"}
                    :response-format :json
                    :keywords? true
                    :timeout 30000
                    :body [{:id 1}]}
                   res))
            (is (= err nil))))
        (done)))))

#?(:cljs
   (deftest -multipart-post-test-positive-with-handler
     (async done
       (go
        (with-redefs [ajax.easy/easy-ajax-request
                      (fn [uri method opts]
                        (is (= method "POST"))
                        ((:handler opts)
                         (dissoc opts
                                 :handler
                                 :error-handler)))]
          (let [{:keys [res err]} (<! (pt/-multipart-post (assoc conn :handler :body)
                                                          url
                                                          {:multipart [{:id 1}]}))]
            (is (= [{:id 1}]
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

#?(:cljs
   (deftest -multipart-post-test-negative-with-custom-error-handler
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
                  {:message "Something bad happen"})
                 (<! (pt/-multipart-post (assoc conn :error-handler :response)
                                         url
                                         {:multipart [{:id 1}]})))))
        (done)))))

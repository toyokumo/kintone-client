(ns kintone-client.authentication-test
  (:require
   #?(:clj [clojure.test :refer [deftest is]]
      :cljs [cljs.test :refer-macros [deftest is]])
   [kintone-client.authentication :refer [new-auth]]
   [kintone-client.protocols :as pt]))

(deftest new-auth-test
  #?(:cljs
     (is (= {} (pt/-header (new-auth)))))

  (is (= {} (pt/-header (new-auth nil))))

  (is (= {"X-Cybozu-API-Token" "XXXYYYXXX"}
         (pt/-header (new-auth {:api-token "XXXYYYXXX"}))))

  (is (= {"Authorization" "Basic Zm9vZm9vOmJhcmJhcg=="
          "X-Cybozu-Authorization" "Zm9vZm9vOmJhcmJhcg=="}
         (pt/-header (new-auth {:basic {:username "foofoo"
                                        :password "barbar"}
                                :password {:username "foofoo"
                                           :password "barbar"}}))))

  (is (= {"Authorization" "Basic Zm9vZm9vOmJhcmJhcg=="
          "X-Cybozu-Authorization" "Zm9vZm9vOmJhcmJhcg=="
          "X-Cybozu-API-Token" "XXXYYYXXX"}
         (pt/-header (new-auth {:basic {:username "foofoo"
                                        :password "barbar"}
                                :password {:username "foofoo"
                                           :password "barbar"}
                                :api-token "XXXYYYXXX"}))))

  (is (= {"Authorization" "Bearer XXXYYYXXX"}
         (pt/-header (new-auth {:user-api-token "XXXYYYXXX"}))))

  (is (thrown? #?(:clj AssertionError :cljs js/Error)
        (new-auth {:basic {:username "foofoo" :password "barbar"}
                   :user-api-token "XXXYYYXXX"}))))

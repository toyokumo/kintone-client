(ns kintone.authentication-test
  (:require #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest is]])))

(deftest new-auth-test
  (is (= 2 (inc 1))))

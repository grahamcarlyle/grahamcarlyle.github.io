(ns com.grahamcarlyle.blog.build-test
  (:require
    [clojure.test :refer [deftest is]]
    [com.grahamcarlyle.blog.build :as build]))


(deftest substitute-test
  (is (= [:a "hello"]
         (build/substitute [:a :foo] {:foo "hello"}))))

(deftest qualify-plain-keyword-keys-test
  (is (= {:e/a :b
          :foo/c :d
          "b" "a"}
         (build/qualify-plain-keyword-keys {:a :b
                                            :foo/c :d
                                            "b" "a"}
                                           "e"))))
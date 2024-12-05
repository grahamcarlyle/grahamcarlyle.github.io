(ns com.grahamcarlyle.blog.posts-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [com.grahamcarlyle.blog.posts :as posts]))

(deftest parse-posts-test
  (testing "Parse simple markdown"
    (is (= {:meta   nil
            :hiccup [:div [:p "This is a post"]]}
           (posts/parse "This is a post"))))

  (testing "Parse markdown with yaml front matter metadata"
    (is (= {:meta   {:title "First post"
                     :tags ["foo" "bar"]}
            :hiccup [:div [:p "This is a post"]]}
           (posts/parse
             "---
title: First post
tags:
 - foo
 - bar
---
This is a post"))))

  (testing "Parse markdown with liquid like templating"
    (is (= [:div [:p "2 + 1 = " [::posts/template " (+ 2 1) "]]]
           (:hiccup (posts/parse
                      "2 + 1 = {{ (+ 2 1) }}"))))))

(deftest eval-template-test
  (testing "simple expression evaluation"
    (is (= "3" (posts/eval-template [::posts/template "(+ 1 2)"]))))
  (testing "nested expression evaluation"
    (is (= [:div "3"] (posts/eval-template [:div [::posts/template "(+ 1 2)"]]))))
  (testing "reference variable in context"
    (is (= "4" (posts/eval-template [::posts/template "(+ 1 a)"] {'a 3})))))

(ns com.grahamcarlyle.blog.posts-test
  (:require
    [clojure.test :refer [deftest is testing]]
    [com.grahamcarlyle.blog.posts :as posts]))

(deftest read-posts-test
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
This is a post")))))

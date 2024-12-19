(ns com.grahamcarlyle.blog.posts-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [com.grahamcarlyle.blog.posts :as posts]))

(deftest parse-posts-test
  (testing "Parse simple markdown"
    (is (= {:front-matter nil
            :content      [:div [:p "This is a post"]]}
           (posts/parse "This is a post"))))

  (testing "Parse markdown with yaml front matter metadata"
    (is (= {:front-matter {:title "First post"
                           :tags       ["foo" "bar"]}
            :content      [:div [:p "This is a post"]]}
           (posts/parse
             "---
title: First post
tags:
 - foo
 - bar
---
This is a post"))))

  (testing "Parse markdown with liquid like templating"
    (is (= [:div [:p "2 + 1 = " "3"]]
           (:content (posts/parse
                       "2 + 1 = {{ (+ 2 1) }}")))))
  (testing "Parse markdown with template referencing front-matter"
    (is (= [:div [:p "Hello " "Alice"]]
           (:content (posts/parse
                       "---
name: Alice
---
Hello {{name}}"))))))

(deftest render-post-test
  (testing "a templated markdown post referencing the front matter meta variables"
    (is (= [:html
            [:head [:title "A title"]]
            [:body
             [:div
              [:h1 {:id "a-title"} "A title"]
              [:p "This is a " "short" " post."]]]]
           (posts/render "---
template: simple
title: A title
adjective: short
---
# {{ title }}
This is a {{ adjective }} post."
             {:templates {:simple (fn [{:keys [front-matter content]}]
                                    [:html
                                     [:head [:title (:title front-matter)]]
                                     [:body content]])}}))))
  (testing "no template markdown post"
    (is (= [:div [:p "This is a " "short" " post."]]
           (posts/render "---
adjective: short
---
This is a {{ adjective }} post."))))
  (testing "TODO Unknown template"))

(ns com.grahamcarlyle.blog.posts-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [com.grahamcarlyle.blog.posts :as posts]))

(deftest parse-posts-test
  (testing "Parse simple markdown"
    (is (= {:meta    nil
            :content [:div [:p "This is a post"]]}
           (posts/parse "This is a post"))))

  (testing "Parse markdown with yaml front matter metadata"
    (is (= {:meta    {:title "First post"
                      :tags  ["foo" "bar"]}
            :content [:div [:p "This is a post"]]}
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
           (:content (posts/parse
                       "2 + 1 = {{ (+ 2 1) }}"))))))

(deftest eval-template-test
  (testing "simple expression evaluation"
    (is (= "3" (posts/eval-template [::posts/template "(+ 1 2)"]))))
  (testing "nested expression evaluation"
    (is (= [:div "3"] (posts/eval-template [:div [::posts/template "(+ 1 2)"]]))))
  (testing "reference variable in context"
    (is (= "3" (posts/eval-template [::posts/template "a"] {:namespaces {'user {'a 3}}}))))
  (testing "calc with variable in context"
    (is (= "4" (posts/eval-template [::posts/template "(+ 1 a)"] {:namespaces {'user {'a 3}}}))))
  (testing "reference variable in other namespace context"
    (is (= "3" (posts/eval-template [::posts/template "ns1/a"] {:namespaces {'ns1 {'a 3}}}))))
  (testing "dont coerce result to string"
    (is (= 4 (posts/eval-template [::posts/template "(+ 1 3)"] {:raw? true})))))

(deftest render-post-test
  (testing "a templated markdown post referencing the front matter meta variables"
    (is (= [:html
            [:head [:title "A title"]]
            [:body
             [:div
              [:h1 {:id "title"} "A title"]
              [:p "This is a " "short" " post."]]]]
           (posts/render "---
template: simple
title: A title
adjective: short
---
# {{ title }}
This is a {{ adjective }} post."
             {:templates {:simple [:html
                                   [:head [:title [::posts/template "post.meta/title"]]]
                                   [:body
                                    [::posts/template "post/content"]]]}}))))
  (testing "no template markdown post"
    (is (= [:div [:p "This is a " "short" " post."]]
           (posts/render "---
adjective: short
---
This is a {{ adjective }} post."))))
  (testing "TODO Unknown template"))

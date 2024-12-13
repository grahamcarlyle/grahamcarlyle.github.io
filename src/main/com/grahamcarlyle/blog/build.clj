(ns com.grahamcarlyle.blog.build
  (:require
   [babashka.fs :as fs]
   [clojure.java.io :as io]
   [com.grahamcarlyle.blog.page :as-alias page]
   [com.grahamcarlyle.blog.posts :as posts]
   [hiccup2.core :as h]))

(defn generate [{:keys [output-dir]}]
  (fs/delete-tree output-dir)
  (fs/create-dirs output-dir)
  (let [result (posts/render (slurp (io/file "posts" "first-scrappy.md")))]
    (spit (io/file output-dir "index.html")
          (h/html (h/raw "<!DOCTYPE html>") result))))

(comment
  (def output-dir "generated-output")
  (fs/delete-tree output-dir)
  (generate {:output-dir output-dir})
  ,)

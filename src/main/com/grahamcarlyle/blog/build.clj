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
  (doseq [post-file (fs/glob "posts" "*.md")]
    (let [result (posts/render (slurp (fs/file post-file)))]
      (spit (io/file output-dir (str (fs/strip-ext (last (fs/components post-file))) ".html"))
            (h/html (h/raw "<!DOCTYPE html>") result)))))

(comment
  (def output-dir "generated-output")
  (fs/delete-tree output-dir)
  (generate {:output-dir output-dir})

  (fs/strip-ext (last (fs/components (first (fs/glob "posts" "*.md")))))
  ,)

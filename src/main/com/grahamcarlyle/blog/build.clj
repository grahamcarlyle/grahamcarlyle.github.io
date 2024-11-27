(ns com.grahamcarlyle.blog.build
  (:require
    [babashka.fs :as fs]
    [com.grahamcarlyle.blog.posts :as posts]
    [clojure.java.io :as io]
    [hiccup2.core :as h]))

(defn generate [{:keys [output-dir]}]
  (fs/delete-tree output-dir)
  (fs/create-dirs output-dir)
  (let [result (posts/parse (slurp (io/file "posts" "first-scrappy.md")))]
    (spit (io/file output-dir "index.html") (h/html (:hiccup result)))))

(comment
  (def output-dir "generated-output")
  (fs/delete-tree output-dir)
  (generate {:output-dir output-dir})
  )

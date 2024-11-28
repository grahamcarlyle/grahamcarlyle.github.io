(ns com.grahamcarlyle.blog.build
  (:require
    [babashka.fs :as fs]
    [com.grahamcarlyle.blog :as-alias blog]
    [com.grahamcarlyle.blog.posts :as posts]
    [clojure.java.io :as io]
    [clojure.walk :as walk]
    [hiccup2.core :as h]))

(def post-template
  [:html {:lang "en"}
   [:body
    :blog.page/content]])

(defn substitute [template mapping]
  (walk/postwalk
    (fn [x]
      (if (keyword? x)
        (get mapping x x)
        x))
    template))


(defn generate [{:keys [output-dir]}]
  (fs/delete-tree output-dir)
  (fs/create-dirs output-dir)
  (let [result (posts/parse (slurp (io/file "posts" "first-scrappy.md")))]
    (spit (io/file output-dir "index.html")
          (h/html (h/raw "<!DOCTYPE html>")
                  (substitute post-template {:blog.page/content (:hiccup result)})))))

(comment
  (def output-dir "generated-output")
  (fs/delete-tree output-dir)
  (generate {:output-dir output-dir})
  )

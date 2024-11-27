(ns com.grahamcarlyle.blog.posts
  (:require
    [nextjournal.markdown :as md]
    [nextjournal.markdown.transform :as md.transform]
    [clj-yaml.core :as yaml]))

(def front-matter-yaml-pattern
  #"(?s)^---\n+(.*?)\n+---\n+(.*)")

(defn split-front-matter [input]
  (let [matches (re-matches front-matter-yaml-pattern input)]
    (if matches
      {:front-matter-yaml (nth matches 1)
       :markdown (nth matches 2)}
      {:markdown input})))

(defn parse [content]
  (let [split-content (split-front-matter content)]
    {:meta   (some-> split-content
                     :front-matter-yaml
                     (yaml/parse-string))
     :hiccup (-> split-content
                 :markdown
                 (md/parse)
                 (md.transform/->hiccup))}))

(ns com.grahamcarlyle.blog.build
  (:require
   [babashka.fs :as fs]
   [clojure.java.io :as io]
   [clojure.walk :as walk]
   [com.grahamcarlyle.blog.page :as-alias page]
   [com.grahamcarlyle.blog.posts :as posts]
   [hiccup2.core :as h]))

(def post-template
  [:html {:lang "en"}
   [:head
    [:title ::page/title]]
   [:body
    ::page/content]])

(defn substitute [template mapping]
  (walk/postwalk
    (fn [x]
      (if (keyword? x)
        (get mapping x x)
        x))
    template))

(defn qualify-plain-keyword-keys [mapping ns-str]
  (update-keys mapping
               #(cond-> %
                        (and (keyword? %) (nil? (namespace %)))
                        (->> name (keyword ns-str)))))

(defn generate [{:keys [output-dir]}]
  (fs/delete-tree output-dir)
  (fs/create-dirs output-dir)
  (let [result (posts/parse (slurp (io/file "posts" "first-scrappy.md")))
        mapping (merge {::page/content (:hiccup result)}
                       (qualify-plain-keyword-keys (:meta result) (namespace ::page/placeholder)))]
    (spit (io/file output-dir "index.html")
          (h/html (h/raw "<!DOCTYPE html>")
                  (substitute post-template mapping)))))

(comment
  (def output-dir "generated-output")
  (fs/delete-tree output-dir)
  (generate {:output-dir output-dir}))

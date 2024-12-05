(ns com.grahamcarlyle.blog.posts
  (:require
   [clj-yaml.core :as yaml]
   [clojure.walk :as walk]
   [nextjournal.markdown :as md]
   [nextjournal.markdown.transform :as md.transform]
   [nextjournal.markdown.utils :as md.utils]
   [sci.core :as sci]))

(def front-matter-yaml-pattern
  #"(?s)^---\n+(.*?)\n+---\n+(.*)")

(defn split-front-matter [input]
  (let [matches (re-matches front-matter-yaml-pattern input)]
    (if matches
      {:front-matter-yaml (nth matches 1)
       :markdown (nth matches 2)}
      {:markdown input})))

(def template-tokenizer
  (md.utils/normalize-tokenizer
    {:regex #"\{\{([^\}]+)\}\}"
     :handler (fn [match] {:type ::template
                           :text (match 1)})}))

(defn template-node->hiccup [_ctx node]
  [::template (:text node)])

(defn parse-markdown [markdown]
  (->> markdown
       (md/parse* (update md.utils/empty-doc :text-tokenizers conj template-tokenizer))
       (md.transform/->hiccup
         (assoc md.transform/default-hiccup-renderers ::template template-node->hiccup))))

(defn parse [content]
  (let [split-content (split-front-matter content)]
    {:meta   (some-> split-content :front-matter-yaml (yaml/parse-string))
     :hiccup (-> split-content :markdown (parse-markdown))}))

(defn eval-string [form-str context]
  (sci/eval-string form-str {:bindings context}))

(defn eval-template
  ([template]
   (eval-template template nil))
  ([template context]
   (walk/postwalk
     (fn [x]
       (if (and (vector? x)
                (= 2 (count x))
                (= ::template (first x))
                (string? (second x)))
         (str (eval-string (second x) context))
         x))
     template)))

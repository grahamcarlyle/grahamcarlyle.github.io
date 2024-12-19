(ns com.grahamcarlyle.blog.posts
  (:require
   [clj-yaml.core :as yaml]
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

(defn template-tokenizer [front-matter]
  (let [front-matter-bindings (update-keys front-matter #(symbol (name %)))]
    (md.utils/normalize-tokenizer
      {:regex   #"\{\{([^\}]+)\}\}"
       :handler (fn [match] {:type ::template
                             :text (-> (match 1)
                                       (sci/eval-string {:bindings front-matter-bindings})
                                       (str))})})))

(defn template-node->hiccup [_ctx node]
  (:text node))

(defn parse-markdown [front-matter markdown]
  (->> markdown
       (md/parse* (update md.utils/empty-doc :text-tokenizers conj (template-tokenizer front-matter)))
       (md.transform/->hiccup
         (assoc md.transform/default-hiccup-renderers ::template template-node->hiccup))))

(defn parse [content]
  (let [split-content (split-front-matter content)
        front-matter (some-> split-content :front-matter-yaml (yaml/parse-string))]
    {:front-matter (some-> split-content :front-matter-yaml (yaml/parse-string))
     :content      (->> split-content :markdown (parse-markdown front-matter))}))

(defn undecorated-content-template [{:keys [content]}] content)

(defn render
  ([post-markdown]
   (render post-markdown nil))
  ([post-markdown context]
   (let [{:keys [front-matter content]} (parse post-markdown)
         template (or (and (-> context :templates)
                           (let [template-name (or (-> front-matter :template (keyword))
                                                   (:default-template context))]
                             (or (-> context :templates (get template-name))
                                 (throw (ex-info "Unknown template" {:template-name template-name})))))
                      undecorated-content-template)]
     (template {:front-matter front-matter :content content}))))

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
       :handler (fn [match] {:type :text
                             :text (-> (match 1)
                                       (sci/eval-string {:bindings front-matter-bindings})
                                       (str))})})))

(defn parse-markdown [front-matter markdown]
  (->> markdown
       (md/parse* (update md.utils/empty-doc :text-tokenizers conj (template-tokenizer front-matter)))
       (md.transform/->hiccup)))

(defn parse [content]
  (let [split-content (split-front-matter content)
        front-matter (some-> split-content :front-matter-yaml (yaml/parse-string))]
    {:front-matter (some-> split-content :front-matter-yaml (yaml/parse-string))
     :content      (->> split-content :markdown (parse-markdown front-matter))}))

(defn undecorated-content-template [{:keys [content]}] content)

(defn get-template [context front-matter]
  (or (and (-> context :templates)
           (let [template-name (or (-> front-matter :template (keyword))
                                   (:default-template context))]
             (or (-> context :templates (get template-name))
                 (throw (ex-info "Unknown template" {:template-name template-name})))))
      undecorated-content-template))

(defn render-as-page
  ([post-markdown]
   (render-as-page post-markdown nil))
  ([post-markdown context]
   (let [{:keys [front-matter] :as post} (parse post-markdown)
         template (get-template context front-matter)]
     (assoc post :page-source (template post)))))

; TODO remove after changing tests to use render-as-page
(defn render
  ([post-markdown]
   (render post-markdown nil))
  ([post-markdown context]
   (:page-source (render-as-page post-markdown context))))

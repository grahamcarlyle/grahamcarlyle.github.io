(ns com.grahamcarlyle.blog.build
  (:require
   [babashka.fs :as fs]
   [clojure.java.io :as io]
   [com.grahamcarlyle.blog.posts :as posts]
   [hiccup2.core :as h]))

(defn home-page [{:keys [posts]}]
  (let [get-date #(-> % :front-matter :date)
        most-recent-posts (->> posts (filter get-date) (sort-by get-date) (take 5))]
    {:path "/index.html"
     :page-source
     (h/html
       (h/raw "<!DOCTYPE html>")
       [:html {:lang "en"}
        [:head
         [:meta {:charset "UTF-8"}]
         [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
         [:title "Graham's Blog"]
         [:link {:rel "stylesheet" :href "/css/blog.css"}]]
        [:body
         [:header
          [:h1 "Graham's Blog"]]
         [:div.container
          [:main.main-content
           [:<>
            (for [post most-recent-posts]
              [:article.post
               [:h2.post-title
                [:a {:href ""} (-> post :front-matter :title)]]
               [:div.post-date (-> post :front-matter :date)]
               [:div.post-excerpt
                (:content post)]])]]
          [:aside.sidebar
           [:div.sidebar-section
            [:h2 "About"]
            [:p "Welcome to my blog."]]
           ]]]])}))

(def site-defaults
  {:posts-glob        "posts/*.md"
   :posts-path-prefix "/posts/"
   :posts-path-ext    ".html"
   :static-files-dir "src/public"})

(def blog-site
  (merge site-defaults
         {:other-pages      [home-page]
          :templates
          {:post
           (fn [{:keys [front-matter content]}]
             (h/html
               (h/raw "<!DOCTYPE html>")
               [:head
                [:meta {:charset "UTF-8"}]
                [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
                [:title (:title front-matter)]
                [:link {:rel "stylesheet" :href "/css/blog.css"}]]
               [:body
                [:article.blog-post
                 [:div.post-date (:date front-matter)]
                 [:h2.post-title (:title front-matter)]
                 [:div.post-content
                  content]]]))}
          :default-template :post}))

(defn post-path [{:keys [posts-path-ext posts-path-prefix] :as _site} post]
  (str posts-path-prefix
       (fs/strip-ext (last (fs/components (:post-file post))))
       posts-path-ext))

(defn render-posts-as-pages [{:keys [posts-glob] :as site}]
  (for [post-file (fs/glob "." posts-glob)]
    (let [post (assoc (posts/render-as-page (slurp (fs/file post-file)) site)
                 :post-file post-file)]
      (assoc post
        :path (post-path site post)))))

(defn add-posts [site]
  (let [posts (render-posts-as-pages site)]
    (-> site
        (assoc :posts posts)
        (update :pages concat posts))))

(defn add-other-pages [site]
  (update site :pages concat (for [make-page-fn (:other-pages site)]
                               (make-page-fn site))))

(defn rel-path [abs-path]
  (when (not= \/ (first abs-path))
    (throw (ex-info "Path needs to be absolute" {:path abs-path})))
  (subs abs-path 1))

(defn write-page [{:keys [path page-source] :as _page} output-dir]
  (let [f (io/file output-dir (rel-path path))]
    (fs/create-dirs (fs/parent f))
    (spit f page-source)))

(defn write-pages [{:keys [output-dir pages] :as site}]
  (doseq [page pages]
    (write-page page output-dir))
  site)

(defn copy-site-static-files [{:keys [output-dir static-files-dir] :as _site}]
  (fs/copy-tree static-files-dir output-dir))

(defn generate [site]
  (-> site
      (add-posts)
      (add-other-pages)
      (write-pages)
      (copy-site-static-files)))

(defn generate-blog [opts]
  (generate (merge blog-site opts)))

(comment
  (def output-dir "generated-output")
  (fs/delete-tree output-dir)
  (generate-blog {:output-dir output-dir})

  (fs/glob "." "posts/**.md")
  (fs/strip-ext (last (fs/components (first (fs/glob "posts" "*.md")))))
  ,)

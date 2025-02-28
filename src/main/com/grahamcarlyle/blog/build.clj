(ns com.grahamcarlyle.blog.build
  (:require
   [babashka.fs :as fs]
   [clojure.java.io :as io]
   [com.grahamcarlyle.blog.posts :as posts]
   [lambdaisland.hiccup :as h]))

(def site-defaults
  {:posts-glob        "posts/*.md"
   :posts-path-prefix "/posts/"
   :posts-path-ext    ".html"
   :static-files-dir "src/public"})

(defn blog-page-template [{:keys [title main-content sidebar]}]
  (h/render
    [:html {:lang "en"}
     [:head
      [:meta {:charset "UTF-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
      [:title title]
      [:link {:rel "stylesheet" :href "/css/blog.css"}]]
     [:body
      [:header
       [:h1 "Graham's Blog"]]
      [:div.container
       [:main.main-content
        main-content]
       [:aside.sidebar
        sidebar]]]]))

(defn home-page-template [most-recent-posts]
  (blog-page-template
    {:title "Graham's Blog"
     :main-content [:<>
                    (for [post most-recent-posts]
                      [:article.post
                       [:h2.post-title
                        [:a {:href ""} (-> post :front-matter :title)]]
                       [:div.post-date (-> post :front-matter :date)]
                       [:div.post-content
                        (:content post)]])]
     :sidebar [:div.sidebar-section
               [:h2 "About"]
               [:p "Welcome to my blog."]]}))

(defn post-page-template [{:keys [front-matter content]}]
  (let [{:keys [title date]} front-matter]
    (blog-page-template
      {:title        title
       :main-content [:article.post
                      [:h1.post-title [:a {:href ""} title]]
                      [:div.post-date date]
                      [:div.post-content content]]
       :sidebar      [:div.sidebar-section]})))

(defn home-page [{:keys [posts]}]
  (let [get-date #(-> % :front-matter :date)
        most-recent-posts (->> posts (filter get-date) (sort-by get-date) (take 5))]
    {:path "/index.html"
     :page-source (home-page-template most-recent-posts)}))

(def blog-site
  (merge site-defaults
         {:other-pages [home-page]
          :templates {:post post-page-template}
          :default-post-template :post}))

(defn post-path [{:keys [posts-path-ext posts-path-prefix] :as _site} post]
  (str posts-path-prefix
       (fs/strip-ext (last (fs/components (:post-file post))))
       posts-path-ext))

(defn render-posts-as-pages [{:keys [posts-glob] :as site}]
  (for [post-file (fs/glob "." posts-glob)]
    (let [post (assoc (posts/parse-rendered-post (slurp (fs/file post-file)) site)
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

(defn make-site-parts [post site]
  (let [{:keys [posts-path-ext posts-path-prefix]} site
        path (str posts-path-prefix (:post-id post) posts-path-ext)
        post-part {:type :str
                   :path path
                   :str (:rendering post)}
        post-dir (fs/file (fs/strip-ext (:post-file post)))]

    (cond-> [post-part]
            (and (fs/exists? post-dir)
                 (fs/directory? post-dir))
            (conj {:type     :dir
                   :src-dir  (str post-dir)
                   :path-dir (str posts-path-prefix (:post-id post))}))))

(defn write-part
  ([part output-dir]
   (write-part part output-dir nil))
  ([part output-dir opts]
   (case (:type part)
     :str (let [f (io/file output-dir (rel-path (:path part)))]
            (fs/create-dirs (fs/parent f))
            (spit f (:str part)))
     :dir (let [{:keys [src-dir path-dir]} part]
            (fs/copy-tree (fs/file src-dir) (fs/file output-dir (rel-path path-dir)) opts)))))

(defn write-parts [parts output-dir]
  (doseq [part parts]
    (write-part part output-dir)))

(comment

  (require '[cursive.inline.scope-capture :as cursive-sc])
  (require '[sc.api :refer [spy]])
  (cursive-sc/show-ep-info)

  (def output-dir "generated-output")
  (fs/delete-tree output-dir)
  (generate-blog {:output-dir output-dir})

  (fs/glob "." "posts/**.md")
  (fs/strip-ext (last (fs/components (first (fs/glob "posts" "*.md")))))

  ; a post from test data
  (-> "---
title: A title
date: 2025-01-10T11:48:00.0Z
---
This is some words in the post

 * first item
 * second item
    "
      (posts/parse)
      (posts/add-rendering blog-site)
      (assoc :post-id "example")
      (make-site-parts blog-site)
      (write-parts "generated-output"))

  ; write out the shared static files
  (write-part {:type :dir :src-dir "src/public" :path-dir "/"} "generated-output" {:replace-existing true})

  ; a post from file content
  (let [post-file (fs/path "posts/first-scrappy.md")
        post-id (fs/strip-ext (last (fs/components post-file)))]
    (-> post-file
        (fs/file)
        (slurp)
        (posts/parse)
        (posts/add-rendering blog-site)
        (assoc :post-id post-id)
        (make-site-parts blog-site)
        (write-parts "generated-output")))

  ; a post from file content with a static resources dir
  (let [post-file (fs/path "posts/reconciling_perspectives_at_xtc.md")
        post-id (fs/strip-ext (last (fs/components post-file)))
        post (-> post-file
                 (fs/file)
                 (slurp)
                 (posts/parse)
                 (posts/add-rendering blog-site)
                 (assoc :post-id post-id)
                 (assoc :post-file post-file))
        parts (-> post
                  (make-site-parts blog-site))]
    (spy)
    (write-parts parts "generated-output"))

  ,)

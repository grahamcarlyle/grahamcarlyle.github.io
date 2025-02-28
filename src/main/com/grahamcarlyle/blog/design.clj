(ns com.grahamcarlyle.blog.design
  (:require
   [com.grahamcarlyle.blog.design-tokens :as dt]
   [lambdaisland.ornament :as o]))

(o/defrules
  layout
  [:body
   {:font-family "var(--font-humanist)"}])                  ;  normalise not available as tokens

(o/defstyled
  cta :button
  {:background-color dt/--jungle-1})

(o/defstyled
  my-list :ul
  {:background-color "green"}
  [:li {:list-style "square"}]
  ([{:keys [users]}]
   (for [{:keys [name]} users]
     [:li name])))

(o/defstyled
  blog-layout :div
  {:width                 "90%"
   :max-width             "1200px"
   :margin                "0 auto"
   :display               "grid"
   :grid-template-columns "3fr 1fr"
   :grid-template-rows    "auto 1fr auto"
   :grid-template-areas   [["header" "header"] ["main" " sidebar"] ["footer" "footer"]]
   :gap                   "20px"
   :padding               "20px 0"}
  [:header
   {:grid-area  "header"
    :padding    "20px"
    :text-align "center"}]
  [:main
   {:grid-area "main"
    :padding   "20px"}]
  [:aside
   {:grid-area "sidebar"}]
  [:footer
   {:grid-area  "footer"
    :text-align "center"}]
  ([header main-content sidebar footer]
   [:<>
    [:header header]
    [:main main-content]
    [:aside sidebar]
    [:footer footer]]))

(defn css-links []
  [:<>
   [:link {:rel "stylesheet" :href "/design.css"}]
   [:link {:rel "stylesheet" :href "/css/open-props.min.css"}]
   [:link {:rel "stylesheet" :href "/css/open-props-normalize.min.css"}]])

#_(comment

    (require '[[lambdaisland.hiccup :as h]])

    #_(spit "generated-output/design.css" (o/defined-styles))

    #_(spit
        "generated-output/design.html"
        (h/render
          [:html
           [:head
            [:link {:rel "stylesheet" :href "/design.css"}]
            [:link {:rel "stylesheet" :href "/css/open-props.min.css"}]
            [:link {:rel "stylesheet" :href "/css/open-props-normalize.min.css"}]]
           [:body
            [cta {:href "https://grahamcarlyle.com"} "Click me"]
            [my-list {:users [{:name "Alice"} {:name "Bob"}]}]
            [:p {:style {:background-color "var(--indigo-2)"}} "Hello"]]]))

    #_(let [scenes [{:name   "CTA"
                     :hiccup [cta {:href "https://grahamcarlyle.com"} "Click me"]}
                    {:name   "My List"
                     :hiccup [my-list {:users [{:name "Alice"} {:name "Bob"}]}]}
                    {:name   "Wibble"
                     :hiccup [:p {:style {:background-color "var(--indigo-2)"}} "Hello"]}
                    {:name   "Left menu layout"
                     :hiccup [left-menu-layout "Left content" "Main content"]}
                    {:name   "Blog layout"
                     :hiccup [blog-layout "Header" "Main content" "Side bar" "Footer"]}]]
        (spit "generated-output/design.css" (o/defined-styles))
        (spit "generated-output/design.html" (h/render [portfolio-page scenes [scene-list scenes]]))
        (doseq [scene scenes]
          (spit (str "generated-output/" (:name scene) ".html")
                (h/render [portfolio-page scenes scene]))))

    ,)

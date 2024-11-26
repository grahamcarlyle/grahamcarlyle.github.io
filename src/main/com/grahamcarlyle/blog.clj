(ns com.grahamcarlyle.blog
  (:require
    [babashka.fs :as fs]
    [clojure.java.io :as io]
    [hiccup2.core :as h]))

(defn declaration-of-intent []
  (h/html [:div
           [:p "Declaration of intent to share scrappy fiddles."]
           [:p
            "I'm still thinking about whether I am compelled to "
            [:a
             {:href "https://www.todepond.com/sky/normalise-dont-share-lol/"}
             "normalise sharing scrappy fiddles"]]]))

(defn build [{:keys [output-dir]}]
  (fs/delete-tree output-dir)
  (fs/create-dirs output-dir)
  (spit (io/file output-dir "index.html") (declaration-of-intent)))

(comment
  (build {:output-dir "build-output"})
  )

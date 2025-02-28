(ns com.grahamcarlyle.blog.design-tokens
  {:ornament/prefix ""}
  (:require
   [charred.api :as charred]
   [clojure.java.io :as io]
   [lambdaisland.ornament :as o]))

(o/import-tokens! (charred/read-json (io/resource "open-props.tokens.json")) {:include-values? false})

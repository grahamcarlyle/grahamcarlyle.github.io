(ns serve
  (:require
   [ring.adapter.jetty :as jetty]
   [ring.middleware.file :as file-middleware]))

(defn serve-site [{:keys [dir]}]
  (let [no-dynamic-content-handler {}
        handler (file-middleware/wrap-file no-dynamic-content-handler dir)
        server (jetty/run-jetty handler {:port 8080 :join? false})]
    (println (format "serving %s from %s" (.getURI server) dir))
    (.join server)))

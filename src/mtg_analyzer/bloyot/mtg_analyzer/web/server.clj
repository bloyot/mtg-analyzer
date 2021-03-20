(ns bloyot.mtg-analyzer.web.server)

(def app
  (if (= (conf/env) :local)
    (wrap-reload routes/handler {:dirs ["src/clj"]})
    routes/handler))

(mount/defstate ^{:on-reload :noop} ApplicationServer
  :start
  (jetty/run-jetty #'app {:port 3000 :join? false})
  :stop (when instance? Server ApplicationServer
              (.stop ApplicationServer)))

(defn -main
  [& args]
  (println "Starting webserver on port 3000")
  (mount/start))

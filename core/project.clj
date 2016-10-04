(defproject org.onyxplatform/onyx-gen-doc "0.9.12-SNAPSHOT"
  :description "Core code for rendering Onyx information model data into a Markdown template."
  :url "https://github.com/colinhicks/onyx-gen-doc"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [table "0.5.0"]]
  :profiles {:dev {:dependencies [[org.onyxplatform/onyx-kafka "0.9.10.1"]]}})


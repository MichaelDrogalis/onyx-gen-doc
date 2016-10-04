(defproject org.onyxplatform/onyx-gen-doc-doc "0.9.12-SNAPSHOT"
  :description "Generates onyx-gen-doc (self) documentation"
  :url "https://github.com/colinhicks/onyx-gen-doc"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:dev {:dependencies [^{:voom {:repo "https://github.com/colinhicks/onyx-gen-doc.git" :branch "master"}}
                                  [org.onyxplatform/onyx-gen-doc "0.9.12-SNAPSHOT"]]
                   :plugins [^{:voom {:repo "https://github.com/colinhicks/onyx-gen-doc.git" :branch "master"}}
                             [org.onyxplatform/lein-onyx-gen-doc "0.9.12-SNAPSHOT"]]
                   :onyx-gen-doc {:information-model onyx.gen-doc.information-model/model
                                  :in-path "README.template.md"
                                  :out-path "README.md"
                                  :verbose? true}}})

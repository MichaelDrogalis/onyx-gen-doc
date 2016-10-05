(defproject org.onyxplatform/onyx-gen-doc-doc "0.9.11.0"
  :description "Generates onyx-gen-doc (self) documentation"
  :url "https://github.com/onyx-platform/onyx-gen-doc"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles
  {:dev
   {:dependencies [[org.onyxplatform/onyx-gen-doc "0.9.11.0"]]
    :plugins [[org.onyxplatform/lein-onyx-gen-doc "0.9.11.0"]]
    :onyx-gen-doc {:information-model onyx.gen-doc.information-model/model
                   :in-path "README.template.md"
                   :out-path "README.md"
                   :verbose? true}}})

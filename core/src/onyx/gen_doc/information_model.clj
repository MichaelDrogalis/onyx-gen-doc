(ns onyx.gen-doc.information-model
  "Available options for onyx-gen-doc templates and the lein plugin. Below, `model` reuses
   the plugin map structure, but onyx-gen-doc should not be confused with an Onyx plugin.")

(def model
  {:catalog-entry
   {:onyx.gen-doc/lein-plugin
    {:summary "Leiningen plugin options for :onyx-doc-gen in the target project.clj."
     :model {:information-model
             {:doc "The fully-qualified symbol of the information model. Must be on the target classpath."
              :type :symbol}

             :in-path
             {:doc "The template file path."
              :type :string}

             :out-path
             {:doc "The output file path."
              :type :string}

             :verbose?
             {:doc "In the output file, includes processed template data, inline as markdown comments."
              :type :boolean
              :default false
              :optional? true}

             :throw?
             {:doc "Throws first exception into the lein process, instead of serializing all exception into output inline comments."
              :type :boolean
              :default false
              :optional? true}}}

    :onyx.gen-doc/summary-template
    {:summary "Prints the catalog entry summary."
     :model {:display
             {:doc "The keyword `:summary`"
              :type :keyword
              :default :summary}

             :model
             {:doc "The catalog entry key."
              :type :keyword}

             :format
             {:doc "Apply a Markdown format."
              :type :keyword
              :choices [:plain :h1 :h2 :h3 :h4 :h5 :h6 :code :clojure :em :strong]
              :optional? true}

             :format-string
             {:doc "Applies `clojure.core/format` with this value, to the summary."
              :type :string
              :default "%s"
              :optional? true}}}

    :onyx.gen-doc/attribute-table-template
    {:summary "A table of the catalog entry attributes."
     :model {:display
             {:doc "The keyword `:attribute-table`"
              :type :keyword
              :default :attribute-table}

             :model
             {:doc "The catalog entry key."
              :type :keyword}

             :columns
             {:doc "A vector of column specs or a keyword specifying a config preset."
              :type :keyword-or-vector
              :default :columns/default
              :optional? true}}}

    :onyx.gen-doc/catalog-entry-template
    {:summary "An example catalog entry. Values are naïvely inferred. Use `:merge-additions` to clarify."
     :model {:display
             {:doc "The keyword `:catalog-entry`"
              :type :keyword
              :default :catalog-entry}

             :model
             {:doc "The catalog entry key."
              :type :keyword}

             :merge-additions
             {:doc "Overrides and additions merged into the example catalog entry. To elide a model entry, use `:onyx.gen-doc/ignore` as the value for the ignored key."
              :type :map
              :optional? true}}}

    :onyx.gen-doc/lifecycle-entry-template
    {:summary "A lifecycle entry."
     :model {:display
             {:doc "The keyword `:lifecycle-entry`"
              :type :keyword
              :default :lifecycle-entry}

             :model
             {:doc "The lifecycle entry key."
              :type :keyword}}}}
   
   :display-order
   {:onyx.gen-doc/lein-plugin
    [:information-model
     :in-path
     :out-path
     :verbose?
     :throw?]
    :onyx.gen-doc/summary-template
    [:display
     :model
     :format
     :format-string]
    :onyx.gen-doc/attribute-table-template
    [:display
     :model
     :columns]
    :onyx.gen-doc/catalog-entry-template
    [:display
     :model
     :merge-additions]
    :onyx.gen-doc/lifecycle-entry-template
    [:display
     :model]}})

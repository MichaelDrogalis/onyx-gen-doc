[//]: # ({:display :header, :valid-structure? true, :all-params ({:display :summary, :model :onyx.gen-doc/lein-plugin, :format :h6} {:display :attribute-table, :model :onyx.gen-doc/lein-plugin, :columns [[:key "Key"] [:type "Type"] [:doc "Description"]]} {:display :catalog-entry, :model :onyx.gen-doc/lein-plugin, :merge-additions {:in-path "README.template.md", :out-path "README.md", :verbose? true, :throw? :onyx.gen-doc/ignore}} {:display :summary, :model :onyx.gen-doc/summary-template, :format :h6} {:display :attribute-table, :model :onyx.gen-doc/summary-template, :columns [[:key "Key"] [:type "Type"] [:optional? "Optional?" :code] [:doc "Description"] [:choices "Choices" :code]]} {:display :catalog-entry, :model :onyx.gen-doc/summary-template, :view-source? true, :merge-additions {:format-string "The summary: %s"}} {:display :summary, :model :onyx.gen-doc/attribute-table-template, :format :h6} {:display :attribute-table, :model :onyx.gen-doc/attribute-table-template, :columns [[:key "Key"] [:type "Type"] [:optional? "Optional?" :code] [:doc "Description"]]} {:display :catalog-entry, :model :onyx.gen-doc/attribute-table-template, :view-source? true} {:display :summary, :model :onyx.gen-doc/catalog-entry-template, :format :h6} {:display :attribute-table, :model :onyx.gen-doc/catalog-entry-template, :columns [[:key "Key"] [:type "Type"] [:optional? "Optional?" :code] [:doc "Description"]]} {:display :catalog-entry, :model :onyx.gen-doc/catalog-entry-template, :view-source? true, :merge-additions {:in-this-model :overridden, :not-in-this-model :added, :ignore-in-this-model :onyx.gen-doc/ignore}} {:display :summary, :model :onyx.gen-doc/lifecycle-entry-template, :format :h6} {:display :attribute-table, :model :onyx.gen-doc/lifecycle-entry-template, :columns [[:key "Key"] [:type "Type"] [:optional? "Optional?" :code] [:doc "Description"]]} {:display :catalog-entry, :model :onyx.gen-doc/lifecycle-entry-template, :view-source? true})})
## onyx-doc-gen

Utilities for rendering Onyx information model data into a Markdown template. Comprises a Leiningen plugin and a core library. Use this system to keep an Onyx plugin's documentation in sync with its respective information model.

#### Installation

In the plugin project file `:dependencies`:

```clojure
[org.onyxplatform/onyx-gen-doc "0.9.12-SNAPSHOT"]
```

In the plugin project file `:plugins`:

```clojure
[org.onyxplatform/lein-onyx-gen-doc "0.9.12-SNAPSHOT"]
```

*Note that the root lein project, `org.onyxplatform/onyx-gen-doc-doc` is used to generate this documentation. It does not need to be installed in the target plugin project.*

#### Plugin configuration

[//]: # ({:display :summary, :model :onyx.gen-doc/lein-plugin, :format :h6})
###### Leiningen plugin options for :onyx-doc-gen in the target project.clj.

[//]: # ({:display :attribute-table, :model :onyx.gen-doc/lein-plugin, :columns [[:key "Key"] [:type "Type"] [:doc "Description"]]})

| Key                  | Type       | Description                                                                                                     |
|--------------------- | ---------- | ----------------------------------------------------------------------------------------------------------------|
| `:information-model` | `:symbol`  | The fully-qualified symbol of the information model. Must be on the target classpath.                           |
| `:in-path`           | `:string`  | The template file path.                                                                                         |
| `:out-path`          | `:string`  | The output file path.                                                                                           |
| `:verbose?`          | `:boolean` | In the output file, includes processed template data, inline as markdown comments.                              |
| `:throw?`            | `:boolean` | Throws first exception into the lein process, instead of serializing all exception into output inline comments. |


Example `onyx-gen-doc`: value:
[//]: # ({:display :catalog-entry, :model :onyx.gen-doc/lein-plugin, :merge-additions {:in-path "README.template.md", :out-path "README.md", :verbose? true, :throw? :onyx.gen-doc/ignore}})
```clojure
{:information-model my.ns/information-model,
 :in-path "README.template.md",
 :out-path "README.md",
 :verbose? true}
```

See this repository's root project.clj for a contextual example.


#### Templates

This readme is itself generated from templates. Compare the raw source of README.template.md to see templates in action.

##### Summary template

[//]: # ({:display :summary, :model :onyx.gen-doc/summary-template, :format :h6})
###### Prints the catalog entry summary.

[//]: # ({:display :attribute-table, :model :onyx.gen-doc/summary-template, :columns [[:key "Key"] [:type "Type"] [:optional? "Optional?" :code] [:doc "Description"] [:choices "Choices" :code]]})

| Key              | Type       | Optional? | Description                                                    | Choices                                                       |
|----------------- | ---------- | --------- | -------------------------------------------------------------- | --------------------------------------------------------------|
| `:display`       | `:keyword` |           | The keyword `:summary`                                         |                                                               |
| `:model`         | `:keyword` |           | The catalog entry key.                                         |                                                               |
| `:format`        | `:keyword` | `true`    | Apply a Markdown format.                                       | `[:plain :h1 :h2 :h3 :h4 :h5 :h6 :code :clojure :em :strong]` |
| `:format-string` | `:string`  | `true`    | Applies `clojure.core/format` with this value, to the summary. |                                                               |


[//]: # ({:display :catalog-entry, :model :onyx.gen-doc/summary-template, :view-source? true, :merge-additions {:format-string "The summary: %s"}})
```clojure
    ```onyx-gen-doc
    {:display :summary,
     :model :my.ns/model,
     :format :plain,
     :format-string "%s",
     :merge-additions
     {:format-string "The summary: %s"}}
    ```
```

##### Attribute table template

[//]: # ({:display :summary, :model :onyx.gen-doc/attribute-table-template, :format :h6})
###### A table of the catalog entry attributes.

[//]: # ({:display :attribute-table, :model :onyx.gen-doc/attribute-table-template, :columns [[:key "Key"] [:type "Type"] [:optional? "Optional?" :code] [:doc "Description"]]})

| Key        | Type                 | Optional? | Description                                                       |
|----------- | -------------------- | --------- | ------------------------------------------------------------------|
| `:display` | `:keyword`           |           | The keyword `:attribute-table`                                    |
| `:model`   | `:keyword`           |           | The catalog entry key.                                            |
| `:columns` | `:keyword-or-vector` | `true`    | A vector of column specs or a keyword specifying a config preset. |


[//]: # ({:display :catalog-entry, :model :onyx.gen-doc/attribute-table-template, :view-source? true})
```clojure
    ```onyx-gen-doc
    {:display :attribute-table,
     :model :my.ns/model,
     :columns :columns/default}
    ```
```

##### Catalog entry template

[//]: # ({:display :summary, :model :onyx.gen-doc/catalog-entry-template, :format :h6})
###### An example catalog entry. Values are naïvely inferred. Use `:merge-additions` to clarify.

[//]: # ({:display :attribute-table, :model :onyx.gen-doc/catalog-entry-template, :columns [[:key "Key"] [:type "Type"] [:optional? "Optional?" :code] [:doc "Description"]]})

| Key                | Type       | Optional? | Description                                                                                                                                         |
|------------------- | ---------- | --------- | ----------------------------------------------------------------------------------------------------------------------------------------------------|
| `:display`         | `:keyword` |           | The keyword `:catalog-entry`                                                                                                                        |
| `:model`           | `:keyword` |           | The catalog entry key.                                                                                                                              |
| `:merge-additions` | `:map`     | `true`    | Overrides and additions merged into the example catalog entry. To elide a model entry, use `:onyx.gen-doc/ignore` as the value for the ignored key. |


[//]: # ({:display :catalog-entry, :model :onyx.gen-doc/catalog-entry-template, :view-source? true, :merge-additions {:in-this-model :overridden, :not-in-this-model :added, :ignore-in-this-model :onyx.gen-doc/ignore}})
```clojure
    ```onyx-gen-doc
    {:display :catalog-entry,
     :model :my.ns/model,
     :merge-additions
     {:in-this-model :overridden,
      :not-in-this-model :added,
      :ignore-in-this-model :onyx.gen-doc/ignore}}
    ```
```

##### Lifecycle entry template

[//]: # ({:display :summary, :model :onyx.gen-doc/lifecycle-entry-template, :format :h6})
###### A lifecycle entry.

[//]: # ({:display :attribute-table, :model :onyx.gen-doc/lifecycle-entry-template, :columns [[:key "Key"] [:type "Type"] [:optional? "Optional?" :code] [:doc "Description"]]})

| Key        | Type       | Optional? | Description                    |
|----------- | ---------- | --------- | -------------------------------|
| `:display` | `:keyword` |           | The keyword `:lifecycle-entry` |
| `:model`   | `:keyword` |           | The lifecycle entry key.       |


[//]: # ({:display :catalog-entry, :model :onyx.gen-doc/lifecycle-entry-template, :view-source? true})
```clojure
    ```onyx-gen-doc
    {:display :lifecycle-entry, :model :my.ns/model}
    ```
```

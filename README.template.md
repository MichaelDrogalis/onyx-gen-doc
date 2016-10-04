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

```onyx-gen-doc
{:display :summary 
 :model :onyx.gen-doc/lein-plugin
 :format :h6}
```

```onyx-gen-doc
{:display :attribute-table
 :model :onyx.gen-doc/lein-plugin
 :columns [[:key "Key"] [:type "Type"] [:doc "Description"]]}
```

Example `onyx-gen-doc`: value:
```onyx-gen-doc
{:display :catalog-entry
 :model :onyx.gen-doc/lein-plugin
 :merge-additions
 {:in-path "README.template.md"
  :out-path "README.md"
  :verbose? true
  :throw? :onyx.gen-doc/ignore}}
```

See this repository's root project.clj for a contextual example.


#### Templates

This readme is itself generated from templates. Compare the raw source of README.template.md to see templates in action.

##### Summary template

```onyx-gen-doc
{:display :summary :model :onyx.gen-doc/summary-template :format :h6}
```

```onyx-gen-doc
{:display :attribute-table
 :model :onyx.gen-doc/summary-template
 :columns [[:key "Key"]
           [:type "Type"] 
           [:optional? "Optional?" :code]
           [:doc "Description"]
           [:choices "Choices" :code]]}
```

```onyx-gen-doc
{:display :catalog-entry
 :model :onyx.gen-doc/summary-template
 :view-source? true
 :merge-additions {:format-string "The summary: %s"}}
```

##### Attribute table template

```onyx-gen-doc
{:display :summary :model :onyx.gen-doc/attribute-table-template :format :h6}
```

```onyx-gen-doc
{:display :attribute-table
 :model :onyx.gen-doc/attribute-table-template
 :columns [[:key "Key"]
           [:type "Type"] 
           [:optional? "Optional?" :code]
           [:doc "Description"]]}
```

```onyx-gen-doc
{:display :catalog-entry
 :model :onyx.gen-doc/attribute-table-template
 :view-source? true}
```

##### Catalog entry template

```onyx-gen-doc
{:display :summary :model :onyx.gen-doc/catalog-entry-template :format :h6}
```

```onyx-gen-doc
{:display :attribute-table
 :model :onyx.gen-doc/catalog-entry-template
 :columns [[:key "Key"]
           [:type "Type"] 
           [:optional? "Optional?" :code]
           [:doc "Description"]]}
```

```onyx-gen-doc
{:display :catalog-entry
 :model :onyx.gen-doc/catalog-entry-template
 :view-source? true
 :merge-additions
 {:in-this-model :overridden
  :not-in-this-model :added
  :ignore-in-this-model :onyx.gen-doc/ignore}}
```

##### Lifecycle entry template

```onyx-gen-doc
{:display :summary :model :onyx.gen-doc/lifecycle-entry-template :format :h6}
```

```onyx-gen-doc
{:display :attribute-table
 :model :onyx.gen-doc/lifecycle-entry-template
 :columns [[:key "Key"]
           [:type "Type"] 
           [:optional? "Optional?" :code]
           [:doc "Description"]]}
```

```onyx-gen-doc
{:display :catalog-entry
 :model :onyx.gen-doc/lifecycle-entry-template
 :view-source? true}
```

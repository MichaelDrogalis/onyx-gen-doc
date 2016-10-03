(ns onyx.gen-doc
  (:require [clojure.edn :as edn]
            [clojure.pprint :as pprint]
            [clojure.string :as s]
            [table.core :as table]
            [table.width]))


(defn format-md [format v]
  (case format
    :h1 (str "# " v)
    :h2 (str "## " v)
    :h3 (str "### " v)
    :h4 (str "#### " v)
    :h5 (str "##### " v)
    :h6 (str "###### " v)
    (:em :italic) (str "*" v "*")
    (:strong :bold) (str "**" v "**")
    :code (str "`" v "`")
    :clojure (str "```clojure\n"
                  (with-out-str
                    (pprint/pprint v))
                  "```")
    :template-source (str "```clojure\n    ```onyx-gen-doc\n"
                          (->> (binding [pprint/*print-right-margin* 50]
                                 (with-out-str
                                   (pprint/pprint v)))
                               (s/split-lines)
                               (map #(str "    " %))
                               (s/join "\n"))
                          "\n    ```\n```")
    v))

(defn format-edn [edn & [width]]
  (binding [pprint/*print-right-margin* (or width 80)]
    (format-md :clojure edn)))

(defn format-comment [message]
  (str "[//]: # (" message ")"))

(defn format-table-cell [v [_ _ format :as col-info]]
  (let [format' (cond
                  (nil? v) nil
                  (and (nil? format) (keyword? v)) :code
                  :else format)]
    (format-md format' v)))

(defn table-body [model col-infos]
  (map (fn row [[model-key _]]
         (map format-table-cell
              (into [model-key]
                    (map (fn model-attr-val [[col-key _ _]]
                           (col-key (model-key model)))
                         (rest col-infos)))
              (cycle col-infos)))
       model))

(defn gen-table [model col-infos table-width]
  (binding [table.width/*width* (delay table-width)] 
    (let [header (map second col-infos)
          body (table-body model col-infos)]  
      (table/table-str (into [header] body)
                       :style :github-markdown))))

(defn summary [information-model key]
  (let [entry (get-in information-model [:catalog-entry key :summary])]
    (assert entry ":model keyword must specify a catalog-entry in the information model.")
    entry))

(defn catalog-entry [information-model key]
  (let [entry (get-in information-model [:catalog-entry key :model])]
    (assert entry ":model keyword must specify a catalog-entry in the information model.")
    entry))

(defn lifecycle-entry [information-model key]
  (let [entry (get-in information-model [:lifecycle-entry key :model])]
    (assert entry ":model keyword must specify a lifecycle-entry in the information model.")
    entry))

(def priority-display-order [:onyx/name :onyx/plugin :onyx/type :onyx/medium])

(defn display-order-comparator [information-model key]
  (let [model-order (get-in information-model [:display-order key])
        ordering (zipmap (into priority-display-order model-order) (range))]
    (fn [a b]
      (compare (get ordering a (Math/abs (hash a)))
               (get ordering b (Math/abs (hash b)))))))

(defmulti gen-section (fn [config params]
                        (:display params)))

(defn section-error [ex config params]
  (when (:throw? config) (throw ex))
  (str "<!-- PARSE ERROR: " (.getMessage ex) "\n"
       (with-out-str (pprint/pprint (or (ex-data ex) params)))
       "-->"))

(defrecord Section [config params parse-error]
  Object
  (toString [_]
    (if parse-error
      (section-error parse-error config params)
      (try
        (let [out-md (gen-section config params)]
          (if (:verbose? config)
            (str (format-comment params) "\n" out-md)
            (or out-md "")))
        (catch Throwable ex
          (section-error ex config params))))))

(defmethod gen-section :default
  [_ params]
  (throw (ex-info "Unhandled :display type" {:params params})))

(defmethod gen-section :summary
  [{:keys [information-model]}
   {:keys [model format format-string]
    :or {format-string "%s"}}]
  (->> model
       (summary information-model)
       (clojure.core/format format-string)
       (format-md format)))

(defmethod gen-section :attribute-table
  [{:keys [information-model presets]}
   {:keys [model columns width] :or {width 800}}]
  (let [entry (-> (display-order-comparator information-model model)
                  (sorted-map-by)
                  (into (catalog-entry information-model model)))
        columns' (cond
                   (nil? columns) (get presets :columns/default)
                   (keyword? columns) (get presets columns)
                   :default columns)
        _ (assert columns' ":columns must be a vector of column specs or a keyword specifying a config preset.")]
    (gen-table entry columns' width)))

(defn ignore-value? [v]
  (identical? ::ignore v))

(defn infer-catalog-entry-values [catalog-entry merge-additions]
  (reduce-kv
   (fn [m k {:keys [default type optional? choices deprecation-version doc]}]
     (if (or deprecation-version
             (ignore-value? (k merge-additions)))
       m
       (let [default' (or default (first choices))]
         (if (not (nil? default'))
           (assoc m k default')
           (assoc m k (case type
                        :boolean false
                        :string doc
                        :keyword (keyword (str "my.ns/" (name k)))
                        :symbol (symbol (str "my.ns/" (name k)))
                        ::please-handle-in-merge-additions))))))
   {}
   catalog-entry))

(defmethod gen-section :catalog-entry
  [{:keys [information-model]}
   {:keys [model merge-additions width view-source?]}]
  (let [ entry (-> (catalog-entry information-model model)
                  (infer-catalog-entry-values merge-additions)
                  (merge (->> (when-not view-source? merge-additions)
                              (remove (fn [[_ v]] (ignore-value? v)))
                              (into {}))))
        sorted (sorted-map-by (display-order-comparator information-model model))
        entry' (into sorted entry)]
    (if view-source?
      (format-md :template-source (if merge-additions
                                    (assoc entry' :merge-additions merge-additions)
                                    entry'))
      (format-edn entry' width))))

(defmethod gen-section :lifecycle-entry
  [{:keys [information-model]}
   {:keys [model width]}]
  (let [entry (lifecycle-entry information-model model)]
    (format-edn entry width)))

(defmethod gen-section :header
  [{:keys [verbose? information-model]}
   {:keys [valid-structure? all-params]}]
  (assert valid-structure? "Template sections must be wrapped with ```onyx-doc-gen ... ```")
  (when verbose?
    ;; todo: set difference of the info-model catalog/lifecycle keys vs. markdown sections
    ))

(def code-block-re #"```")
(def onyx-gen-doc-re #"onyx-gen-doc(?=\s+\{)")

(defn parse-template [config md]
  (let [md-parts (s/split md code-block-re)
        valid-structure? (odd? (count md-parts))
        body (map-indexed
              (fn [idx md-part]
                (if (and valid-structure?
                         (odd? idx))
                  (if-let [template-md
                           (second (s/split md-part onyx-gen-doc-re))]
                    (try
                      (->Section config (edn/read-string template-md) nil)
                      (catch Throwable ex
                        ( ->Section config nil (ex-info "Error reading edn."
                                                        {:input template-md
                                                         :error-message (.getMessage ex)}))))
                    (str "```" md-part "```"))
                  md-part))
              md-parts)
        header (->Section config
                          {:display :header
                           :valid-structure? valid-structure?
                           :all-params (keep :params body)}
                          nil)]
    (into [header] body)))

(defn gen-template [config md]
  (s/join (parse-template config md)))

(defn run [{:keys [in-path out-path] :as config}]
  (let [in-md (slurp in-path)
        out-md (gen-template config in-md)]
    (spit out-path out-md)))

(comment
  (gen-template
   {:throw? true
    :information-model
    {:catalog-entry
     {:aplugin/read
      {:summary "reads"
       :model {:foo {:doc "foo" :type :string :default "bar"}
               :baz {:doc "" :type :map }
               :quux {:doc "" :type :symbol}}}}
     :display-order
     {:aplugin/read
      [:baz :foo]}}}
   (str
    "# Header ... \n"
    "sit amet ... \n\n"
    "```onyx-gen-doc \n"
    "{:display :summary\n"
    " :model :aplugin/read :format :h3 :format-string \"This does %s.\"} \n"
    "``` \n\n"
    "```onyx-gen-doc \n"
    "{:display :attribute-table \n\n"
    " :model :aplugin/read \n"
    " :columns [[:key \"Parameter\"] [:type \"Type\"]]} \n"
    "``` \n\n"
    "## Intermezzo ... \n"
    "lorem ipsum \n\n"
    "```clojure\n"
    "[org.onyxplatform/onyx-gen-doc \"0.0.0\"]\n"
    "```\n"
    "```onyx-gen-doc \n"
    "{:display :catalog-entry \n"
    " :model :aplugin/read \n"
    " :view-source? true"
    " :merge-additions \n
{:onyx/name :read \n
 :foo :onyx.gen-doc/ignore \n
 :extra true \n
 :onyx/secondary-key 100}}\n
```\n"
    "## Footer ..."
    ))
)

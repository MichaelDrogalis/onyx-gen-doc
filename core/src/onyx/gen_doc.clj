(ns onyx.gen-doc
  (:require [clojure.edn :as edn]
            [clojure.pprint :as pprint]
            [clojure.string :as s]
            [mapdown.core :as mapdown]
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
    :code (str "`" v "`")
    :clojure (str "```clojure\n"
                  (with-out-str
                    (pprint/pprint v))
                  "```")
    (:em :italic) (str "*" v "*")
    (:strong :bold) (str "**" v "**")
    v))

(defn format-edn [edn & [width]]
  (binding [pprint/*print-right-margin* (or width 40)]
    (format-md :clojure edn)))

(defn format-comment [message]
  (str "\n[//]: # (" message ")"))

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

(def barrier-re #":{10,200}")
(def block-ticks-re #"(?m)```(?:\w*)")
(def ticks-re #"`(.*?)`")

(defn strip-format [str]
  (-> str
      (s/replace block-ticks-re "")
      (s/replace ticks-re "$1")))

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

(defrecord Section [config params]
  Object
  (toString [_]
    (try
      (let [out-md (gen-section config params)]
        (if (:verbose? config)
          (str (format-comment params) "\n" out-md)
          (or out-md "")))
      (catch Throwable ex
        (when (:throw? config) (throw ex))
        (str "<!-- PARSE ERROR: " (.getMessage ex) "\n"
             (with-out-str (pprint/pprint (or (ex-data ex) params)))
             "-->")))))

(defmethod gen-section :default
  [_ params]
  (throw (ex-info "Unhandled :display type" {:params params})))

(defmethod gen-section :attribute-table
  [{:keys [information-model presets]}
   {:keys [model columns width] :or {width 800}}]
  (let [entry (-> (display-order-comparator information-model model)
                  (sorted-map-by)
                  (into (catalog-entry information-model model)))
        columns' (if (keyword? columns)
                   (get presets columns)
                   columns)
        _ (assert columns' ":columns must be a vector of column specs or a keyword specifying a config preset.")]
    (gen-table entry columns' width)))

(defn infer-catalog-entry-values [catalog-entry merge-additions]
  (reduce-kv
   (fn [m k {:keys [default type optional? choices deprecation-version doc]}]
     (when (or deprecation-version
               (identical? :gen-doc-ignore (k merge-additions)))
       m)
     (let [default' (or default (first choices))]
       (if default'
         (assoc m k default')
         (case type
           :boolean (assoc m k false)
           :string (when-not optional?
                     (assoc m k doc))
           (:long :map) (when-not optional?
                          (assoc m k :gen-doc-please-handle-in-merge-additions))
           :keyword (when-not optional?
                      (assoc m k (keyword (str "my.ns/" (name k)))))
           m))))
   {}
   catalog-entry))

(defmethod gen-section :catalog-entry
  [{:keys [information-model]}
   {:keys [model merge-additions width]}]
  (let [entry (-> (catalog-entry information-model model)
                  (infer-catalog-entry-values merge-additions)
                  (merge merge-additions))
        sorted (sorted-map-by (display-order-comparator information-model model))]
    (format-edn (into sorted entry) width)))

(defmethod gen-section :lifecycle-entry
  [{:keys [information-model]}
   {:keys [model width]}]
  (let [entry (lifecycle-entry information-model model)]
    (format-edn entry width)))

(defmethod gen-section :header
  [{:keys [verbose? information-model]}
   {:keys [valid-structure? all-params]}]
  (assert valid-structure? "Template sections must be delineated by :::::::::: at top and bottom.")
  (when verbose?
    ;; todo: set difference of the info-model catalog/lifecycle keys vs. markdown sections
    ))

(defn parse-section [config md]
  (->> (mapdown/parse md)
       (reduce-kv (fn [m k v]
                    (->> v
                         (strip-format)
                         (edn/read-string)
                         (assoc m k)))
                  {})
       ((partial ->Section config))))

(defn parse-template [config md]
  (let [md-parts (s/split md barrier-re)
        valid-structure? (odd? (count md-parts))        
        body (map-indexed (fn [idx md-part]
                            (if (and valid-structure? (odd? idx))
                              (parse-section config md-part)
                              md-part))
                          md-parts)
        header (->Section config {:display :header
                                  :valid-structure? valid-structure?
                                  :all-params (keep :params body)})]
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
      {:model {:foo {:doc "foo" :type :string :default "bar"}
               :baz {:doc "" :type :map }}}}
     :display-order
     {:aplugin/read
      [:baz :foo]}}}
   (str
    "# Header ... \n"
    "sit amet ... \n\n"
    ":::::::::::::::::::: \n"
    ":display `:attribute-table` \n\n"
    ":model :aplugin/read \n"
    ":columns [[:key \"Parameter\"] [:type \"Type\"]] \n"
    ":::::::::::::::::::: \n\n"
    "## Intermezzo ... \n"
    "lorem ipsum \n\n"
    ":::::::::::::::::::: \n"
    ":display :catalog-entry \n"
    ":model :aplugin/read \n"
    ":merge-additions \n
```clojure\n
{:onyx/name :read \n
 :foo \"buz\" \n
 :extra true \n
 :onyx/secondary-key 100}\n
```\n"
    ":::::::::::::::::::: \n\n"
    "## Footer ..."
    ))
)

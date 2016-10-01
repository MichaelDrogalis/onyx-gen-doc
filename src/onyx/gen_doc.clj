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
  (binding [pprint/*print-right-margin* (or width 20)]
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

(defn mapdown-parse [markdown]
  ;; todo rethrow ex with custom message
  (mapdown/parse markdown))

(def barrier-re #":{10,200}")
(def block-ticks-re #"(?m)```(?:\w*)")
(def ticks-re #"`(.*?)`")

(defn strip-format [str]
  (-> str
      (s/replace block-ticks-re "")
      (s/replace ticks-re "$1")))

(defn catalog-entry [information-model key]
  (get-in information-model [:catalog-entry key :model]))

(defn lifecycle-entry [information-model key]
  (get-in information-model [:lifecycle-entry key :model]))

(defn display-order [information-model key]
  (get-in information-model [:display-order key]))

(defmulti gen-section (fn [config params]
                        (:display params)))

(defrecord Section [config params]
  Object
  (toString [_]
    (try
      (let [out-md (gen-section config params)]
        (if (:verbose? config)
          (str (format-comment params) "\n" out-md)
          out-md))
      (catch Throwable ex
        (str "<!-- PARSE ERROR: " (.getMessage ex) "\n"
             (with-out-str (pprint/pprint (ex-data ex)))
             "-->")))))

(defmethod gen-section :default
  [_ params]
  (throw (ex-info "Unhandled :display type" {:params params})))

(defmethod gen-section :attribute-table
  [config {:keys [model columns]}])

(defn parse-section [config md]
  (->> (mapdown-parse md)
       (reduce-kv (fn [m k v]
                    (->> v
                         (strip-format)
                         (edn/read-string)
                         (assoc m k)))
                  {})
       ((partial ->Section config))))

(defn assert-parity [md-parts]
  (assert (odd? (count md-parts)) "Template sections must be delineated by :::::::::: at top and bottom")
  md-parts)

(defn parse-template [config md]
  (->> (s/split md barrier-re)
       (assert-parity)
       (map-indexed (fn [idx md-part]
                      (if (odd? idx)
                        (parse-section config md-part)
                        md-part)))))

(defn gen-template [config md]
  (s/join (parse-template config md)))

(comment
  (gen-template
   {}
   (str
    "# Header ... \n"
    "sit amet ... \n\n"
    ":::::::::::::::::::: \n"
    ":display `:attribute-table` \n"
    ":model :onyx.plugin.kafka/read-messages \n"
    ":columns [[:key \"Parameter\"] [:type \"Type\"]] \n"
    ":::::::::::::::::::: \n\n"
    "## Intermezzo ... \n"
    "lorem ipsum \n\n"
    ":::::::::::::::::::: \n"
    ":display :catalog-entry \n"
    ":model :onyx.plugin.kafka/read-messages \n"
    ":defaults \n
```clojure\n
{:foo \"bar\" \n
 :baz :quux}\n
```\n"
    ":::::::::::::::::::: \n\n"
    "## Footer ..."
    ))
)




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

(defn gen-table-markdown [model col-infos table-width]
  (binding [table.width/*width* (delay table-width)] 
    (let [header (map second col-infos)
          body (table-body model col-infos)]  
      (table/table-str (into [header] body)
                       :style :github-markdown))))

(defn mapdown-parse [markdown]
  ;; todo rethrow with custom message
  (mapdown/parse markdown))

(defmulti gen-section-markdown (fn [params] (:display params)))

(defmethod gen-section-markdown :default [params]
  (str params))

(def barrier-re #":{10,200}")
(def block-ticks-re #"(?m)```(?:\w*)")
(def ticks-re #"`(.*?)`")

(defn strip-format [str]
  (-> str
      (s/replace block-ticks-re "")
      (s/replace ticks-re "$1")))

(defn parse-section [md]
  (->> (mapdown-parse md)
       (reduce-kv (fn [m k v]
                    (->> v
                         (strip-format)
                         (edn/read-string)
                         (assoc m k)))
                  {})
       (gen-section-markdown)))

(defn assert-parity [parts]
  (assert (odd? (count parts)) "Template sections must be delineated by barriers.")
  parts)

(defn parse-template [md]
  (->> (s/split md barrier-re)
       (assert-parity)
       (map-indexed (fn [idx part]
                      (if (odd? idx)
                        (parse-section part)
                        part)))
       (s/join)))


(comment
  (parse-template
   (str
    "# Header ... \n"
    "sit amet ... \n\n"
    ":::::::::::::::::::: \n"
    ":display `:attribute-table` \n"
    ":model :onyx.plugin.kafka/read-messages \n"
    ":attribute-table/columns [[:key \"Parameter\"] [:type \"Type\"]] \n"
    ":::::::::::::::::::: \n"
    "## Intermezzo ... \n"
    "lorem ipsum \n\n"
    ":::::::::::::::::::: \n"
    ":display :entry"
    ":model :onyx.plugin.kafka/read-messages \n"
    ":entry/merge \n
```clojure\n
{:foo \"bar\" \n
 :baz :quux}\n
```\n
    :::::::::::::::::::: \n"
    "## Footer ..."
    ))
)




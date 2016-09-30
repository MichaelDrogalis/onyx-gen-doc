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

(defn gen-section-markdown [params]
  ;; todo
  (str params))

(defn parse-section [md]
  (->> (mapdown-parse md)
       (reduce-kv (fn [m' k v]
                    (assoc m' k (edn/read-string v)))
                  {})
       gen-section-markdown))

;; from https:/gihub.com/magnars/mapdown
(def eighty-dashes
  "--------------------------------------------------------------------------------")
(def eighty-dashes-re (re-pattern eighty-dashes))

(defn parse-template [md]
  (->> (s/split md eighty-dashes-re)
       (map-indexed (fn [idx part]
                      (if (odd? idx)
                        (parse-section part)
                        part)))
       (s/join)))


(comment
  (parse-section
   (str
    eighty-dashes "\n"
    ":model :some.ns/model\n"
    ":columns [[:key \"Parameter\"] [:type \"Type\"]]"
    eighty-dashes
    ))

  (parse-template
   (str
    "# Header \n"
    "Lots of text ... \n\n]"
    eighty-dashes "\n"
    ":model :some.ns/model\n"
    ":columns [[:key \"Parameter\"] [:type \"Type\"]]"
    eighty-dashes "\n"
    "## Intermezzo ... \n"
    eighty-dashes "\n"
    ":model :some.ns/model2\n"
    ":columns [[:key \"Parameter\"] [:type \"Type\"]]"
    eighty-dashes "\n"
    "## Footer ...."
    ))
)




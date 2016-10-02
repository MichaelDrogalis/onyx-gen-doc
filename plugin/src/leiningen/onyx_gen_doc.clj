(ns leiningen.onyx-gen-doc
  (:require [clojure.pprint]
            [leiningen.core.eval :as eval]
            [leiningen.core.project :as project]
            [leiningen.core.main :as main]))

(def onyx-gen-doc-profile
  {:onyx-gen-doc
   {:presets {:columns/default [[:key "Parameter"]
                                [:type "Type"]
                                [:optional? "Optional?" :code]
                                [:default "Default" :code]
                                [:doc "Description"]]}}})

(defn onyx-gen-doc
  "Render Onyx information model data into a Markdown template"
  [project]
  (let [profile (get-in project [:profiles :onyx-gen-doc] onyx-gen-doc-profile)
        project (project/merge-profiles project [profile])
        {:keys [information-model in-path out-path] :as config} (:onyx-gen-doc project)
        im-ns (-> information-model namespace symbol)]
    (main/info (format "Parsing %s into %s" in-path out-path))
    (eval/eval-in-project project
                          `(onyx.gen-doc/run ~config)
                          `(do (require '~im-ns)
                               (require 'onyx.gen-doc))))
  )

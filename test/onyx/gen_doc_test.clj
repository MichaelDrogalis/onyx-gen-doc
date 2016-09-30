(ns onyx.gen-doc-test
  (:require [clojure.test :refer [deftest is testing]]
            [onyx.gen-doc :as gen-doc]
            [onyx.kafka.information-model :as kafka]))

(deftest table
  (let [body (gen-doc/table-body
              (get-in kafka/model [:catalog-entry :onyx.plugin.kafka/read-messages :model])
              [[:key "Parameter"]
               [:type "Type"]
               [:optional? "Optional?" :code]
               [:default "Default" :code]
               [:doc "Description"]])]
    (is (= ["`:kafka/chan-capacity`" "`:long`" "`true`" "`1000`" "The buffer size of the Kafka reading channel."]
           (vec (last body))))))

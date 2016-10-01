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


(deftest display-order-comparator
  (let [cx (gen-doc/display-order-comparator kafka/model :onyx.plugin.kafka/read-messages)]
    (is (= [:onyx/name :kafka/topic :kafka/partition :onyx/batch-size]
           (sort cx [:kafka/partition :kafka/topic :onyx/name :onyx/batch-size])))))

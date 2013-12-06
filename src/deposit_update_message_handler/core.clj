(ns deposit-update-message-handler.core
    (:gen-class)

    (:require [clj-kafka.consumer.zk :refer :all])
    (:require [clj-kafka.producer :refer :all])
    (:require [clj-kafka.core :refer :all])
  )

(def producer-config {"metadata.broker.list" "localhost:9092"
                  "serializer.class" "kafka.serializer.DefaultEncoder"
                  "partitioner.class" "kafka.producer.DefaultPartitioner"})

(def consumer-config {
             "zookeeper.connect" "localhost:2181"
             "group.id" "clj-kafka.consumer"
             "auto.offset.reset" "smallest"
             "auto.commit.enable" "false"})

(defn process "Some fake processing"
  [input]
  (str "PROCESSED - " input))

(defn -main
  [& args]
  (prn "Starting")

  (let [p (producer producer-config)] 
  
  (with-resource [c (consumer consumer-config)]
    shutdown
  
    ; Loop forever doing the processing and posting back.
    (doseq [incoming-message (messages c ["incoming-email"])] 
      (let [value (apply str (map char (:value incoming-message)))
            processed-value (process value)]
        (prn ">>>" value)
        (send-message p (message "status-update" (.getBytes processed-value)))
    )
  ))
))
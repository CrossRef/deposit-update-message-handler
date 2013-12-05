(defproject deposit-update-message-handler "0.1.0-SNAPSHOT"
  :description "Handle DOI deposit messages on the message queue"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-kafka "0.1.1-0.8-beta1"]
                 [zookeeper-clj "0.9.3"]
                 
                 
                 
                 [zookeeper-clj "0.9.3"]
                 [org.clojure/data.json "0.2.2"]
                 
                 ;; kafka and its related deps
                 [org.apache.kafka/kafka_2.9.2 "0.8.0-beta1"]
                 [org.scala-lang/scala-library "2.9.2"]
                 [org.apache.zookeeper/zookeeper "3.3.4"]
                 [net.sf.jopt-simple/jopt-simple "3.2"]
                 [com.yammer.metrics/metrics-core "2.2.0"]
                 [com.101tec/zkclient "0.3"]
                 
                  [org.slf4j/slf4j-log4j12 "1.7.5"]
                 
                 ]
  :main ^:skip-aot deposit-update-message-handler.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

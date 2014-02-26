(defproject deposit-update-message-handler "0.1.0-SNAPSHOT"
  :description "Handle DOI deposit messages on the message queue"
  :url "http://example.com/FIXME"
  :repositories [["java.net" "http://download.java.net/maven/2"]]
  :plugins [[lein-environ "0.4.0"]]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/data.zip "0.1.1"]
                 [org.clojure/data.json "0.2.2"]
                 [org.clojure/tools.logging "0.2.6"]
                
                 ; Logging
                 [org.slf4j/slf4j-log4j12 "1.7.5"]
                 
                 ; http://search.maven.org/remotecontent?filepath=javax/mail/mail/1.5.0-b01/mail-1.5.0-b01.jar
                 [javax.mail/mail "1.4.7"]
                 [com.sun.mail/pop3 "1.4.4"]
                 
                 [environ "0.4.0"]
                 
                 [com.novemberain/monger "1.6.0"]
                 [clojurewerkz/quartzite "1.1.0"]
               ]
  :main ^:skip-aot deposit-update-message-handler.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  ; :resource-paths ["resources"]
  )

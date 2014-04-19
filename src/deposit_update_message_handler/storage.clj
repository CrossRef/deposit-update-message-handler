(ns deposit-update-message-handler.storage
  (:import [com.mongodb MongoOptions ServerAddress]
           [org.bson.types ObjectId] [com.mongodb DB WriteConcern])
  (:require [monger.core :as mg] 
            [monger.collection :as mc]
            [environ.core :refer [env]]
            [clojure.tools.logging :refer [info error]]))

(defn connect-mongo []
  (mg/connect!)
  (mg/set-db! (mg/get-db (env :mongo-db-name))))

(defn update-status 
  "Update an entry with the parsed submission log."
  [batch-id submission-log]
  (info "Updating status for batch id", batch-id)
  (let [new-status (if (or (zero? (:success-count submission-log))
                           (not (zero? (:failure-count submission-log))))
                     :failed
                     :completed)
        query {:batch-id batch-id}
        update {"$set" {:submission submission-log
                        :status new-status}}]
    (mc/upsert :deposits query update)))




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
  [batch-id status]
  (info "Updating status for batch id", batch-id)
  (mc/upsert :deposits {:batch-id batch-id} {"$set" {:submission-log status}}))




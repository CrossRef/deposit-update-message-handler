(ns deposit-update-message-handler.storage
    (:import
          [com.mongodb MongoOptions ServerAddress]
          [org.bson.types ObjectId] [com.mongodb DB WriteConcern])
    
     (:require [monger.core :as mg] [monger.collection :as mc]) 
     (:require [environ.core :refer [env]])
)

(defn connect-mongo []
    (mg/connect!)
    (mg/set-db! (mg/get-db (env :mongo-db-name)))
)

(defn update-status 
  "Update an entry with the parsed submission log."
  [submission-id status]
  (mc/upsert :deposits {:submission-id submission-id} {"$set" {:submission-log status}}))




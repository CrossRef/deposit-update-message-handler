(ns deposit-update-message-handler.parse
    (:gen-class)   
  
  
  (:require [clojure.data.zip.xml :as z]                                                                                                                            
            [clojure.zip :as zip]                                                     
            [clojure.xml :as xml]                                                                                                                                         
            [clojure.pprint :refer [pprint]]) 
  
  (:require [clojure.string :refer :all])

  
)

(defn parse-int-or-zero
  "Parse an integer from a string or return zero if nil"
  [s]
  (if (nil? s) 0
   (Integer. (re-find  #"\d+" s ))))



(defn extract-record-info 
  "Extract information from a record_diagnostic tag."
  [record]
  { 
   :success (= (-> (first record) :attrs :status) "Success")
   :doi (z/xml1-> record :doi z/text)
   :message (trim (z/xml1-> record :msg z/text))
    }
  )

(defn parse-xml
  "Parse a message's XML content"
  [input]
  (let [
        x (xml/parse (java.io.ByteArrayInputStream. (.getBytes input)))
        zipped (zip/xml-zip x)
        submission-id (parse-int-or-zero (z/xml1-> zipped :submission_id z/text))
        batch-id (parse-int-or-zero (z/xml1-> zipped :batch_id z/text))
        record-count (parse-int-or-zero(z/xml1-> zipped :batch_data :record_count z/text))
        success-count (parse-int-or-zero(z/xml1-> zipped :batch_data :success_count z/text))
        warning-count (parse-int-or-zero(z/xml1-> zipped :batch_data :warning_count z/text))
        failure-count (parse-int-or-zero(z/xml1-> zipped :batch_data :failure_count z/text))
        records (z/xml-> zipped :record_diagnostic)
        records-info (map extract-record-info records)
    ]
        
    {
     :submission-id submission-id
     :batch-id batch-id
     :record-count record-count
     :success-count success-count
     :warning-count warning-count
     :failure-count failure-count
     :records records-info
     }
  )    
)

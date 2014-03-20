(ns deposit-update-message-handler.parse
  (:require [clojure.data.zip.xml :as z]                                                                        
            [clojure.zip :as zip]                                                     
            [clojure.xml :as xml]      
            [clojure.pprint :refer [pprint]]
            [clojure.string :refer [trim]])
  (:import [org.xml.sax SAXParseException]))

; http://help.crossref.org/#suberrors
; http://help.crossref.org/#ID5768
(def error-type-identifiers
  {"MalformedXmlException" :xml-syntax-malformed
   
   ;; Added with conflict
   "Added with conflict" :submission-added-with-conflict
   
   ;; Record not processed because submitted version: xxxxxxx is less or equal to previously submitted version (DOI match) 
   "Record not processed because submitted version" :submission-version-older-than-last
   
   ;; User with ID: {0} cant submit into handle, please contact the CrossRef admin
   "submit into handle" :permission-not-your-handle
   
   ;; User not allowed to add records for prefix: {0}
   "User not allowed to add records for prefix" :permission-not-your-prefix
   
   ;; All prefixes in a submission must match (DOI[{0}]) 
   "All prefixes in a submission must match" :xml-content-differing-prefixes
   
   ;; year: {0} in not a valid integer
   "in not a valid integer" :xml-content-invalid-year
   
   ;; title "{title}" was previously deleted by a CrossRef admin
   "was previously deleted by a CrossRef" :submission-title-deleted-by-crossref-admin
   
   ;; user not allowed to add or update records for the title "{title}"
   "user not allowed to add or update records for the title" :permission-not-your-title
   
   ;; ISSN "12345678" has already been assigned to a different title/publisher/genre
   "has already been assigned to a different" :permission-not-your-issn
   
   ;; [error] :286:24:Invalid content starting with element {element name}'. The content must match '(("http://www.crossref.org/schema/4.3.0": item_number) {0-3}, ("http://www.crossref.org/schema/4.3.0": identifier) {0-10})
   "Invalid content starting with element" :xml-syntax-schema-validation-fail
   
   ;; org.jdom.input.JDOMParseException: Error on line 312 of document file:///export/home/resin/journals/crossref/inprocess/395032106: The content of elements must consist of well-formed character data or markup.
   "JDOMParseException" :xml-syntax-bad-character-data
   
   ;; [fatal error] :1:1: Content is not allowed in prolog.
   "Content is not allowed in prolog" :xml-syntax-content-in-prolog
   
   ;; java.io.UTFDataFormatException: invalid byte 1 of 1-byte > UTF-8 sequence (0x92) 
   "UTFDataFormatException" :xml-syntax-bad-character-encoding
   
   ;; java.sql.SQLException: ORA-00001: unique constraint (ATYPON.NDX1_CIT_RELS) violated
   "ATYPON.NDX1_CIT_RELS" :submission-unique-doi
   
   ;; java.lang.NullPointerException
   "NullPointerException" :submission-npe
   
   ;; Submission version NULL is invalid 
   "Submission version NULL is invalid" :xml-content-submission-version-is-null})

(defn parse-int-or-zero
  "Parse an integer from a string or return zero if nil"
  [s]
  (if (nil? s) 0
   (Integer. (re-find  #"\d+" s ))))

(defn status
  [input]
  (case (-> (first input) :attrs :status)
    "Success" :success
    "Failure" :failure
    "Warning" :warning))

(defn extract-types-from-message
  "Extract identifiers from message text."
  [message]
  (letfn [(message-contains [needle]
            (>= (.indexOf message needle) 0))
          (append-if-contains [acc [identifier-string, identifier-symbol]]
            (if (message-contains identifier-string)  
              (conj acc identifier-symbol)
              acc))]
    (reduce append-if-contains #{} error-type-identifiers)))

(defn extract-record-info 
  "Extract information from a record_diagnostic tag."
  [record]
  (let [message (trim (z/xml1-> record :msg z/text))]
    {:status (status record) 
     :doi (z/xml1-> record :doi z/text)
     :message message
     :message-types (extract-types-from-message message)}))

(defn parse-xml
  "Parse a message's XML content. Raise SAXParseException."
  [input]
    (let [x (xml/parse (java.io.ByteArrayInputStream. (.getBytes input)))
          zipped (zip/xml-zip x)
          submission-id (parse-int-or-zero (z/xml1-> zipped :submission_id z/text))
          batch-id (parse-int-or-zero (z/xml1-> zipped :batch_id z/text))
          record-count (parse-int-or-zero(z/xml1-> zipped :batch_data :record_count z/text))
          success-count (parse-int-or-zero(z/xml1-> zipped :batch_data :success_count z/text))
          warning-count (parse-int-or-zero(z/xml1-> zipped :batch_data :warning_count z/text))
          failure-count (parse-int-or-zero(z/xml1-> zipped :batch_data :failure_count z/text))
          records (z/xml-> zipped :record_diagnostic)
          records-info (map extract-record-info records)]
    {:submission-id submission-id
     :batch-id batch-id
     :record-count record-count
     :success-count success-count
     :warning-count warning-count
     :failure-count failure-count
     :records records-info}))

(defn parse-xml-robust
  "Parse a message's XML content. Return nil on error. This can be passed any kind of message that might appear in the mail account's inbox."
  [input]
  (try
    (parse-xml (trim input))
    (catch SAXParseException _ nil)
    (catch NullPointerException _ nil)))

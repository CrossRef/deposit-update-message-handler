(ns deposit-update-message-handler.mail
    (:gen-class)
    (:import [javax.mail URLName Session Flags$Flag Flags Folder]
             [javax.mail.internet MimeMultipart]
             [com.sun.mail.pop3 POP3Store]
             [java.util Properties])
    (:require [clojure.tools.logging :refer [info error]]
              [environ.core :refer [env]]))

; Background reading http://www.oracle.com/technetwork/java/faq-135477.html
(defn fetch-mail
  "Fetch a batch of messages. Call callback with the string of each message body. If the callback returns true, delete the message."
   [callback]  
  (let [properties (Properties.)
        url (URLName. "pop3" "mailserv.crossref.org" 110 "" (env :email-username) (env :email-password))
        session (Session/getInstance properties nil)
        store (POP3Store. session url)]
        (.connect store)
        (info "Connected to mailbox")
        (let [folder (.getFolder store "INBOX")]
          (info "Opening mailbox")
          (.open folder Folder/READ_WRITE)
          (info "Done")
          (let [messages (.getMessages folder)]
            (doall (map (fn [message]
              (let [content (.getContent message)
                    subject (.getSubject message)]
                            
                ; The content might be a string for a simple message or a multi-part.
                (if (instance? String content)
                  (do
                    (info "Got simple message with subject" subject)
                    
                    (let [callback-status (callback content)]
                      (info (if callback-status 
                              "Callback for at simple mail succeeded" 
                              "Callback did not succeed"))
                      (if callback-status
                        (do
                           (info "Deleting simple message with subject" subject)
                           (.setFlag message Flags$Flag/SEEN true)
                           (.setFlag message Flags$Flag/DELETED true))))))
                
                (if (instance? MimeMultipart content)
                  (let [part-count (.getCount content)
                        subject (.getSubject message)
                        parts (doall (map #(-> (.getBodyPart content %) .getContent .toString) (range 0 part-count)))
                        callback-result (doall (map #(callback (.toString %)) parts))
                        worked-for-at-least-one-part (not (every? false? callback-result))]
                    (info "Got multi-part message with subject" subject)
                    (info (if worked-for-at-least-one-part 
                            "Callback for at least one part of multi-part succeeded" 
                            "Callback did not succeed"))
                    (if worked-for-at-least-one-part
                      (do
                        (info "Deleting multi-part message with subject" subject)
                        (.setFlag message Flags$Flag/SEEN true)
                        (.setFlag message Flags$Flag/DELETED true)))))))
                messages))

    ; This will commit any message deletions if we want it to.
    (info "Closing mail")
    (.close folder true)
    (info "Closing mail store")
    (.close store)          
    (info "Done mail job")))))

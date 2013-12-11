(ns deposit-update-message-handler.mail
    (:gen-class)

 (:import javax.mail.URLName
          javax.mail.Session
          javax.mail.Flags$Flag
          javax.mail.Flags
          javax.mail.Folder
          javax.mail.internet.MimeMultipart
          com.sun.mail.pop3.POP3Store
          java.util.Properties
))

; Background reading http://www.oracle.com/technetwork/java/faq-135477.html
(defn fetch-mail
  "Fetch a batch of messages. Call callback with the string of each message body. If the callback returns true, delete the message."
   [callback]  
  (let [properties (new Properties)
        url (new URLName "pop3" "mailserv.crossref.org" 110 "" "USERNAME" "PASSWORD")
        session (Session/getInstance properties nil)
        store (new POP3Store session url)]
        (.connect store)
        (prn "connected")
        
        (let [folder (.getFolder store "INBOX")]
          (prn "open...")
          (.open folder Folder/READ_WRITE)
          (prn "opened")
          (let [messages (.getMessages folder)]
            (doall (map (fn [message]
              (let [content (.getContent message)
                    subject (.getSubject message)]
                            
                ; The content might be a string for a simple message or a multi-part.
                (if (instance? String content)
                  (do
                    (prn "Simple message")
                    (prn "Got subject:" subject)
                    (prn "Got content" content)

                    (if (callback content)
                      (do
                         (prn "Deleting" message)
                         (.setFlag message Flags$Flag/SEEN true)
                         (.setFlag message Flags$Flag/DELETED true)))))
                
                (if (instance? MimeMultipart content)
                  (let [part-count (.getCount content)
                        parts (doall (map #(-> (.getBodyPart content %) .getContent .toString) (range 0 part-count)))
                        callback-result (doall (map #(callback (.toString %)) parts))
                        worked-for-at-least-one-part (not (every? false? callback-result))]
                    (prn "Got subject:" subject)

                    (if worked-for-at-least-one-part
                      (do
                        (prn "Deleting")
                        (.setFlag message Flags$Flag/SEEN true)
                        (.setFlag message Flags$Flag/DELETED true)))))))
                messages))

    ; This will commit any message deletions if we want it to.
    (prn "close folder")
    (.close folder true)
    (prn "close store")
    (.close store)          
    (prn "done")))))
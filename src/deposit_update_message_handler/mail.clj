(ns deposit-update-message-handler.mail
    (:gen-class)

 (:import javax.mail.URLName
          javax.mail.Session
          javax.mail.Flags$Flag
          javax.mail.Folder
          com.sun.mail.pop3.POP3Store
          java.util.Properties
))

; Background reading http://www.oracle.com/technetwork/java/faq-135477.html
(defn fetch-mail
  "Fetch a batch of messages. Call callback with the string of each message body. If the callback returns true, delete the message."
   [callback]  
  (let [properties (new Properties)
        url (new URLName "pop3" "mailserv.crossref.org" 110 "" "USER" "PASSWORD")
        session (Session/getInstance properties nil)
        store (new POP3Store session url)
        ]
        (prn "connect...")
        (.connect store)
        (prn "connected")
        
        (let [folder (.getFolder store "INBOX")]
          (prn "open...")
          (.open folder Folder/READ_WRITE)
            (prn "opened")
          (let [messages (.getMessages folder)]
            (doall (map (fn [message]
                          (let [content (.getContent message)
                                subject (.getSubject message)
                                ]
                            (prn "Got subject:" subject)
                            (if (not (nil? content))
                              (let [callback-result (callback (.toString content))]
                                (if callback-result
                                  (prn "Deleting")
                                  (.setFlag message Flags$Flag/DELETED true))))    
                                )
                              )                            
                        messages
        )))
          
        ; This will commit any message deletions if we want it to.
        (.close folder true)
        (prn "close store")
        (.close store)          
        (prn "done")
    )
        
)
        
        
        
  
  ;try {
            ; Properties props = new Properties();
            ; Session session;

            ; URLName url = new ;
            ; session = Session.getInstance(props, null);
            ; Store store = new POP3SSLStore(session,url);
            ; store.connect();

            ; Folder folder = store.getFolder("INBOX");
            ; folder.open(Folder.READ_ONLY);

            ; Message message[] = folder.getMessages();

            ; for (int i=0, n = message.length; i<n; i++) {
            ;     System.out.println(message[i].getSubject());
            ; }
        ;     folder.close(false);
        ;     store.close();
        ; }
        ; catch (MessagingException e) {
        ;     System.out.println("Error: " + e);
        ; }
  
  
  
  )
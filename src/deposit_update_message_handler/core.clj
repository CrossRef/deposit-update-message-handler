(ns deposit-update-message-handler.core
    (:gen-class)
    (:require [deposit-update-message-handler.mail :refer :all]
              [deposit-update-message-handler.parse :refer :all]
              [deposit-update-message-handler.storage :refer :all]
              [deposit-update-message-handler.storage :refer [connect-mongo update-status]]
              
              )

    (:require [clojurewerkz.quartzite.triggers :as qt]
            [clojurewerkz.quartzite.jobs :as qj]
            [clojurewerkz.quartzite.schedule.daily-interval :as daily]
            [clojurewerkz.quartzite.schedule.calendar-interval :as cal])
   (:use [clojurewerkz.quartzite.jobs :only [defjob]])
   (:require [clojurewerkz.quartzite.scheduler :as qs]))

(defn poll-email
  []
  
  (defn process [message]
    (let [response (parse-xml-robust message)]

            
      (when (not (nil? response))
        (update-status (:submission-id response) response)  
      )
      
      ; Return true if this was processed OK and we can delete the email.
      (not (nil? response))))
    
   (fetch-mail process))

(defjob PollEmailJob
  [ctx]
  (poll-email))

(defn -main
  [& args]
  (qs/initialize)
  (connect-mongo)
  
  (qs/start)
  
  (let [job (qj/build
              (qj/of-type PollEmailJob)
              (qj/with-identity (qj/key "jobs.poll-email"))
              )
        trigger (qt/build
                  (qt/with-identity (qt/key "triggers.poll-email"))
                  (qt/start-now)
                  (qt/with-schedule (cal/schedule (cal/with-interval-in-seconds 30))))]
        (qs/schedule job trigger)))
(ns deposit-update-message-handler.core
    (:gen-class)
    (:require [deposit-update-message-handler.mail :refer :all]
              [deposit-update-message-handler.parse :refer :all]
              [deposit-update-message-handler.storage :refer :all]
              [deposit-update-message-handler.storage :refer [connect-mongo update-status]]
              [clojurewerkz.quartzite.triggers :as qt]
              [clojurewerkz.quartzite.jobs :as qj]
              [clojurewerkz.quartzite.schedule.daily-interval :as daily]
              [clojurewerkz.quartzite.schedule.calendar-interval :as cal]
              [clojurewerkz.quartzite.jobs :refer [defjob]]
              [clojurewerkz.quartzite.scheduler :as qs]
              [clojure.tools.logging :refer [info error]]))

(defn process-message [message]
  (let [response (parse-xml-robust message)]
    (info "Schedule run")
    (when (not (nil? response))
      (update-status (:submission-id response) response))
    ;; Return true if this was processed OK and we can delete the email.
    (not (nil? response))))

(defjob PollEmailJob
  [ctx]
  (fetch-mail process-message))

(defn -main
  [& args]
  (qs/initialize)
  (info "Starting")
  (connect-mongo)
  (info "Start scheduler")
  (qs/start)
  (let [job (qj/build
             (qj/of-type PollEmailJob)
             (qj/with-identity (qj/key "jobs.poll-email")))
        trigger (qt/build
                 (qt/with-identity (qt/key "triggers.poll-email"))
                 (qt/start-now)
                 (qt/with-schedule (cal/schedule (cal/with-interval-in-seconds 30))))]
    (qs/schedule job trigger)))


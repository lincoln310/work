(ns demo.transactions
  (:require [immutant.transactions.scope :as tx]
            [immutant.messaging :as msg]
            [immutant.caching :as csh]
            [demo.db-layer.loc-records :as loc-records]
            [clojure.java.jdbc :as sql]))

(def cache (delay (csh/cache (str *ns*) :transactional? true)))
(def queue (delay (msg/queue (str *ns*))))


(defn unit-of-work [m]
  ;(tx/not-supported
  ;  (csh/swap-in! @cache :attempts (fnil inc 0)))
  ;(csh/swap-in! @cache :count (fnil inc 0))
  ;(msg/publish @queue m)
  ;(loc-records/new-records! m)
  )

(defn dump []
  ;(println "tx:queue =>" (msg/receive @queue :timeout -1))
  ;(println "tx:cache =>" (into {} @cache))
  (let [ret (loc-records/list-all)]
    (println "tx:db    =>" ret)
    ret))

(defn dotx [f & args]
  (tx/required (f args)))

(defn -main [& _]
  (dump))

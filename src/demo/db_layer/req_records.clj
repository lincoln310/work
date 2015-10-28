(ns demo.db-layer.req-records
  (:require [clojure.java.jdbc :as sql]
            [clojure.tools.logging :as log]
            [korma.core :as korma-core]
            [korma.db :as korma-db])
  (:use demo.db-layer.db-define
        demo.db-layer.db-json))

(def table :req_record)

(defn create-tb!
  []
  (sql/db-do-commands
   db (sql/create-table-ddl
       table
       [:sid :serial]
       [:req "json"]
       [:calc "json"]
       [:result "json"]
       [:req_time :timestamp]
       [:res_time :timestamp])))

(defn drop-tb!
  []
  (sql/db-do-commands
   db (sql/drop-table-ddl
       table)))

(defn- ext-req
  [{each :each}]
  (log/info "ext-req" each)
  (let [ret (map #(select-keys % [:sid :bssid :rssi]) each)]
    (log/debug ret)
    ret
    ))

(defn- ext-calc
  [{each :each}]
  (log/debug each)
  (map #(select-keys % [:sid :p_mult :p_plus]) each)
  )

(defn- ext-result
  [{each :each :as data}]
  (log/debug each)
  (merge (select-keys data [:loc])
         {:each (map #(select-keys % [:sid :distance]) each)})
  )

(defn- ext-time
  [data]
  (log/debug data)
  (select-keys data [:req-time :res-time])
  )

(defn- do-insert!
  [m]
  (sql/db-do-commands
   db (sql/insert! db table m)))

(defn insert!
  [data]
  (log/info "insert to log-db:" data)
  (let [m {:req (ext-req data)
           :calc (ext-calc data)
           :result (ext-result data)
           :req_time (:req-time data)
           :res_time (:res-time data)}]
    (log/info "insert:" m)
    (do-insert! m)))

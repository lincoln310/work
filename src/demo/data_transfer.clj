(ns demo.data-transfer
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]))

(defn trans-dssi-to-id [param]
  (conj param {:id (Long/valueOf (clojure.string/replace (param :bssid) ":" "") 16)}))

(defn- trans-all-ids [params]
  (map trans-dssi-to-id params))

(defn- value-convert [[k v]]
  (log/info k v)
  (log/info (type v))
  {k (read-string v)})

(defn convert-all-type [l]
  (log/info l)
  (map #(apply conj (map value-convert %)) l))

(defn do-trans [l]
  "output:  [{:id 1, :rssi 11.111},
  {:id 2, :rssi 22.222},
  {:id 3, :rssi 33.333},
  {:id 4, :rssi 44.444}]"
  (log/info l)
  (trans-all-ids l))

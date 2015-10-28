(ns demo.data-processer
  (:require [demo.db-layer.loc-records :as sql]
            [clojure.tools.logging :as log]
            [demo.logic.calc-loc  :as calc]))

(defn do-process [l]
  "input:  [{:id 1, :rssi 11.111},
  {:id 2, :rssi 22.222},
  {:id 3, :rssi 33.333},
  {:id 4, :rssi 44.444}]"
  (log/info "before-process" l)
  (let [ids (map :id l) ;[1 2 3 4]
        l (map #(conj %1 %2) l (sql/get-record ids))]
    (log/info "middle-process" l)
    (calc/get-loc
     (for [tmp l]
       (conj tmp (calc/get-distance tmp))))))

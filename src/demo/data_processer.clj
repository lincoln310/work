(ns demo.data-processer
  (:require [demo.db-layer.loc-records :as sql]
            [clojure.tools.logging :as log]
            [demo.logic.calc-loc  :as calc]))

(defn do-process [{l :each :as data}]
  "input:  [{:id 1, :rssi 11.111},
  {:id 2, :rssi 22.222},
  {:id 3, :rssi 33.333},
  {:id 4, :rssi 44.444}]"
  (log/info "before-process" data)
  (let [records (sql/get-record (map :id l))
        l (map #(conj %1 %2) l records)
        l (map #(conj % (calc/get-distance %)) l)
        loc (calc/get-loc l)
        ret (merge data
               {:each l}
               {:loc loc}
               )]
    (log/info "middle-process" data)
    (log/info l)
    (log/info loc)
    (log/info ret)
    ret))

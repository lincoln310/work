(ns demo.data-processer
  (:require [demo.db-layer.loc-records :as sql]
            [clojure.tools.logging :as log]
            [demo.logic.calc-loc  :as calc]))

(defn do-process [l]
  "input:  [{:devId 1, :cssi 11.111},
  {:devId 2, :cssi 22.222},
  {:devId 3, :cssi 33.333},
  {:devId 4, :cssi 44.444}]"
  (log/info l)
  (let [ids (map :devId l) ;[1 2 3 4]
        locs (sql/get-record ids) ;["Point(1 1)" "Point(2 2)" ...]
        indicates (zipmap ids locs)]
    (println indicates)
    (calc/get-loc (for [[indicate cssi] indicates]
      (calc/get-distance indicate cssi)))))

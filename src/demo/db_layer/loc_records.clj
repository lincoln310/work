(ns demo.db-layer.loc-records
  (:require [clojure.java.jdbc :as sql]))

(def db {:subprotocol "postgresql"
                  :subname "location"
                  :user "tsinghua"
                  :password "tsinghua"})

(defn new-record! [data]
  (let [sql-cmd (format "insert into loc values(%d, ST_GeomFromText('%s', 26910), now());" (:id data) (:geo data))]
    (println sql-cmd)
    (sql/execute! db [sql-cmd])))

(defn new-records! [datas]
  (for [data datas]
    (new-record! data)))

(defn get-record [cri]
  (let [sql-cmd (format "select id, ST_AsText(geom) as geo, update_time, valid from loc where id in (%s);" (clojure.string/join "," cri))
        ret (sql/query db sql-cmd)]
    (println sql-cmd)
    (println ret)
    (map :geo ret)))

(defn list-all []
  (sql/query db "SELECT id, ST_AsText(geom) as geo, update_time, valid FROM loc ORDER BY update_time DESC;"))

(defn update-record! [id point]
  (sql/execute! db [(format "UPDATE loc SET geom = ST_GeomFromText('%s', 26910), update_time=now() WHERE id = %d;" point id)]))

(defn disable-record! [id]
  (sql/execute! db [(format "update loc set valid=0 WHERE id=%d;" id)]))

(defn enable-record! [id]
  (sql/execute! db [(format "update loc set valid=1 WHERE id=%d;" id)]))

(ns demo.db-layer.loc-records
  (:require [clojure.java.jdbc :as sql]
            [clojure.tools.logging :as log]
            [korma.core :as korma-core]
            [korma.db :as korma-db])
  (:use demo.db-layer.db-define
        demo.db-layer.db-json))

(defn- all-columns []
  "loc table的所有有效字段"
  #{:sid :id :geom :update_time :valid :p_plus :p_mult :tag})

(defn create-tb! []
  (sql/db-do-commands
   db (sql/create-table-ddl
       :loc
       [:sid :serial "PRIMARY KEY"]
       [:id "bigint"]
       [:geom "geometry"]
       [:valid :int]
       [:p_plus :real]
       [:p_mult :real]
       [:update_time :timestamp]
       [:tag "json"]
       )))

(defn drop-tb! []
  (sql/db-do-commands
   db (sql/drop-table-ddl
       :loc)))

(defn- translate-to-sql [[k v]]
  "针对不同的table字段，转换成insert和update所需要的sql指令"
  (cond
    (= :geo k) {:geom (format "ST_GeomFromText('%s', 26910)" v)}
    (= :p_plus k) {k (float v)}
    (= :p_mult k) {k (float v)}
    (= :sid k) {k "default"}
    (k (all-columns)) {k v}
    :else nil))

(defn new-record! [data]
  "添加一条新数据"
  (let [params (apply merge (map translate-to-sql data))
        params (conj params {:update_time "now()"})
        params-str (clojure.walk/stringify-keys params)
        sql-cmd (format "insert into loc (%s) values(%s);"
                        (clojure.string/join "," (keys params-str))
                        (clojure.string/join "," (vals params-str)))]
    (log/info "cmd:" sql-cmd)
    (sql/execute! db [sql-cmd])))

(defn new-records! [datas]
  "批量添加数据"
  (log/info "new-records:" datas)
  (map new-record! datas))

(defn get-record [cri]
  "获取数据，传入的是id的array"
  (let [sql-cmd (format "select sid,id,p_plus, p_mult, tag, update_time, ST_AsText(geom) as geo from loc where id in (%s);"
                        (clojure.string/join "," cri))]
    (log/info sql-cmd)
    (sql/query db sql-cmd)))

(defn list-all []
  "获取所有的数据"
  (sql/query db "SELECT *, ST_AsText(geom) as geo FROM loc ORDER BY update_time DESC;"))

(defn update-record! [id-array updates]
  ^{:doc "更新数据"
    :pre (coll? id-array updates)}
  (let [transformed (apply merge (map translate-to-sql updates))
        transformed (conj transformed {:update_time "now()"})
        transformed-str (clojure.walk/stringify-keys transformed)
        updates-str-list (map #(clojure.string/join "=" %) transformed-str)
        updates-str (clojure.string/join "," updates-str-list)
        cmd (format "UPDATE loc set %s where id in (%s);"
                    updates-str
                    (clojure.string/join "," id-array))]
    (sql/execute! db [cmd])))

(defn delete-record! [id-array]
  (sql/with-db-connection [con-db db]
    (sql/execute! db ;delete! con-db :loc
                  [(format "delete from loc where id in (%s);" (clojure.string/join "," id-array))])))

;;; for calculate using postgis
(defn get-centroid [polygon]
  (log/info "centroid: " polygon)
  (let [multi-p (clojure.string/replace polygon "POLYGON((" "MULTIPOINT(")
        multi-p (clojure.string/replace multi-p "))" ")")
        cmd (format "select ST_AsText(ST_Centroid('%s')) as center;" multi-p)
        ret (sql/query db cmd)]
    (log/info cmd)
    (log/info ret)
    (get (first ret) :center)))

(defn get-circle [c]
  (format "ST_Buffer('%s', %f)" (:geo c) (float (:distance c))))

(defn get-polygon [pl]
  (format "'%s'" pl))

(defn get-intersection-sql-cmd [s1 s2]
  (format "select ST_AsText(ST_Intersection(%s::geometry, %s::geometry)) as ret;" s1 s2))

(defn do-intersection [buffer1 buffer2]
  (let [sql-cmd (get-intersection-sql-cmd buffer1 buffer2)
        ret (sql/query db sql-cmd)]
    (log/info sql-cmd)
    (log/info ret)
    (get (first ret) :ret "POLYGON(())")))

(defn get-intersection-circle-circle [c1 c2]
  (do-intersection (get-circle c1) (get-circle c2)))

(defn get-intersection-polygon-polygon [pl1 pl2]
  (do-intersection (get-polygon pl1) (get-polygon pl2)))


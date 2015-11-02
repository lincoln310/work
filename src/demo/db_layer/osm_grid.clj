(ns demo.db-layer.osm-grid
  (:require [clojure.java.jdbc :as sql]
            [clojure.tools.logging :as log]
            [korma.core :as korma-core]
            [korma.db :as korma-db])
  (:use demo.db-layer.db-define))

(defn get-bondingbox []
  (first (sql/query db ["select min(lat) as min_lat, min(lon) as min_lon, max(lat) as max_lat, max(lon) as max_lon from planet_osm_nodes;"])))

(defn get-tables []
  (sql/query db ["select "]))

(defn get-grid [{:keys [min_lat min_lon max_lat max_lon] :as data}]
  (log/debug data)
  (let [lat-range (- max_lat min_lat)
        lon-range (- max_lon min_lon)]
    (for [x (range 0 lon-range 50)
          y (range 0 lat-range 50)]
      {:lon (+ x min_lon) :lat (+ y min_lat)})))

(defn check-inside [g1 g2]
  (sql/query db [(format "select ST_Within(ST_GeomFromText('%s', 26910), ST_GeomFromText('%s', 26910)) as result;" g1 g2)]))

(defn get-ways []
  (sql/query db ["select * from planet_osm_ways;"]))

(defn is-polygon [g]
  (apply = ((juxt first last) (:nodes g))))

(defn is-blocked [g]
  (not (empty? (filter #{"bench" "wall"} (:tags g)))))

(defn get-blocked [data]
  (filter #(every? true? ((juxt is-polygon is-blocked) %)) data))

(defn get-nodes [ids]
  (let [cmd (format "select * from planet_osm_nodes where id in (%s);" (clojure.string/join "," ids))]
    (println cmd)
    (sql/query db [cmd])))

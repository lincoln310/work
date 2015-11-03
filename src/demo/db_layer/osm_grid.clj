(ns demo.db-layer.osm-grid
  (:require [clojure.java.jdbc :as sql]
            [clojure.tools.logging :as log]
            [korma.core :as korma-core]
            [korma.db :as korma-db])
  (:use demo.db-layer.db-define))

(defn get-bondingbox
  "获取区域的边界"
  []
  (first (sql/query db ["select min(lat) as min_lat, min(lon) as min_lon, max(lat) as max_lat, max(lon) as max_lon from planet_osm_nodes;"])))

(defn get-grid
  "根据边界生成网格"
  [{:keys [min_lat min_lon max_lat max_lon] :as data}]
  (log/debug data)
  (let [lat-range (- max_lat min_lat)
        lon-range (- max_lon min_lon)]
    (for [x (range 0 lon-range 50)
          y (range 0 lat-range 50)]
      {:lon (+ x min_lon) :lat (+ y min_lat)})))

(defn get-geometry-point [m]
  (format "POINT(%s %s)" (:lon m) (:lat m)))

(defn check-inside
  "检查g1是否在g2内"
  [g1 g2]
  (log/debug g1 " <:> " g2)
  (sql/query db [(format "select ST_Within(ST_GeomFromText('%s', 26910), ST_GeomFromText('%s', 26910)) as result;" g1 g2)] :result-set-fn (comp :result first)))

(defn get-ways
  "从planet_osm_ways表中获取所有的way数据"
  []
  (sql/query db ["select * from planet_osm_ways;"]))

(defn is-polygon
  "判断是否polygon，即闭合"
  [g]
  (apply = ((juxt first last) (:nodes g))))

(defn is-blocked
  "判断是否障碍"
  [g]
  (not (empty? (filter #{"bench" "wall"} (:tags g)))))

(defn get-blocked-polygons-nodes
  "过滤闭合的障碍区域"
  [data]
  (filter #(every? true? ((juxt is-polygon is-blocked) %)) data))

(defn get-nodes [ids]
  (let [cmd (format "select id,lon,lat from planet_osm_nodes where id in (%s);" (clojure.string/join "," ids))]
    (sql/query db [cmd])))

(defn get-blocked-polygons-lonlat []
  (let [polygons (get-blocked-polygons-nodes (get-ways))] ;all the blocked-polygon-nodes
    (println polygons)
    (for [polygon polygons]
      (let [node-ids (:nodes polygon)
            nodes-infos (get-nodes node-ids)]
        (for [id node-ids]
          (first (filter #(= id (:id %)) nodes-infos)))))))

(defn get-multi-values [m p]
  (map #(get m %) p))

(defn get-lonlat-from-node [n]
  (get-multi-values n [:lon :lat]))

(defn get-geometry-polygon-from-lonlat [nodes]
  (format "POLYGON((%s))"
          (-> (reduce conj [] (map get-lonlat-from-node nodes))
              str
              (clojure.string/replace "[(" "")
              (clojure.string/replace ")]" "")
              (clojure.string/replace ") (" ","))))

(defn check-inside-any [point polygons]
  (some #(check-inside (get-geometry-point point) %) polygons))

(defn get-available-grid []
  (let [polygons (map get-geometry-polygon-from-lonlat (get-blocked-polygons-lonlat))
        points (map-indexed (fn [idx itm] (merge {:id idx} itm)) (get-grid (get-bondingbox)))]
    (filter #(not (check-inside-any % polygons)) points)))



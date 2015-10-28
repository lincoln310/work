(ns demo.logic.calc-loc
  (:import java.lang.Math)
  (:require[clojure.tools.logging :as log]
           [demo.db-layer.loc-records :as db]))


(defn get-distance [{:keys [id rssi p_plus p_mult] :as data}]
  (log/info "get-distance" id rssi)
  {:distance (* 1 (Math/pow 10 (/ (- (* -1 rssi) p_plus) p_mult)))})


(defn get-loc [distances]
  (log/info "get-loc" distances)
  (let [intersection (map #(db/get-intersection-circle-circle % %)
                          distances)
        polygon (reduce db/get-intersection-polygon-polygon
                        intersection)]
    (db/get-centroid polygon)))

(defn get-fspl [d f]
  (+ 32.44 (* 20 (Math/log10 (/ d 1000))) (* 20 (Math/log10 f))))

(defn get-fspl-our [d]
  (+ 40 (* 25 (Math/log10 d))))

(defn get-records [f]
  (line-seq (clojure.java.io/reader f)))

(defn splite-records [r]
  (clojure.string/split r #":"))

(defn take-last-3 [l]
  (take-last 3 l))

(defn splite-all [l]
  (map splite-records l))

(defn get-last-list [l]
  (filter #(> (count %) 0) (mapcat #(take-last 1 %) l)))

(defn get-valid-values [l]
  (filter #(> 0 %) (map #(int (Float/valueOf %)) (get-last-list l))))

(defn get-valid-values-from-file [f]
  (get-valid-values (splite-all (get-records f))))

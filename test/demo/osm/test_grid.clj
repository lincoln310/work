(ns demo.osm.test-grid
  (:use
   demo.db-layer.osm-grid
   ring.mock.request
   clojure.test))

(deftest test-check-inside-any
  (-> (check-inside "POINT(0 0)" ["POLYGON((-1 -1, -1 1, 1 1, 1 -1, -1 -1))"
                                  "POLYGON((1 1, 1 2, 2 2, 2 1, 1 1))"])
      true?))

(deftest test-check-inside
  (-> (check-inside "POINT(0 0)" "POLYGON((-1 -1, -1 1, 1 1, 1 -1, -1 -1))")
      true?))

(def grids (get-available-grid))

(deftest test-consistant
  (let [test-grid (take 10 grids)]
    (= (set (map :id test-grid))
       (set (map first (get-final-conns test-grid))))))

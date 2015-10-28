(ns demo.db-test
  (:require  [clojure.test :as t]
             [demo.logic.calc-loc :as calc]
             [demo.db-layer.loc-records :as db])
  (:use
   ring.mock.request
   clojure.test))

(defn circles []
  [{:id 1 :geo "POINT(-1 -1)", :distance 1.42 :gis-str "ST_Buffer('POINT(-1 -1)', 1.420000)" :valid 1, :p_plus 40, :p_mult 25}
              {:id 2 :geo "POINT(-1 1)", :distance 1.42 :gis-str "ST_Buffer('POINT(-1 1)', 1.420000)" :valid 1, :p_plus 40, :p_mult 25}
              {:id 3 :geo "POINT(1 1)", :distance 1.42 :gis-str "ST_Buffer('POINT(1 1)', 1.420000)" :valid 1, :p_plus 40, :p_mult 25}
              {:id 4 :geo "POINT(1 -1)", :distance 1.42 :gis-str "ST_Buffer('POINT(1 -1)', 1.420000)" :valid 1, :p_plus 40, :p_mult 25}])

(deftest test-get-circle
  "测试circle指令转换"
  (is (every? true? (map #(= (:gis-str %) (db/get-circle %)) (circles)))))

(deftest test-get-intersection-sql-cmd
  "测试计算交集sql函数生成"
  (is (= "select ST_AsText(ST_Intersection(ST_Buffer('POINT(-1 -1)', 1.420000)::geometry, 'POLYGON((0 0, 0 1, 1 1, 1 0))'::geometry)) as ret;"
         (db/get-intersection-sql-cmd
          (db/get-circle (get (circles) 0))
          (db/get-polygon "POLYGON((0 0, 0 1, 1 1, 1 0))")))))

(deftest test-intersection-circle-empty
  "测试空集"
  (is (= (db/get-intersection-circle-circle
           (conj (get (circles) 0) {:distance 1})
           (conj (get (circles) 2) {:distance 1}))
        "GEOMETRYCOLLECTION EMPTY")))

(deftest test-intersect-circles-empty
  "测试空集, get-loc是外部调用接口"
  (is (= (calc/get-loc (map #(conj % {:distance 1}) (circles)))
         "POINT EMPTY")))

(defn- get-circles-center []
  "POINT(-3.78454702600602e-16 0.000827726962104769)")

(deftest test-intersect-circles
  "测试计算交集中心点"
  (is (= (calc/get-loc (circles))
         (get-circles-center))))

;; (deftest test-centroid
  ;; (is (= (db/get-centroid (get-test-polygon))
         ;; '({:center POINT(-3.78454702600602e-16 0.000827726962104769)}))))

;;测试updates
(deftest test-update-geo2
  (db/update-record! [5] {:valid 1}))

(defn- init-db []
  (db/new-records! (circles)))

(defn- test-delete []
  (db/delete-record! [1,2,3,4,5]))

(defn- test-insert []
  (db/new-records! [{:id 5 :geo "POINT(5 5)" :valid 1 :p_plus 41 :p_mult 26}]))

(defn- test-get []
  (is (= [5] (map #(get % :id) (db/get-record [5])))))

(defn- test-update []
  (db/update-record! [5] {:geo "POINT(6 6)" :valid 0}))

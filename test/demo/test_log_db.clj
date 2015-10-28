(ns demo.test-log-db
  (:require  [clojure.test :as t])
  (:use
   ring.mock.request
   clojure.test
   demo.db-layer.req-records))

(deftest test-insert
  (insert! {:sid 1, :loc "POINT(0 0)", :time {:req-time 1, :res-time 2}, :each []}))



(ns demo.test-db-1102
  (:require  [clojure.test :as t])
  (:use [demo.util]))

(defn- data []
  "1102房间的设备信息"
  [
   {:last-part "3D:2E" :tag "00" :loc [0 0]}
   {:last-part "53:c6" :tag "01" :loc [7.791 0]}
   {:last-part "61:70" :tag "02" :loc [7.791 6.222]}
   {:last-part "53:CA" :tag "03" :loc [7.791 13.221]}
   {:last-part "3D:5c" :tag "04" :loc [7.791 20.376]}
   {:last-part "53:c2" :tag "05" :loc [0 20.376]}
   ])

(defn- base []
  "mac地址的头8位"
  "FC:D7:33:EE:")

(defn- insert-data- [data]
  (map #(insert-by-last-mac
         (base)
         (:last-part %)
         (get-in % [:loc 0])
         (get-in % [:loc 1])
         40 25
         (:tag %)) data))


(defn- test-insert-1102 []
  (insert-data- (data)))

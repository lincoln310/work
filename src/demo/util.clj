(ns demo.util
  (:use [demo.db-layer.loc-records :only [new-record!]]
        [demo.data-transfer :only [trans-dssi-to-id]] :reload))

;;; for input new record manully
(defn insert-by-mac [mac x y p m tag]
  (let [data (trans-dssi-to-id {:bssid mac})]
    (println data)
    (new-record! {:id (:id data),
                  :geo (format "POINT(%f %f)" (float x) (float y)),
                  :p_plus (float p)
                  :p_mult (float m)
                  :valid 1
                  :tag tag})))

(defn insert-by-last-mac [base mac x y p m tag]
  (println mac tag x y p m)
  (insert-by-mac (str base mac) x y p m tag))

(ns demo.web-test
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [demo.db-test :as db-test]
            [demo.web :refer :all])
  (:use clojure.test
        ring.mock.request))

(deftest testapp
  (println (app (request :get "/test"))))

(defn get-rssi-from-dis [dis]
  (+ 40 (* 25 (Math/log10 dis))))

(defn testparam [& n]
  (let [all-ret (map #(conj % {:bssid (get % :id)
                 :rssi (get-rssi-from-dis
                        (get % :distance))}) (db-test/circles))]
    all-ret))

(defn- url [] "/whereAmI")

(defn- do-request [param]
  (-> (app (request
        :get
        (url)
        {:fingerprint (json/write-str param)
         :content-type "application/json"}))
      (:body)
      (json/read-str)
      (clojure.walk/keywordize-keys)))


(deftest test-not-enought-params
  (let [param (testparam)
        cnt (count param)]
    (is (= false
           (:suc (do-request (take 0 param)))
           (:suc (do-request (take (- cnt 1) param)))))))

(deftest testjson
  (is (= true (:suc (do-request (testparam))))))

(defn get-response []
  (do-request (testparam)))

(defn put-new []
  (app (request
        :post
        "/newIndications"
        {:id 119 :x 2 :y 2 :p_plus 1 :p_mult 2 :valid 1 :tag "5"
         :content-type "application/json"})))

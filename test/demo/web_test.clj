(ns demo.web-test
  (:require [demo.web :refer :all]
            [clojure.data.json :as json])
  (:use
   ring.mock.request
   clojure.test))

(deftest testapp
  (println (app (request :get "/test"))))

(deftest testjson
  (let [pr {:figerprint [{:devId 1, :cssi 1}]}
        url "/whereAmI?"
        fp (query-string {} pr) 
        req (str url fp)]
      (println fp)
      (println req)
      (println (app (request
                     :get
                     url
                     pr
                     ;; :content-type "application/json"
                     )))))

(deftest addtest
  (is (= 4 (+ 1 3))))

(deftest failedtest
  (is (= 1 2)))

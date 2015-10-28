(ns demo.db-layer.db-define
  (:require [clojure.java.jdbc :as sql]))


(def db {:subprotocol "postgresql"
         :subname "location"
         :user "tsinghua"
         :password "tsinghua"})

(ns demo.db-layer.db-json
  (:require [clojure.data.json :as json]
            [clojure.java.jdbc :as jdbc]
            [clj-time.coerce :as tc]
            [clojure.tools.logging :as log])
  (:import org.postgresql.util.PGobject))

(defn value-to-json-pgobject [value]
  (log/info "value-to-json" value)
  (doto (PGobject.)
    (.setType "json")
    (.setValue (json/write-str value))))

(extend-protocol jdbc/ISQLValue
  clojure.lang.LazySeq
  (sql-value [value] (value-to-json-pgobject (force value)))

  ;; org.joda.time.DateTime
  ;; (sql-value [value] (value-to-json-pgobject (tc/to-sql-time value)))

  java.sql.Timestamp
  (sql-value [value] value)

  clojure.lang.IPersistentMap
  (sql-value [value] (value-to-json-pgobject value))

  clojure.lang.IPersistentVector
  (sql-value [value] (value-to-json-pgobject value)))


(extend-protocol jdbc/IResultSetReadColumn
  PGobject
  (result-set-read-column [pgobj metadata idx]
    (let [type  (.getType pgobj)
          value (.getValue pgobj)]
      (log/info type)
      (log/info value)
      (case type
        "json" (json/read-str value :key-fn keyword)
        "geometry" nil
        :else value))))

(ns demo.data-transfer
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [clojure.walk :refer [keywordize-keys]]))

(defn do-trans [l]
  "output:  [{devId 1, cssi 11.111},
  {devId 2, cssi 22.222},
  {devId 3, cssi 33.333},
  {devId 4, cssi 44.444}]"
  (log/info (type l))
  (log/info l)
  (keywordize-keys l))

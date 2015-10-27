(ns demo.web
  (:require [immutant.web :as web]
            [immutant.web.middleware :as mw]
            [clojure.pprint :refer (pprint)]
            [compojure.route :as route]
            [compojure.core :refer (ANY GET PUT POST defroutes routes wrap-routes)]
            [ring.util.response :refer (response redirect content-type)]
            [ring.middleware.params :refer (wrap-params)]
            [ring.middleware.format :refer [wrap-restful-format]]
            [liberator.core :refer [resource]]
            [environ.core :refer (env)]
            [demo.transactions :refer (dotx)]
            [demo.db-layer.loc-records :as loc]
            [demo.logic.calc-loc :as calc]
            [demo.data-transfer :as dt]
            [demo.data-processer :as dtproc]
            [clojure.tools.logging :as log]
            ))

(defn where-am-i
  "An example manipulating session state from
  https://github.com/ring-clojure/ring/wiki/Sessions
  figerPrint is a list with at least 4 maps in it, for example:
  [{devId 1, cssi 11.111},
  {devId 2, cssi 22.222},
  {devId 3, cssi 33.333},
  {devId 4, cssi 44.444}]"
  [req]
  (log/info req)
  (let [figerPrint (or (get-in req [:query-params "figerprint"] )
                       (get-in req [:params "figerprint"])
                       "[{a:1, b:2},{a:2, b:3}]")
        ret (dtproc/do-process (dt/do-trans figerPrint))]
    (println "figerPrint =>" figerPrint " your location: " ret)
    (response ret)))

(defn insert-indications [indications]
  (for [indication indications]
    (dotx loc/new-records! indication)))

(defn update-indications [indications]
  (for [indication indications]
    (dotx loc/update-record! indication)))

(defroutes app-routes
  (GET "/" {c :context} (redirect (str c "/index.html")))
  (GET "/test" [] "hi, you got me!")
  (route/resources "/"))

(defroutes rest-routes
  (GET "/whereAmI" [] where-am-i)
  (PUT "/newIndications" [] insert-indications)
  (POST "/updateIndications" [] update-indications))

(defn wrap-app-mdw
  [handler]
  (fn [req]
    (log/info "app mdw!")
    (log/info req)
    ((wrap-params handler) req)))

(defn wrap-rest-mdw
  [handler]
  (fn [req]
    (log/info "rest mdw!")
    (log/info req)
    ((-> handler
         (wrap-restful-format :formats [:json-kw])
         (wrap-params))
     req)))

(def app
  (routes
   (-> rest-routes
       (wrap-routes wrap-rest-mdw))
   (-> app-routes
       (wrap-routes wrap-app-mdw))
   (route/not-found "Not Found!")))

(defn -main [& {:as args}]
  (web/run-dmc
    (-> app 
        (mw/wrap-session {:timeout 20})
        #_(immutant.web.middleware/wrap-websocket
            {:on-open (fn [ch] (println "You opened a websocket!"))}))
    (merge {"host" (env :demo-web-host), "port" (env :demo-web-port)}
           args)))

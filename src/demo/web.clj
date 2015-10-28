(ns demo.web
  (:require [clojure.data.json :as json]
            [clojure.pprint :refer (pprint)]
            [clojure.tools.logging :as log]
            [clojure.walk :refer [keywordize-keys]]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [demo.data-processer :as dtproc]
            [demo.data-transfer :as dt]
            [demo.db-layer.loc-records :as loc]
            [demo.transactions :refer (dotx)]
            [demo.db-layer.req-records :as log-db]
            [environ.core :refer (env)]
            [immutant.web :as web]
            [immutant.web.middleware :as mw]
            [liberator.core :refer [resource]]
            [ring.middleware.keyword-params :refer (wrap-keyword-params)]
            [ring.middleware.params :refer (wrap-params)]
            [ring.util.response :refer (response redirect content-type)])
  (:use compojure.core
        ring.middleware.json))


(defn- deal-request [fingerPrint]
  (log/info "deal-request" fingerPrint)
  (-> {:each fingerPrint, :req-time (tc/to-sql-time (t/now))}
      (dt/do-trans)
      (dtproc/do-process)))

(defn- deal-response [status & resp]
  (log/info "resp" status resp)
  (let [ret (apply #(conj {:suc status} %) resp)
        ret (merge ret {:res-time (tc/to-sql-time (t/now))})]
    (log/info ret)
    (log-db/insert! ret)
    (response (json/write-str (select-keys ret [:suc :loc])))))

(defn where-am-i
  ^{:doc "http api, 用于提供查询位置的接口，需要提供3个及更多的有效参数来定位。
reqest params：
[
  {
    bssid: 11:22:33:44:55:66, ;;mac地址
    cssi: 123.32              ;;信号强度
  },
  {bssid: ..., cssi: ...},
  ...
]
reponse: {loc: 'POINT(x y)'}"
    }
  [req]
  (log/info req)
  (let [param-len-limit 4]
    (try 
        (let [fingerPrint (get-in req [:params :fingerprint])
              fingerPrint (json/read-str fingerPrint)
              fingerPrint (keywordize-keys fingerPrint)
              ;; fingerPrint (dt/convert-all-type fingerPrint)
              param-len (count fingerPrint)]
          (log/info fingerPrint)
          (log/info "param-len:" param-len)
          (if (< param-len param-len-limit)
            (deal-response false {:err (format "params length [%d] less than [%d]" param-len param-len-limit)})
            (deal-response true (deal-request fingerPrint))))
        (catch clojure.lang.ExceptionInfo e
            (deal-response false {:err (str e)})))))


(defn insert-indications
  ^{:doc "添加新的参考点"}
  [{indication :params}]
  (log/info indication)
  (-> indication
      dt/convert-all-type
      dt/trans-dssi-to-id
      (conj {:geo (format "POINT(%f %f)"
                          (Float/valueOf (:x indication))
                          (Float/valueOf (:y indication)))})
      (loc/new-record!)
      response))

(defn update-indications
  ^{:doc "更新参考点"}
  [{indications :params}]
  (for [indication indications]
    (dotx loc/update-record! indication)))

(defroutes app-routes
  ^{:doc "设置路由 该部分用于测试链接等用途"}
  (GET "/index" {c :context} (redirect (str c "/index.html")))
  (GET "/test" [] (fn [req] "hi, you got me again!"))
  (route/resources "/"))

(defroutes rest-routes
  ^{:doc "设置路由 定位使用的API"}
  (GET "/whereAmI" [] where-am-i)
  (POST "/newIndications" [] insert-indications)
  (POST "/updateIndications" [] update-indications))

(defn wrap-app-mdw
  ^{:doc "wrap the handler for app-routes"}
  [handler]
  (-> handler
      wrap-params))

(defn wrap-rest-mdw
  ^{:doc "wrap the handler for restful-routes:
    wrap-keyword-param: 把map参数的key从string转换成keyword.
    wrap-params: 把request中的query参数提取，并转换成map，加入:params中"}
  [handler]
  (-> handler
      wrap-keyword-params
      wrap-json-response
      wrap-params))

(def app
  ^{:doc "定义一个application，供服务器提供相应的服务，同时对不同的路由设置不同的handler wrapper。"}
  (routes
   (-> rest-routes
       (wrap-routes wrap-rest-mdw))
   (-> app-routes
       (wrap-routes wrap-app-mdw))
   (route/not-found "Not Found!")))

(defn -main [& {:as args}]
  ^{:doc "服务器启动函数
    :host '...'
    :port ..."}
  (web/run-dmc
    (-> app 
        (mw/wrap-session {:timeout 20}))
    (merge {"host" (env :demo-web-host), "port" (env :demo-web-port)}
           args)))

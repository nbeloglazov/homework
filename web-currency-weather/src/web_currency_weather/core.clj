(ns web-currency-weather.core
  (:use [ring.middleware resource keyword-params params content-type]
        [ring.util.response :only (content-type resource-response)])
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]
            [incanter.charts :as charts]))

(def weather-key "186492758e4eee07")

(def currency
  (memoize
   (fn [day]
     (let [{{:keys [EUR GBP] :as res} :rates}  (-> (format "http://openexchangerates.org/api/historical/2012-10-%02d.json" day)
                                                   io/reader
                                                   json/read-json)]
       (/ GBP EUR)))))

(def weather
  (memoize
   (fn [day]
     (let [observations (-> (format "http://api.wunderground.com/api/%s/history_201210%02d/q/RU/Moscow.json" weather-key day)
                            io/reader
                            json/read-json
                            (get-in [:history :observations]))]
       (->> (map :tempm observations)
            (map bigdec)
            (apply max))))))

(defn predict [vals]
  (let [[x0 x1 x2] (reverse (map second vals))
        last-day (first (last vals))
        dx (if (nil? x1) 0 (- x0 x1))
        ddx (if (nil? x2) 0 (+ x0 x2 (* -2 x1)))]
    [[(+ 1 last-day) (+ x0 dx)]
     [(+ 2 last-day) (+ x0 dx dx ddx)]]))

(defn datify-single [[day val]]
  [(format "%02d.10.12" day) val])

(defn get-data [fn from to]
  (let [datify #(map datify-single %)
        data (->> (range from (inc to))
                  (map #(vector % (fn %))))
        prediction (predict data)]
    {:data (datify data)
     :prediction (datify prediction)}))



(defn build-chart [series title y-label]
  (let [chart (charts/bar-chart
               (map first series)
               (map second series)
               :title title
               :x-label "Дата"
               :y-label y-label)
        min-val (apply min (map second series))
        max-val (apply max (map second series))
        delta (* 0.2 (- max-val min-val))]
    (-> chart
        (charts/set-y-range (- min-val delta) (+ max-val delta))
        (charts/set-stroke-color java.awt.Color/GREEN))))

(defn chart-to-stream [chart]
  (let [output (java.io.ByteArrayOutputStream.)]
    (javax.imageio.ImageIO/write (.createBufferedImage chart 800 600) "PNG" output)
    (java.io.ByteArrayInputStream. (.toByteArray output))))

(defmulti handle :uri)

(defn handler [request]
  (println request)
  (handle request))

(defn wrap-from-to-params [handler]
  (fn [{{:keys [from to]} :params :as request}]
    (if (nil? from)
      (handler request)
      (-> request
          (update-in [:params :from] #(Integer/parseInt %))
          (update-in [:params :to] #(Integer/parseInt %))
          handler))))

(def app
  (-> handler
      wrap-from-to-params
      wrap-keyword-params
      wrap-params
      (wrap-resource "static")
      wrap-content-type))

(defmethod handle "/" [request]
  (content-type (resource-response "static/index.html") "text/html"))

(defn response-chart [chart]
  (content-type
   {:status 200
    :body (chart-to-stream chart)}
   "image/png"))

(defmethod handle "/chart/weather" [{{:keys [from to]} :params}]
  (-> (get-data weather from to)
      :data
      (build-chart "Температура в Москве" "°C")
      response-chart))

(defmethod handle "/chart/currency" [{{:keys [from to]} :params}]
  (-> (get-data currency from to)
      :data
      (build-chart "Курс валют евро к фунту" "Курс")
      response-chart))

(defmethod handle "/data/weather.json" [{{:keys [from to]} :params}]
  {:body (json/json-str (get-data weather from to))})

(defmethod handle "/data/currency.json" [{{:keys [from to]} :params}]
  {:body (json/json-str (get-data currency from to))})


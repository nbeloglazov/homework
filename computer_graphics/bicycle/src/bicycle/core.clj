(ns bicycle.core
  (:require [seesaw [core :as sc]
             [graphics :as sg]])
  (:import javax.swing.Timer
           java.awt.event.ActionListener))

(def conf {:v 4
           :r-wheel-out 65
           :r-wheel-in 10
           :r-treadle-out 35
           :r-treadle-in 16
           :treadle-width 8
           :wheel-arms 10
           :delay 50
           :treadles-offset [-20 0]
           :wheel-offset 105
           :body [[-105 0 -20 0]
                  [-105 0 -45 -75]
                  [-20 0 -65 -130]
                  [-90 -130 -40 -130]
                  [-45 -75 60 -105]
                  [40 -155 105 0]
                  [-20 0 60 -105]
                  [30 -145 42 -157]
                  [-1500 67 1500 67]]})

(def angle-wheel (/ (:v conf) (:r-wheel-out conf) 1.0))
(def angle-treadle (/ (* angle-wheel (:r-wheel-in conf)) (:r-treadle-in conf)))

(def width 1200)
(def height 700)
(def base-point (atom [(/ width 2)
                       (/ height 2)]))
(def styles {:wheel (sg/style :stroke 2 :foreground "black")
             :inner-wheel (sg/style :stroke 2 :foreground "black" :background "black")
             :arm (sg/style :stroke 1 :foreground "black")
             :body (sg/style :stroke 4 :foreground "black")})
(declare frame)

(defn rotate-point [[x y] angle]
  [(- (* x (Math/cos angle)) (* y (Math/sin angle)))
   (+ (* x (Math/sin angle)) (* y (Math/cos angle)))])

(defn point [radius angle]
  (map #(* radius %)
       [(Math/cos angle)
        (Math/sin angle)]))

(defn create-wheel []
  (->> (iterate inc 0)
       (take (:wheel-arms conf))
       (map #(/ (* 2 Math/PI %) (:wheel-arms conf)))
       (map #(point (:r-wheel-out conf) %))))

(def wheel (atom (create-wheel)))
(def treadles (atom [(point (:r-treadle-out conf) 0.0)
                     (point (:r-treadle-out conf) Math/PI)]))

(defn line [g x1 y1 x2 y2 style]
  (sg/draw g (sg/line x1 y1 x2 y2)
           (styles style)))

(defn draw-wheel [g b-x b-y inner]
  (sg/draw g
           (sg/circle b-x b-y (:r-wheel-out conf))
           (:wheel styles))
  (when inner
    (sg/draw g
             (sg/circle b-x b-y (:r-wheel-in conf))
             (:inner-wheel styles)))
  (doseq [[x y] @wheel]
    (line g
          (+ x b-x) (+ y b-y)
          b-x b-y
          :arm)))

(defn draw-body [g]
  (let [[b-x b-y] @base-point]
    (doseq [[x1 y1 x2 y2] (:body conf)]
      (line g
            (+ x1 b-x) (+ y1 b-y)
            (+ x2 b-x) (+ y2 b-y)
            :body))))

(defn draw-treadles [g]
  (let [[b-x b-y] (map + @base-point (:treadles-offset conf))]
    (sg/draw g (sg/circle b-x b-y (:r-treadle-in conf))
             (:wheel styles))
    (doseq [p @treadles]
      (let [[x y] (map + p [b-x b-y])]
        (line g x y b-x b-y :arm)
        (line g
              (+ x (:treadle-width conf)) y
              (- x (:treadle-width conf)) y
              :wheel)))))

(defn draw [c g]
  (let [[x y] @base-point]
    (draw-wheel g (- x (:wheel-offset conf)) y true)
    (draw-wheel g (+ x (:wheel-offset conf)) y false))
  (draw-treadles g)
  (draw-body g))


(defn canvas []
  (sc/canvas :size [width :by height]
             :paint draw
             :id :canvas))

(defn create-frame []
  (sc/frame :title "Hello"
            :content (canvas)
            :on-close :dispose))

(defn start []
  (sc/invoke-later
   (-> frame
       sc/pack!
       sc/show!)))


(defn run-timer []
  (let [listener (proxy [ActionListener] []
                   (actionPerformed [e]
                     (swap! base-point (fn [[x y]]
                                         [(mod (+ x (:v conf)) width) y]))
                     (swap! wheel #(map (fn [p] (rotate-point p angle-wheel)) %))
                     (swap! treadles #(map (fn [p] (rotate-point p angle-treadle)) %))
                           (.repaint (sc/select frame [:#canvas]))))]
    (def timer (Timer. (:delay conf) listener))
    (.start timer)))

(defn restart []
  (def frame (create-frame))
  (run-timer)
  (start))

(defn stop-timer []
  (.stop timer))

(defn -main [& args]
  (start))

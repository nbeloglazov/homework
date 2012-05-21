(ns delaunay.gui
  (:require [seesaw [core :as sc]
                    [graphics :as sg]]
            [delaunay [core :as core]])
  (:import [java.awt.event MouseEvent]))

(def width 1200)
(def height 700)
(def radius 3)
(def points (atom []))
(def triangles (atom []))
(declare frame)

(def styles {:point (sg/style :stroke 1 :background "red")
             :line (sg/style :stroke 1 :foreground "blue")})

(defn rand-style []
  (->> (repeatedly 3 #(rand-int 128))
       (map #(+ 127 %))
       (apply seesaw.color/color)
       (sg/style :background)))

(defn rand-point []
  [(+ (rand-int 800) 200)
   (+ (rand-int 500) 100)])

(defn draw-points [g]
  (doseq [[x y] @points]
    (sg/draw g
             (sg/circle x y radius)
             (styles :point))))

(defn draw-triangles [g]
  (doseq [triangle @triangles]
    (sg/draw g
             (apply sg/polygon triangle)
             (rand-style))))

(defn draw [c g]
  (draw-triangles g)
  (draw-points g))

(defn re-triangulate []
  (if (> (count @points) 2)
    (reset! triangles (core/triangulation @points))))


(defn left-button [e]
  (= (.getButton e) (MouseEvent/BUTTON1)))

(defn middle-button [e]
  (= (.getButton e) (MouseEvent/BUTTON2)))

(defn right-button [e]
  (= (.getButton e) (MouseEvent/BUTTON3)))

(defn add-point [event]
  (swap! points conj [(.getX event)
                      (.getY event)]))


(defn mouse-clicked [e]
  (cond (left-button e)
        (do (add-point e)
            (re-triangulate))
        (right-button e)
        (do (reset! points [] #_[[413 522] [519 155] [623 492] [869 514] [661 355]])
            (reset! triangles [])
            (re-triangulate))
        (middle-button e)
        (swap! triangles core/flip-all))
  (.repaint (sc/select frame [:#canvas])))

(defn canvas []
  (sc/canvas :size [width :by height]
             :paint draw
             :id :canvas
             :listen [:mouse-pressed mouse-clicked]))

(defn create-frame []
  (sc/frame :title "Hello"
            :content (canvas)
            :on-close :dispose))


(defn start []
  (sc/invoke-later
   (-> frame
       sc/pack!
       sc/show!)))

(defn restart []
  (def frame (create-frame))
  #_(reset! points (repeatedly 10000 rand-point))
  (start))

(defn -main [& args]
  (start))


(ns convex-hull.gui
  (:require [seesaw [core :as sc]
                    [graphics :as sg]]
            [convex-hull [core :as core]])
  (:import [java.awt.event MouseEvent]))

(def width 1200)
(def height 700)
(def radius 3)
(def polygon (atom []))
(def points (atom []))
(declare frame)

(def styles {:point (sg/style :stroke 1 :background "red")
             :line (sg/style :stroke 1 :foreground "blue")})

(defn rand-point []
  [(+ (rand-int 800) 200)
   (+ (rand-int 500) 100)])

(defn draw-polygon [g]
  (sg/draw g
           (apply sg/polygon @polygon)
           (styles :line)))

(defn draw-points [g]
  (doseq [[x y] @points]
    (sg/draw g
             (sg/circle x y radius)
             (styles :point))))

(defn draw [c g]
  (draw-polygon g)
  (draw-points g))


(defn left-button [e]
  (= (.getButton e) (MouseEvent/BUTTON1)))

(defn right-button [e]
  (= (.getButton e) (MouseEvent/BUTTON3)))

(defn add-point [event]
  (swap! points conj [(.getX event)
                      (.getY event)]))

(defn recalculate-hull []
  (reset! polygon (core/convex-hull @points)))

(defn mouse-clicked [e]
  (when (left-button e)
    (add-point e)
    (recalculate-hull)
    (.repaint (sc/select frame [:#canvas]))))

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
  (reset! points (repeatedly 10000 rand-point))
  (recalculate-hull)
  (start))

(defn -main [& args]
  (start))


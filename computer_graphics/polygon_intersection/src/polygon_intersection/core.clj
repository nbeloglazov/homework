(ns polygon-intersection.core
  (:require [seesaw [core :as sc]
                    [graphics :as sg]]
            [polygon-intersection [intersection :as intersection]])
  (:import [java.awt.event MouseEvent KeyEvent]))

(def width 1200)
(def height 700)
(def polygon (atom []))
(def view (atom []))
(def inters (atom []))

(declare frame)

(def styles {polygon (sg/style :stroke 1 :foreground "black")
             view (sg/style :stroke 1 :foreground "blue")
             inters (sg/style :stroke 3 :foreground "red" :background "red")})

(defn draw-polygon [g polygon]
  (println @polygon)
  (sg/draw g
           (apply sg/polygon @polygon)
           (styles polygon)))

(defn draw [c g]
  (doseq [p [polygon view inters]]
    (draw-polygon g p)))


(defn left-button [e]
  (= (.getButton e) (MouseEvent/BUTTON1)))

(defn right-button [e]
  (= (.getButton e) (MouseEvent/BUTTON3)))

(defn add-point [polygon event]
  (swap! polygon conj [(.getX event)
                       (.getY event)]))

(defn mouse-clicked [e]
  (cond (and (.isShiftDown e) (left-button e))
        (reset! inters (intersection/polygon-polygon @view 1 @polygon))
        (and (.isShiftDown e) (right-button e))
        (do (reset! polygon [])
            (reset! view [])
            (reset! inters []))
        (left-button e)
        (add-point polygon e)
        (right-button e)
        (add-point view e))
  (.repaint (sc/select frame [:#canvas])))

(defn canvas []
  (sc/canvas :size [width :by height]
             :paint draw
             :id :canvas
             :listen [:mouse-clicked mouse-clicked]))

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
  (start))

(defn -main [& args]
  (start))


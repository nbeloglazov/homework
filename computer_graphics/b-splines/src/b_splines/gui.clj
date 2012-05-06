(ns b-splines.gui
    (:require [seesaw [core :as sc]
                    [graphics :as sg]]
              [b-splines [core :as core]])
  (:import [java.awt.event MouseEvent KeyEvent]))

(def width 1200)
(def height 700)
(def radius 3)
(def step 0.005)
(def points (atom []))
(def degree (atom 3))
(declare frame)

(def styles {:point (sg/style :stroke 1 :background "red")
             :line (sg/style :stroke 1 :foreground "blue")})

(defn draw-points [g]
  (doseq [[x y] @points]
    (sg/draw g
             (sg/circle x y radius)
             (styles :point))))

(defn draw-spline [g]
  (when (>  (count @points) @degree)
    (let [spline (core/get-b-spline @points @degree)
          lines (->> (range 0 1 step)
                     (mapv spline)
                     (#(conj % (last @points)))
                     (partition 2 1)
                     (map flatten))]
      (doseq [l lines]
        (sg/draw g
                 (apply sg/line l)
                 (styles :line))))))

(defn draw [c g]
  (draw-spline g)
  (draw-points g))


(defn left-button [e]
  (= (.getButton e) (MouseEvent/BUTTON1)))

(defn right-button [e]
  (= (.getButton e) (MouseEvent/BUTTON3)))

(defn add-point [event]
  (swap! points conj [(.getX event)
                      (.getY event)]))

(defn mouse-clicked [e]
  (if (left-button e)
    (add-point e)
    (reset! points []))
  (.repaint (sc/select frame [:#canvas])))

(def degrees {KeyEvent/VK_UP 1
              KeyEvent/VK_DOWN -1})

(defn key-pressed [e]
  (when (contains? degrees (.getKeyCode e))
    (swap! degree + (degrees (.getKeyCode e)))
    (.repaint (sc/select frame [:#canvas]))))


(defn canvas []
  (sc/canvas :size [width :by height]
             :paint draw
             :id :canvas
             :listen [:mouse-pressed mouse-clicked
                      :key-pressed key-pressed]))

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
  (reset! points [])
  (.setFocusable (sc/select frame [:#canvas]) true)
  (start))

(defn -main [& args]
  (start))


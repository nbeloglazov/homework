(ns ortogonal-intersection.core
  (:require [seesaw [core :as sc]
                    [graphics :as sg]]
            [ortogonal-intersection [intersection :as intersection]])
  (:import [java.awt.event MouseEvent KeyEvent]))

(def width 1200)
(def height 700)
(def max-length 30)
(def n 5000)
(def points (atom #{}))
(def radius 2)
(declare segments)
(declare frame)
(def segment-style (sg/style :stroke 1 :foreground "black"))
(def point-style (sg/style :foreground "red" :background "red"))
(def phase (atom :nothing))

(defn rand-pair [max]
  (let [a (rand-int max)]
    [a (+ a (rand-int max-length))]))

(defn generate-vertical []
  {:type :vertical
   :x (rand-int width)
   :y (rand-pair height)})

(defn generate-horizontal []
  {:type :horizontal
   :y (rand-int height)
   :x (rand-pair width)})



(defn generate-segments [n]
  (repeatedly n #(if (even? (rand-int 2))
                   (generate-vertical)
                   (generate-horizontal))))

(defn between? [x [a b]]
  (<= a x b))

(defn slow-intersections [segments]
  (let [vertical (filter #(= :vertical (:type %)) segments)
        horizontal (filter #(= :horizontal (:type %)) segments)]
    (doseq [v vertical h horizontal]
      (when (and (between? (:x v) (:x h))
                 (between? (:y h) (:y v)))
        (swap! points conj [(:x v) (:y h)])))))

(defn draw-segments [g]
  (doseq [{:keys [type x y]} segments]
    (sg/draw g
             (apply sg/line
                    (if (= :vertical type)
                      [x (first y) x (last y)]
                      [(first x) y (last x) y]))
             segment-style)))

(defn draw-point [g [x y]]
  (sg/draw g
           (sg/circle x y radius)
           point-style))

(defn draw-points [g]
  (doseq [point @points]
    (draw-point g point)))

(defn draw [c g]
  (if (= :searching @phase)
    (draw-point g (last @points))
    (do (draw-segments g)
        (draw-points g))))


(defn point-found [point]
  (swap! points conj point))

(defn left-button [e]
  (= (.getButton e) (MouseEvent/BUTTON1)))

(defn right-button [e]
  (= (.getButton e) (MouseEvent/BUTTON3)))

(defn mouse-clicked [e]
  (cond (left-button e)
        (do (reset! points #{})
            (time (if (.isShiftDown e)
                    (intersection/find-intersections segments point-found)
                    (slow-intersections segments)))
            (.repaint (sc/select frame [:#canvas])))
        (right-button e)
        (do (reset! points #{})
            (.repaint (sc/select frame [:#canvas])))))

(defn canvas []
  (sc/canvas :size [width :by height]
             :paint draw
             :id :canvas
             :listen [:mouse-clicked mouse-clicked]))


(defn create-frame []
  (sc/frame :title "Hello"
            :content (sc/border-panel
                      :north (sc/label :id :status
                                       :text "hello")
                      :center (canvas))
            :on-close :dispose))

(def frame (create-frame))

(defn start []
  (sc/invoke-later
   (-> frame
       sc/pack!
       sc/show!)))

(defn restart []
  (def frame (create-frame))
  (def segments (generate-segments n))
  (reset! points #{})
  ;(time (intersection/find-intersections segments #(swap! points conj %)))
  (start))

(defn -main [& args]
  (start))

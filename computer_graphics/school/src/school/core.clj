(ns school.core
  (:use [clojure.set])
  (:require [seesaw [core :as sc]
                    [graphics :as sg]]
            [school [kdtree :as kdtree]
                    [quadratic-tree :as qtree]])
  (:import [java.awt.event MouseEvent KeyEvent]))

(def width 1200)
(def height 1000)
(def n 100000)
(def radius 3)
(def points
  (repeatedly n
              (fn [] [(rand-int width)
                      (rand-int height)])))

(def tree (kdtree/build-tree points 0))
(def qdtree (qtree/build-tree points))

(def selection (atom nil))
(def state (atom :not-selected))
(def init-position (atom nil))
(def last-search (atom 0))
(def repaint-rect (atom nil))
(def selected-points-old (atom nil))
(def search-type (atom :slow))

(def not-selected-style (sg/style :foreground "blue" :background "blue"))
(def selected-style (sg/style :foreground "red" :background "red"))
(def selection-style (sg/style :foreground "black"))

(declare frame)

(defn slow-selected-points [[x y width height]]
  (set (filter (fn [[px py]] (and (<= x px (+ x width))
                                  (<= y py (+ y height))))
               points)))

(defn fast-selected-points [rect]
  (set (kdtree/find-points tree rect)))

(defn q-fast-selected-points [rect]
  (set (qtree/find-points qdtree rect)))

(def search-methods {:fast fast-selected-points
                     :slow slow-selected-points
                     :q-fast q-fast-selected-points})

(defn selected-points [rect]
  ((search-methods @search-type) rect))

(defn status []
  (format "Points: %d Search type %s Last search %d" n @search-type @last-search))

(defn update-status [& params]
  (sc/config! (sc/select frame [:#status])
              :text (status)))

(defn repaint-canvas
  ([] (.repaint (sc/select frame [:#canvas])))
  ([x y width height] (.repaint (sc/select frame [:#canvas]) x y width height)))


(defn left-button [e]
  (= (.getButton e) (MouseEvent/BUTTON1)))

(defn right-button [e]
  (= (.getButton e) (MouseEvent/BUTTON3)))

(defn mouse-point [e]
  (let [p (.getPoint e)]
    [(.x p) (.y p)]))

(defn timer [atm fn & args]
  (let [start (System/currentTimeMillis)
        res (apply fn args)
        end (System/currentTimeMillis)]
    (reset! atm (- end start))
    (update-status)
    res))


(defn mouse-clicked [e]
  (when (and (right-button e)
           (= @state :selected))
    (reset! state :not-selected)
    (reset! selection nil)
    (repaint-canvas)))

(defn inside? [[x y width height] [px py]]
  (and (<= x px (+ x width))
       (<= y py (+ y height))))

(defn calc-repaint-rect [[ox oy width height] dx dy]
  [(- (Math/min ox (+ ox dx)) radius)
   (- (Math/min oy (+ oy dy)) radius)
   (+ width (Math/abs dx) radius radius)
   (+ height (Math/abs dy) radius radius)])

(defn mouse-dragged [e]
  (let [[mx my] (mouse-point e)
        [x y] @init-position]
    (cond (= @state :selecting)
          (do (reset! selection [(min mx x) (min my y) (Math/abs (- x mx)) (Math/abs (- y my))])
              (repaint-canvas))
          (= @state :dragging)
          (let [dx (- mx x)
                dy (- my y)]
            (reset! search-type (cond (.isShiftDown e) :fast
                                      (.isAltDown e) :q-fast
                                      :else :slow))
            (update-status)
            (reset! repaint-rect (calc-repaint-rect @selection dx dy))
            (swap! selection (fn [[sx sy width height]]
                               [(+ sx dx) (+ sy dy) width height]))
            (reset! init-position (mouse-point e))
            (apply repaint-canvas @repaint-rect)))))

(defn mouse-pressed [e]
  (when (left-button e)
    (cond (= @state :not-selected)
          (do (reset! state :selecting)
              (reset! init-position (mouse-point e)))
          (and (= @state :selected)
               (inside? @selection (mouse-point e)))
          (do (reset! state :dragging)
              (reset! init-position (mouse-point e))))))

(defn mouse-released [e]
  (when (left-button e)
    (cond (= @state :selecting)
          (do (reset! state :selected)
              (repaint-canvas))
          (= @state :dragging)
          (do (reset! state :selected)
              (repaint-canvas)))))

(defn enlarge [[x y width height] val]
  [ (- x val)
    (- y val)
    (+ width val val)
    (+ height val val)])

(defn draw-all-points [g]
  (let [sel-ps (if (nil? @selection) #{}
                   (selected-points @selection))]
    (doseq [[x y] points]
      (sg/draw g (sg/rect (- x radius) (- y radius) radius)
               (if (sel-ps [x y])
                 selected-style
                 not-selected-style)))))

(defn draw-selected-points [g]
  (let [sel-ps (timer last-search selected-points @selection)
        all-inside (selected-points (enlarge @repaint-rect radius))]
    (doseq [[x y] sel-ps]
      (sg/draw g (sg/rect (- x radius) (- y radius) radius)
               selected-style))
    (doseq [[x y] (difference  all-inside sel-ps)]
      (sg/draw g (sg/rect (- x radius) (- y radius) radius)
               not-selected-style))
    (reset! selected-points-old sel-ps)))



(defn draw-selection [g]
  (when-not (nil? @selection)
    (sg/draw g (apply sg/rect @selection)
             selection-style)))

(defn draw-all [c g]
  (if (= @state :dragging)
    (draw-selected-points g)
    (draw-all-points g))
  #_(draw-selection g))


(defn canvas []
  (sc/canvas :size [width :by height]
             :paint draw-all
             :id :canvas
             :listen [:mouse-clicked mouse-clicked
                      :mouse-pressed mouse-pressed
                      :mouse-released mouse-released
                      :mouse-dragged mouse-dragged]))


(defn create-frame []
  (sc/frame :title "Hello"
            :content (sc/border-panel
                      :north (sc/label :id :status
                                       :text (status))
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
  (start))

(defn -main [& args]
  (start))

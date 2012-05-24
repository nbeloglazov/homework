(ns kunz.gui
  (:require [seesaw [core :as sc]
                    [graphics :as sg]]
            [kunz [core :as core]
                  [projection :as projection]])
  (:import [java.awt.event MouseEvent KeyEvent]))


(def objects (atom [{:points [[20 1 -1]
                              [20 1 1]
                              [20 -1 1]
                              [20 -1 -1]
                              [22 1 -1]
                              [22 1 1]
                              [22 -1 1]
                              [22 -1 -1]]
                     :faces [[0 1 2 3 0]
                             [4 5 6 7 4]]}]))

(def width 750)
(def height 750)
(def angle-sp 0.003)
(def speed 0.1)
(def radius 2)
(def n 20)
(def dist (/ 1.0 (dec n)))

(defn p0 [t] [t 0 (Math/sin (* 2 t))])

(defn p1 [t] [t 1 (Math/cos (* 2 t))])

(def config (atom
             {:VUP [0 0 1]
              :VPN [1 0 0]
              :VPN-y [0 1 0]
              :VPN-z [0 0 1]
              :u [-0.5 0.5]
              :v [-0.5 0.5]
              :COP [0 0 0]
              :TRG [2 0 0]
              :focus 1
              :width width
              :height height}))

(defn build-plane []
  (let [interpolator (core/interpolate p0 p1)]
    {:points (for [s (range 0 1.0001 dist)
                   t (range 0 1.0001 dist)]
               (interpolator s t))
     :faces (concat (partition n (range (* n n)))
                    (->> (* n n) range (group-by #(rem % n)) vals))}))

(reset! objects [(build-plane)])

(def cursor (let [img (java.awt.image.BufferedImage. 16, 16, java.awt.image.BufferedImage/TYPE_INT_ARGB)]
              (.createCustomCursor (java.awt.Toolkit/getDefaultToolkit) img (java.awt.Point. 0 0) "blank cursor")))

(declare frame)
(def prev-pos (atom nil))

(defn rotate [crds]
  (let [old @prev-pos]
    (reset! prev-pos crds)
    (when-not (nil? old)
      (let [[dx dy] (map - crds old)
            a-z (* (- angle-sp) dx)
            a-y (* (- angle-sp) dy)]
        (swap! config projection/rotate a-y a-z)))))



(def styles {:point (sg/style :stroke 1 :background "red")
             :line (sg/style :stroke 1 :foreground "blue")
             :border (sg/style :stroke 3 :foreground "black")})

(defn transform-faces [{:keys [points faces]}]
  (let [transformed (mapv #(projection/transform % @config) points)]
    (map #(map transformed %) faces)))

(defn get-all-faces []
  (->> (map transform-faces @objects)
       (apply concat)
       (filter (fn [el] (every? #(not (nil? %)) el)))))

(defn draw-border [g]
  (sg/draw g
           (sg/rect 2 2 (- width 3) (- height 3))
           (styles :border)))

(defn draw-face [g face]
  (doseq [[[x0 y0] [x1 y1]] (partition 2 1 face)]
    (sg/draw g
             (sg/line x0 y0 x1 y1)
             (styles :line))))

(defn draw-faces [g faces]
  (doseq [face faces]
    (draw-face g face)
    #_(sg/draw g
             (apply sg/polygon face)
             (styles :line))))

(defn draw [c g]
  (draw-faces g (get-all-faces))
  (draw-border g))


(defn left-button [e]
  (= (.getButton e) (MouseEvent/BUTTON1)))

(defn right-button [e]
  (= (.getButton e) (MouseEvent/BUTTON3)))

(defn recalculate []
  (swap! config #(-> % projection/calc-parameters projection/calc-matrix)))

(defn mouse-dragged [e]
  (rotate [(.getX e) (.getY e)])
  (recalculate)
  (.repaint (sc/select frame [:#canvas])))

(defn mouse-wheel [e]
  (let [dist (* (.getWheelRotation e) 0.1 -1)
        move (fn [{:keys [focus] :as config}]
               (assoc config
                 :focus (+ focus dist)))]
    (swap! config move))
  (recalculate)
  (.repaint (sc/select frame [:#canvas])))

(defn mouse-pressed [e]
  (reset! prev-pos [(.getX e) (.getY e)]))

(defn move [{:keys [COP] :as config} dist dir]
  (assoc config
    :COP (mapv + COP (projection/mult dist (config dir)))))

(def keymap
  {KeyEvent/VK_W [1 :VPN]
   KeyEvent/VK_S [-1 :VPN]
   KeyEvent/VK_A [-1 :VPN-y]
   KeyEvent/VK_D [1 :VPN-y]
   KeyEvent/VK_Q [1 :VPN-z]
   KeyEvent/VK_E [-1 :VPN-z]})

(defn key-pressed [e]
  (when (contains? keymap (.getKeyCode e))
    (let [[sign dir] (keymap (.getKeyCode e))
          mult (if (.isShiftDown e) 10 1)]
      (swap! config move (* sign speed mult) dir)
      (recalculate)
      (.repaint (sc/select frame [:#canvas])))))



(defn canvas []
  (let [canvas (sc/canvas :size [width :by height]
                          :paint draw
                          :id :canvas
                          :listen [:mouse-dragged mouse-dragged
                                   :mouse-pressed mouse-pressed
                                   :key-pressed key-pressed
                                   :mouse-wheel-moved mouse-wheel])]
    (.setCursor canvas cursor)
    canvas))

(defn create-frame []
  (sc/frame :title "Hello"
            :content (canvas)
            :on-close :dispose))

(defn run-timer []
  (let [listener (proxy [java.awt.event.ActionListener] []
                   (actionPerformed [e]
                     (.repaint (sc/select frame [:#canvas]))))]
    (def timer (javax.swing.Timer. 1000 listener))
    (.start timer)))

(defn start []
  (sc/invoke-later
   (-> frame
       sc/pack!
       sc/show!)))

(defn restart []
  (def frame (create-frame))
  (.setFocusable (sc/select frame [:#canvas]) true)
  #_(run-timer)
  (recalculate)
  (start))

(defn stop-timer []
  (.stop timer))


(defn -main [& args]
  (restart))


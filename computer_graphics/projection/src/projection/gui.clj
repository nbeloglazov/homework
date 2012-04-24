(ns projection.gui
  (:require [seesaw [core :as sc]
                    [graphics :as sg]]
            [projection [core :as core]
                        [digits :as digits]])
  (:import [java.awt.event MouseEvent KeyEvent]))


#_(def objects [{:points [[5 1 -1]
                        [5 1 1]
                        [5 -1 1]
                        [5 -1 -1]
                        [3 0 0]]
               :faces [[0 1 2 3 0]
                       [0 1 4 0]
                       [1 4 2 1]
                       [2 3 4 2]
                       [0 4 3 0]]}])

(def objects (atom []))

(def width 750)
(def height 750)
(def angle-sp 0.003)
(def speed 0.1)
(def radius 2)

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
        (swap! config core/rotate a-y a-z)))))



(def styles {:point (sg/style :stroke 1 :background "red")
             :line (sg/style :stroke 1 :foreground "blue")
             :border (sg/style :stroke 3 :foreground "black")})

(defn draw-points [g points]
  (doseq [[x y] (remove nil? points)]
    (sg/draw g
             (sg/circle x y radius)
             (styles :point))))

(defn draw-face [g points face]
  (doseq [[a b] (partition 2 1 face)]
    (when (every? #(not (nil? (points %))) [a b])
      (let [[x1 y1] (points a)
            [x2 y2] (points b)]
        (sg/draw g
                 (sg/line x1 y1 x2 y2)
                 (styles :line))))))

(defn draw-object [g {:keys [points faces]}]
  (let [transformed (mapv #(core/transform % @config) points)]
    (draw-points g transformed)
    (doseq [face faces] (draw-face g transformed face))))

(defn draw-border [g]
  (sg/draw g
           (sg/rect 2 2 (- width 3) (- height 3))
           (styles :border)))

(defn draw [c g]
  (doseq [object @objects]
    (draw-object g object))
  (draw-border g))


(defn left-button [e]
  (= (.getButton e) (MouseEvent/BUTTON1)))

(defn right-button [e]
  (= (.getButton e) (MouseEvent/BUTTON3)))

(defn recalculate []
  (swap! config #(-> % core/calc-parameters core/calc-matrix)))

(defn mouse-dragged [e]
  (rotate [(.getX e) (.getY e)])
  (recalculate)
  (.repaint (sc/select frame [:#canvas])))

(defn mouse-wheel [e]
  (let [dist (* (.getWheelRotation e) speed -1)
        move (fn [{:keys [COP VPN] :as config}]
               (assoc config
                 :COP (mapv + COP (core/mult dist VPN))))]
    (swap! config move))
  (recalculate)
  (.repaint (sc/select frame [:#canvas])))

(defn mouse-pressed [e]
  (reset! prev-pos [(.getX e) (.getY e)]))

(defn move [{:keys [COP] :as config} dist dir]
  (assoc config
    :COP (mapv + COP (core/mult dist (config dir)))))

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
                     (reset! objects (digits/get-objects (.getSeconds (java.util.Date.))))
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
  (run-timer)
  (recalculate)
  (start))

(defn stop-timer []
  (.stop timer))


(defn -main [& args]
  (start))


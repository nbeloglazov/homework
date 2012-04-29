(ns projection.warnock
  (:import projection.Utils)
  (:require [seesaw [core :as sc]
                    [graphics :as sg]]))

(defn line-segment-intersection [[lx0 ly0 :as l0]
                                 [lx1 ly1 :as l1]
                                 [sx0 sy0 :as s0]
                                 [sx1 sy1 :as s1]]
  (let [ldx (- lx1 lx0)
        ldy (- ly1 ly0)
        sdx (- sx1 sx0)
        sdy (- sy1 sy0)
        t (/ (- (* ldx (- sy0 ly0))
                (* ldy (- sx0 lx0)))
             (Utils/vecMult l0 l1 s0 s1)
             -1.0)]
    [(+ sx0 (* t sdx))
     (+ sy0 (* t sdy))]))

(defn visible? [lp0 lp1 point sign]
  (>= (* (Utils/vecMult lp0 lp1 lp0 point)
         sign)
      0))

(defn line-segment [line segment sign]
  (let [lp0 (first line)
        lp1 (second line)
        sp0 (first segment)
        sp1 (second segment)
        p0-visible? (visible? lp0 lp1 sp0 sign)
        p1-visible? (visible? lp0 lp1 sp1 sign)]
    (cond (and p0-visible? p1-visible?)
          [sp1]
          (and p0-visible? (not p1-visible?))
          [(line-segment-intersection lp0 lp1 sp0 sp1)]
          (and (not p0-visible?) p1-visible?)
          [(line-segment-intersection lp0 lp1 sp0 sp1) sp1]
          :default
          [])))

(defn line-polygon [line sign polygon]
  (->> (cons (last polygon) polygon)
       (partition 2 1)
       (map #(line-segment line % sign))
       (apply concat)))

(defn polygon-polygon [view sign polygon]
  (->> (partition 2 1 view)
       (reduce #(line-polygon %2 sign %1) polygon)))


(defn mid [a b]
  (/ (+ a b) 2.0))

(defn split [[x1 y1 x2 y2]]
  (let [xm (mid x1 x2)
        ym (mid y1 y2)]
    [[x1 y1 xm ym]
     [xm y1 x2 ym]
     [x1 ym xm y2]
     [xm ym x2 y2]]))

(defn view-to-polygon [[x1 y1 x2 y2]]
  (with-meta [[x1 y1] [x1 y2] [x2 y2] [x2 y1] [x1 y1]]
    {:params [x1 y1 x2 y2]}))


(defn plane [[x1 y1 z1] [x2 y2 z2] [x3 y3 z3]]
  (let [A (+ (* y1 (- z2 z3))
             (* y2 (- z3 z1))
             (* y3 (- z1 z2)))
        B (+ (* z1 (- x2 x3))
             (* z2 (- x3 x1))
             (* z3 (- x1 x2)))
        C (+ (* x1 (- y2 y3))
             (* x2 (- y3 y1))
             (* x3 (- y1 y2)))
        D (+ (* x1 (- (* y2 z3) (* y3 z2)))
             (* x2 (- (* y3 z1) (* y1 z3)))
             (* x3 (- (* y1 z2) (* y2 z1))))]
    (when (zero? C)
      (println [x1 y1 z1] [x2 y2 z2] [z3 y3 z3])
      (println A B C D))
    (fn [x y]
      (/ (- D (+ (* A x) (* B y)))
         C))))

(defn outside? [[x y] view]
  (let [[xmn ymn xmx ymx] (:params (meta view))]
    (and (or (< x (+ xmn 1e-5))
             (> x (- xmx 1e-5)))
         (or (< y (+ ymn 1e-5))
             (> y (- ymx 1e-5))))))

(defn outer? [face view]
  (every? #(outside? % view) face))

(defn with-plane [face]
  (let [pl (apply plane (take 3 face))
        meta (or (meta face) {})
        face (map #(->> (take 2 %) (map double) vec) face)]
    (with-meta face (assoc meta :plane pl))))

(defn attach-planes [faces]
  (map with-plane faces))

(defn find-closest-to-point [faces [x y]]
  (apply min-key #((:plane (meta %)) x y) faces))

(defn find-closest [faces view]
  (let [closest (distinct (map #(find-closest-to-point faces %) view))]
    (if (= 1 (count closest))
      (first closest)
      nil)))

(defn intersect [face view]
  (let [res #_(polygon-polygon view -1.0 (rest face))
        (Utils/polygonPolygon view -1.0 (rest face))]
    (if (empty? res)
      []
      (cons (last res) res))))

(defn stop-or-split [g faces view]
  (cond (empty? faces)
        nil
        (= 1 (count faces))
        (first faces)
        :default
        (let [closest (find-closest faces view)]
          (if (and (not (nil? closest))
                   (:outer (meta closest)))
            closest
            :split))))

(def style (sg/style :stroke 1 :foreground "cyan" :background "cyan"))

(defn draw-face [g face]
  (when-not (nil? face)
    (sg/draw g
             (apply sg/polygon face)
             (or (:style (meta face))
                 style))))

(defn intersect-all [faces view]
  (->> (map #(if (:outer (meta %))
               %
               (with-meta (intersect % view) (meta %)))
            faces)
       (remove empty?)
       (map #(if (or (:outer (meta %))
                     (outer? % view))
               (with-meta % (assoc (meta %) :outer true))
               %))))

(defn draw [g ffaces view]
  (if (> (- (view 2) (view 0)) 1)
    (let [view-pol (view-to-polygon view)
          faces (intersect-all ffaces view-pol)
          res (stop-or-split g faces view-pol)]
      (if (= res :split)
        (doseq [sub-view (split view)]
          (draw g faces sub-view))
        (when-not (nil? res)
          (draw-face g res))))
    (sg/draw g
             (sg/rect (first view) (second view) 1)
             (or (:style (meta (first ffaces)))
                 style))))



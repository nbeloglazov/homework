(ns polygon-intersection.intersection)

(defn vec-mult [[x0 y0] [x1 y1] [x2 y2] [x3 y3]]
  (let [dx1 (- x1 x0)
        dy1 (- y1 y0)
        dx2 (- x3 x2)
        dy2 (- y3 y2)]
    (- (* dx1 dy2) (* dx2 dy1))))

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
             (vec-mult l0 l1 s0 s1)
             -1.0)]
    [(+ sx0 (* t sdx))
     (+ sy0 (* t sdy))]))

(defn visible? [lp0 lp1 point sign]
  (>= (* (vec-mult lp0 lp1 lp0 point)
         sign)
      0))

(defn line-segment [[lp0 lp1] [sp0 sp1] sign]
  (let [p0-visible? (visible? lp0 lp1 sp0 sign)
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
  (->> (cons (last view) view)
       (partition 2 1)
       (reduce #(line-polygon %2 sign %1) polygon)))




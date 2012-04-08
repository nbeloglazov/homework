(ns school.quadratic-tree)

(def *max-points* 7)

(defn inside? [[x y width height] [px py]]
  (and (<= x px (+ x width))
       (<= y py (+ y height))))

(defn divide [[x y width height]]
  (let [half-width (/ width 2.0)
        half-height (/ height 2.0)]
        (for [cur-x [x (+ x half-width)]
              cur-y [y (+ y half-height)]]
          [cur-x cur-y half-width half-height])))

(defn filter-inside [rect points]
  (filter #(inside? rect %) points))


(defn build-tree
  ([rect points]
     (if (> (count points) *max-points*)
       {:rect rect
        :children (->> (divide rect)
                       (map #(build-tree % (filter-inside % points))))}
       {:rect rect
        :points points}))
  ([points]
     (let [x-min (apply min (map first points))
           x-max (apply max (map first points))
           y-min (apply min (map second points))
           y-max (apply max (map second points))]
       (build-tree [x-min y-min (- x-max x-min) (- y-max y-min)]
                   points))))

(defn contains-rect? [[b-x b-y b-width b-height] [x y width height]]
  (not (or (< (+ x width) b-x)
           (> x (+ b-x b-width))
           (< (+ y height) b-y)
           (> y (+ b-y b-height)))))

(defn find-points
  ([tree rect result]
     (when (contains-rect? (:rect tree) rect)
       (if (contains? tree :points)
         (->> (:points tree)
              (filter #(inside? rect %))
              (map #(conj! result %))
              (doall))
         (doall (map #(find-points % rect result) (:children tree)))))
     )
  ([tree rect]
     (let [result (transient [])]
       (find-points tree rect result)
       (persistent! result))))

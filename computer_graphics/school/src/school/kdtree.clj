(ns school.kdtree)

(defn inside? [point rect]
  (every? true? (map (fn [[a c] b] (<= a b c)) rect point)))

(def cmps [<= >=])

(defn go? [boundaries v i]
  ((cmps i) (boundaries i) v))

(defn find-points
  ([{:keys [point ind children]} rect result]
     (when (inside? point rect)
       (conj! result point))
     (doseq [i [0 1]]
       (when (and (not (nil? (children i)))
                  (go? (rect ind) (point ind) i))
         (find-points (children i) rect result))))
  ([tree [x y width height]]
     (let [result (transient [])]
       (find-points tree [[x (+ x width)] [y (+ y height)]] result)
       (persistent! result))))

(defn build-tree [points i]
  (if (empty? points)
    nil
    (let [sorted (sort-by #(nth % i) points)
          [left right] (split-at (quot (count sorted) 2) sorted)
          new-i (bit-xor i 1)]
      {:point (first right)
       :ind i
       :children [(build-tree left new-i) (build-tree (rest right) new-i)]})))

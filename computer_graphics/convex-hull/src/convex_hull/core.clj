(ns convex-hull.core)

(defn mult [[x0 y0] [x1 y1] [x2 y2]]
  (- (* (- x1 x0)
        (- y2 y0))
     (* (- x2 x0)
        (- y1 y0))))

(defn build-chain [points sign]
  (loop [pnts (rest points)
         stack [(first points)]]
    (cond (empty? pnts)
          stack
          (= 1 (count stack))
          (recur (rest pnts) (conj stack (first pnts)))
          :else (let [sgn (mult (last (butlast stack)) (last stack) (first pnts))]
                  (if (pos? (* sgn sign))
                    (recur (rest pnts) (conj stack (first pnts)))
                    (recur pnts (pop stack)))))))

(defn convex-hull [points]
  (let [sorted (sort-by first points)
        upper-chain (build-chain sorted 1)
        lower-chain (build-chain sorted -1)]
    (concat upper-chain (reverse lower-chain))))
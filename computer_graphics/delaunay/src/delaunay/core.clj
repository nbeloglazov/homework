(ns delaunay.core
  (:require clojure.set))


(defn mult [[x0 y0] [x1 y1] [x2 y2]]
  (- (* (- x1 x0)
        (- y2 y0))
     (* (- x2 x0)
        (- y1 y0))))

(defn build-chain [points sign]
  (loop [pnts (rest points)
         stack [(first points)]
         triangles #{}]
      (if (empty? pnts)
        {:stack stack
         :triangles triangles}
        (let [new (first pnts)]
          (if (= 1 (count stack))
            (recur (rest pnts) (conj stack new) triangles)
            (let [sgn (mult (last (butlast stack)) (last stack) new)]
              (if (pos? (* sgn sign))
                (recur (rest pnts) (conj stack new) triangles)
                (recur pnts (pop stack) (conj triangles #{new (peek stack) (peek (pop stack))})))))))))

(defn triangulation [points]
  (let [sorted (sort-by first points)]
    (->> [1 -1]
         (map #(build-chain sorted %))
         (map :triangles)
         (apply clojure.set/union))))


(defn edges [triangle]
  (let [[a b c] (vec triangle)]
    [#{a b} #{b c} #{a c}]))

(defn get-neibs [neibs triangle]
  (->> (edges triangle)
       (map neibs)
       (mapcat #(disj % triangle))))

(defn add-to-neibs [neibs triangle]
  (reduce
   #(assoc % %2 (conj (% %2 #{}) triangle))
   neibs
   (edges triangle)))

(defn remove-from-neibs [neibs triangle]
  (reduce #(update-in % [%2] (fn [s] (disj s triangle)))
          neibs
          (edges triangle)))

(defn split-triangles [a b]
  [(vec (clojure.set/intersection a b))
   (mapv first [(clojure.set/difference a b)
                (clojure.set/difference b a)])])

(defn add-to-queue [q neibs triangle]
  (->> (get-neibs neibs triangle)
       (map #(vector triangle %))
       (reduce conj q)))

(defn sq [x]
  (* x x))

(defn dist [[x1 y1] [x2 y2]]
  (Math/sqrt (+ (sq (- x1 x2))
                (sq (- y1 y2)))))

(defn angle [p1 p2 p3]
  (let [a (dist p1 p2)
        b (dist p2 p3)
        c (dist p1 p3)]
    (Math/acos (/ (- (+ (sq b) (sq c))
                     (sq a))
                  (* 2 b c)))))

(defn flip? [a b]
  (let [[[n1 n2] [o1 o2]] (split-triangles a b)]
    (> (+ (angle n1 n2 o1)
          (angle n1 n2 o2)
          -1e-10)
        Math/PI)))


(defn build-neibs [triangles]
  (reduce add-to-neibs {} triangles))

(defn flip-all [triangles1]
  (loop [triangles triangles1
         neibs (build-neibs triangles)
         queue (reduce #(add-to-queue % neibs %2) [] triangles)
         ind 0]
    (if (empty? queue)
      triangles
      (let [[a b] (peek queue)]
        (if (and (triangles a) (triangles b) (flip? a b))
          (let [[[o1 o2] [n1 n2]] (split-triangles a b)
                new-a #{o1 n1 n2}
                new-b #{o2 n1 n2}
                new-neibs (reduce add-to-neibs (reduce remove-from-neibs neibs [a b]) [new-a new-b])]
            (recur (conj (disj triangles a b)  new-a new-b)
                   new-neibs
                   (reduce #(add-to-queue % new-neibs %2) (pop queue) [new-a new-b])
                   (inc ind)))
          (recur triangles
                 neibs
                 (pop queue)
                 (inc ind)))))))


(defn convex-hull [points]
  (let [sorted (sort-by first points)
        upper-chain (:stack (build-chain sorted 1))
        lower-chain (:stack (build-chain sorted -1))]
    (concat upper-chain (reverse lower-chain))))


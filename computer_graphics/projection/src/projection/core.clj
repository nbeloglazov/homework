(ns projection.core)

(defn transpose [mat]
  (apply mapv vector mat))

(defn scalar-mult [a b]
  (apply + (map * a b)))

(defn vector-mult [[x1 y1 z1] [x2 y2 z2]]
  (mapv #(* -1 %)
  [(- (* y1 z2) (* z1 y2))
   (- (* z1 x2) (* z2 x1))
   (- (* x1 y2) (* y1 x2))]))

(defn mult [scalar v]
  (mapv #(* scalar %) v))

(defn matrix-mult [a b]
  (let [t-b (transpose b)]
    (mapv (fn [row] (mapv #(scalar-mult row %) t-b)) a)))

(defn norm [v]
  (->> (mapv #(* % %) v)
       (apply +)
       (Math/sqrt)))

(defn normalize [v]
  (let [v-norm (norm v)]
    (mapv #(/ % v-norm) v)))

(defn R-mat [{:keys [VPN VUP]}]
  (let [r-z (mult -1 VPN)
        r-x (normalize (vector-mult VPN VUP))
        r-y (vector-mult r-x r-z)]
    (transpose [(conj r-x 0)
                (conj r-y 0)
                (conj r-z 0)
                [0 0 0 1]])))

(defn rotation-matrix [a [x y z]]
  (let [c (Math/cos a)
        s (Math/sin a)]
    [[(+ c (* x x (- 1 c))) (- (* x y (- 1 c)) (* z s)) (+ (* x z (- 1 c)) (* y s))]
     [(+ (* x y (- 1 c)) (* z s)) (+ c (* y y (- 1 c))) (- (* y z (- 1 c)) (* x s))]
     [(- (* z x (- 1 c)) (* y s)) (+ (* z y (- 1 c)) (* x s)) (+ c (* z z (- 1 c)))]]))

(defn rotate [{:keys [VPN VPN-y VPN-z] :as config} a-y a-z]
  (let [rot (matrix-mult (rotation-matrix  a-y VPN-y)
                         (rotation-matrix  a-z VPN-z))]
    (reduce #(update-in %1 [%2] (fn [v] (first (matrix-mult [v] rot)))) config [:VPN :VPN-y :VPN-z])))

(defn S-mat [x y z]
  [[x 0 0 0]
   [0 y 0 0]
   [0 0 z 0]
   [0 0 0 1]])

(defn T-mat [x y z]
  [[1 0 0 0]
   [0 1 0 0]
   [0 0 1 0]
   [x y z 1]])

(defn M-mat [z-min]
  (let [z (- z-min)]
   [[1 0 0 0]
    [0 1 0 0]
    [0 0 (/ (inc z)) 1]
    [0 0 (/ z (inc z)) 0]]))


(defn calc-parameters [{:keys [COP TRG focus VPN] :as config}]
  (let [VRP (mapv + COP (mult focus VPN))
        F (* -0.9 focus)]
    (merge config
           {:VRP VRP
            :F F
            :B 1000})))

(defn calc-matrix [{:keys [VPN VUP VRP COP B F width height] :as config}]
  (let [T (apply T-mat (mult -1 COP))
        R (R-mat config)
        T-pl (S-mat 1 1 -1)
        mid (reduce matrix-mult [T R T-pl])
        [[w-x w-y w-z _]] (matrix-mult [(conj VRP 1)] mid)
        [a b] (mapv #(/ % w-z -1) [w-x w-y])
        Sh [[1 0 0 0]
            [0 1 0 0]
            [a b 1 0]
            [0 0 0 1]]
        S (S-mat(/ w-z 0.5 (+ w-z B)) (/ w-z 0.5 (+ w-z B)) (/ (+ w-z B)))
        z-min (/ (+ w-z F) (+ w-z B))
        M (M-mat z-min)]
    ;(println "T" T)
    ;(println "R" R)
    ;(println "Sh" Sh)
                                        ;(println "S" S)
    ;(println M)
    ;(println z-min)
    (assoc config
      :matrix (reduce matrix-mult [mid Sh S])
      :matrix-2 (reduce matrix-mult [M (T-mat 1 1 0) (S-mat 0.5 0.5 1) (S-mat width height 1)])
      :z-min z-min)))

(defn transform [point {:keys [matrix matrix-2 z-min]}]
  (let [[p] (matrix-mult [(conj point 1)] matrix)]
    (if (<= z-min (p 2) 1)
      (let [[pp] (matrix-mult [p] matrix-2)]
        (->> (mapv #(/ % (last pp)) pp)
             (take 2)))
      nil)))
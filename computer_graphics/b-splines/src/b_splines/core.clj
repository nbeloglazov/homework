(ns b-splines.core)

(defn get-x_i [n m i]
  (-> (/ (- i m) n)
      (max 0)
      (min 1)))

(defn get-xs [n m]
  (map #(get-x_i n m %) (range (inc (+ n m m)))))

(defn get-N-0 [xs i x]
  (let [x_i   (nth xs i)
	x_i+1 (nth xs (inc i))]
    (if (and (>= x x_i)
	     (< x x_i+1))
      1
      0)))

(def get-N (memoize (fn [xs i cur-m x]
  (if (zero? cur-m)
    (get-N-0 xs i x)
    (let [[x_i x_i+1 x_i+m+1 x_i+m] (map #(nth xs %)
					 [i (inc i) (+ i cur-m 1) (+ cur-m i)])]
      (+ (if (= x_i+m x_i)
	   0
	   (* (/ (- x x_i)
		 (- x_i+m x_i))
	      (get-N xs i (dec cur-m) x)))
	 (if (= x_i+1 x_i+m+1)
	   0
	   (* (/ (- x_i+m+1 x)
		 (- x_i+m+1 x_i+1))
	      (get-N xs (inc i) (dec cur-m) x)))))))))

(defn get-b-spline [points degree]
  (let [M (dec (count points))
	xs (get-xs (- M degree -1) degree)
	pnts-x (map first points)
	pnts-y (map last points)]
    (fn [t]
      (let [Ns (map #(get-N xs % degree t) (range (inc M)))]
	[(reduce + (map * Ns pnts-x))
	 (reduce + (map * Ns pnts-y))]))))

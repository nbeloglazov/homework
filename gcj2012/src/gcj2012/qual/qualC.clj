(ns gcj2012.qual.qualC)

(defn shift [num max]
  (+ (* max (mod num 10))
     (quot num 10)))

(defn highest [num]
  (->> (iterate #(* 10 %) 1)
       (take-while #(<= % num))
       (last)))

(defn all-shifts [num min max]
  (let [mx (highest num)]
    (->> (iterate #(shift % mx) num)
         (take (count (str num)))
         (filter #(<= min % max))
         (set))))

(defn add [[was res] number mn mx]
  (let [shifts (all-shifts number mn mx)
        key (reduce min shifts)
        cnt (count shifts)]
    [(conj was key)
     (if (was key)
       res
       (+ res (/ (* cnt (dec cnt)) 2)))]))



(defn solve
  ([min max]
     (->> (range min (inc max))
          (reduce #(add %1 %2 min max) [#{} 0])
          (second)))

  ([]
     (->> (slurp "input.txt")
          (clojure.string/split-lines)
          (rest)
          (map #(re-seq #"\d+" %))
          (map (fn [test] (map #(Integer/parseInt %) test)))
          (map #(apply solve %))
          (map-indexed #(format "Case #%d: %d" (inc %1) %2))
          (doall)
          (interpose \newline)
          (apply str)
          (spit "output.txt"))))

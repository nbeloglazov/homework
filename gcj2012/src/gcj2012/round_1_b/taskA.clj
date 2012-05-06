(ns gcj2012.round-1-b.taskA)

(defn level? [s level sum]
  (->> (map #(- level %) s)
       (remove neg?)
       (apply +)
       (>= sum)))

(defn find-level [s]
  (let [sum (apply + s)]
    (loop [left 0.0
           right (* 2 sum)]
      (if (> 1e-9 (- right left))
        left
        (let [mid (/ (+ left right) 2)]
          (if (level? s mid sum)
            (recur mid right)
            (recur left mid)))))))

(defn calc-prob [s]
  (let [level (find-level s)
        sum (apply + s)]
    (map #(if (>= % level)
            0
            (/ (- level %) sum 0.01)) s)))


(defn parse-line [line]
  (map #(Integer/parseInt %) (rest (.split line " "))))

(defn seq-to-str [s]
  (apply str (interpose " " s)))

(defn solve []
  (->> (slurp "input.txt")
       (clojure.string/split-lines)
       (rest)
       (map parse-line)
       (map calc-prob)
       (map seq-to-str)
       (map-indexed #(format "Case #%d: %s" (inc %1) %2))
       (doall)
       (interpose \newline)
       (apply str)
       (spit "output.txt")))

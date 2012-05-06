(ns gcj2012.round-1-b.taskC
  (:require [clojure.math.combinatorics :as comb]))

(defn seq-to-str [s]
  (apply str (interpose " " s)))

(defn find-subsets [s]
  (loop [subsets (comb/subsets s)
         vals {}]
    (if (empty? subsets)
      "Impossible"
      (let [nxt (first subsets)
            sum (apply + nxt)]
        (if (contains? vals sum)
          (str (seq-to-str (vals sum))
               "\n"
               (seq-to-str nxt))
          (recur (rest subsets)
                 (assoc vals sum nxt)))))))

(defn parse-line [line]
  (map #(Integer/parseInt %) (rest (.split line " "))))

(defn solve []
  (->> (slurp "input.txt")
       (clojure.string/split-lines)
       (rest)
       (map parse-line)
       (map find-subsets)
       (map-indexed #(format "Case #%d: \n%s" (inc %1) %2))
       (doall)
       (interpose \newline)
       (apply str)
       (spit "output.txt")))



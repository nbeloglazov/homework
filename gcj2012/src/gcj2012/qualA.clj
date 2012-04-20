(ns gcj2012.qualA)

(def pairs [["ejp mysljylc kd kxveddknmc re jsicpdrysi" "our language is impossible to understand"]
            ["rbcpc ypc rtcsra dkh wyfrepkym veddknkmkrkcd" "there are twenty six factorial possibilities"]
            ["de kr kd eoya kw aej tysr re ujdr lkgc jv" "so it is okay if you want to just give up"]
            ["qz" "zq"]])

(defn dict [pairs]
  (->> (map (fn [[a b]] (map vector a b)) pairs)
       (map #(into {} %))
       (reduce merge)))

(def d (dict pairs))

(defn translate [word]
  (apply str (map d word)))

(defn solve []
  (->> (slurp "input.txt")
       (clojure.string/split-lines)
       (rest)
       (map translate)
       (map-indexed #(format "Case #%d: %s" (inc %1) %2))
       (doall)
       (interpose \newline)
       (apply str)
       (spit "output.txt")))




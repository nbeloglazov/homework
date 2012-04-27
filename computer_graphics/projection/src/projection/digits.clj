(ns projection.digits)


(defn create-points [& points]
  (concat (map #(cons 0 %) points)
          (map #(cons 1 %) points)))

(defn conjv [s v]
  (conj (vec s) v))

(defn rangel
  ([start end]
     (conjv (range start end) start))
  ([end]
     (rangel 0 end)))

(defn create-faces [s1 s2]
  (map (fn [[f0 f1] [b0 b1]] [f0 f1 b1 b0 f0])
       (partition 2 1 s1)
       (partition 2 1 s2)))


(def digits-map
  {\0 {:points (create-points
                [0 0]
                [0 5]
                [3 5]
                [3 0]
                [1 1]
                [1 4]
                [2 4]
                [2 1])
       :faces (let [f-out (rangel 4)
                    f-in (rangel 4 8)
                    b-out (rangel 8 12)
                    b-in (rangel 12 16)]
                (concat (create-faces f-in f-out)
                        (create-faces b-in b-out)
                        (create-faces f-in b-in)
                        (create-faces f-out b-out)))}
   \1 {:points (create-points
                [0 0]
                [0 1]
                [1 1]
                [1 4]
                [0 4]
                [0 5]
                [2 5]
                [2 1]
                [3 1]
                [3 0])
       :faces (let [front (rangel 10)
                    back (rangel 10 20)]
                (concat [front back]
                        (create-faces front back)))}})


(defn move-object [{:keys [points] :as object} shift]
  (assoc object
    :points (mapv #(mapv + % shift) points)))

(defn digits [num]
  (->> (Integer/toBinaryString num)
       (seq)
       (concat [\0 \0 \0 \0 \0])
       (take-last 6)))

(defn get-objects [num]
  (let [shift [20 -12 -2.5]]
    (->> (digits num)
         (map digits-map)
         (map-indexed #(move-object %2 [0 (* 4 %1) 0]))
         (mapv #(move-object % shift)))))
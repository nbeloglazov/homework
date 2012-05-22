(ns lab2.core
  (:require clojure.set))

(defn parse-data [data]
  (let [objects (reduce (fn [res [cl attrs]]
                          (assoc res cl (conj (res cl []) attrs)))
                        {} data)]
    {:objects objects
     :attrs (->> (vals objects)
                 (apply concat)
                 (reduce clojure.set/union))}))

(defn map-fn [f coll]
  (reduce #(assoc % %2 (f %2)) {} coll))

(defn averages [objects attrs]
  (println attrs objects)
  (let [m (count objects)
        average #(/ (count (filter % objects)) m)]
    (map-fn average attrs)))

(defn average-attr [averages attr]
  (let [l (count averages)]
    (->> (vals averages)
         (map attr)
         (apply +)
         (#(/ % l)))))

(defn deviations [averages bs]
  (map-fn #(Math/abs (double (- (bs %) (averages %)))) (keys bs)))

(defn teach [{:keys [objects attrs] :as data}]
  (let [averages (into {} (map #(update-in % [1] averages attrs) objects))
        bs (map-fn #(average-attr averages %) attrs)
        res (into {} (map #(update-in % [1] deviations bs) averages))]
    (assoc data
      :devs res)))

(defn objects-similarity [target sample devs]
  (let [sum (apply + (vals devs))]
    (->> (map #((if (= (target %) (sample %)) + -) (devs %)) (keys devs))
         (apply +)
         (#(/ % sum))
         (max 0.0))))

(defn class-similarity [target {:keys [devs objects]} class]
  (apply max (map #(objects-similarity target % (devs class)) (objects class))))

(defn similarities [target data]
  (map-fn #(class-similarity target data %) (keys (:objects data))))
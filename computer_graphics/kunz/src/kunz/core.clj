(ns kunz.core)

(defn L [x]
  [(- 1 x) x])

(defn mult-L [L v]
  (apply mapv + (mapv #(mapv (partial * %) %2) L v)))

(defn m-L [x v]
  (mult-L (L x) v))

(defn interpolate [p0 p1]
  (fn [s t]
    (let [p (fn [s t] (m-L s [(p0 t) (p1 t)]))
          q (mapv +
                  (m-L s [(p 0 t) (p 1 t)])
                  (m-L t [(p s 0) (p s 1)]))
          b (m-L t [(m-L s [(p 0 0) (p 1 0)])
                    (m-L s [(p 0 1) (p 1 1)])])]
      (mapv - q b))))

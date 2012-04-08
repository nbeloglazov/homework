(ns ortogonal-intersection.intersection
  (:import [java.util TreeMap]))

(defmulti to-events :type)

(defmethod to-events :vertical [{x :x :as segment}]
  [{:type :vertical
    :x x
    :segment segment}])

(defmethod to-events :horizontal [{[start end] :x :as segment}]
  [{:type :start
    :x start
    :segment segment}
   {:type :end
    :x end
    :segment segment}])

(def order {:start 0
            :vertical 1
            :end 2})
(defn compare-events [{x1 :x type1 :type} {x2 :x type2 :type}]
  (if (== x1 x2)
    (- (order type1) (order type2))
    (- x1 x2)))

(defn to-events-seq [segments]
  (->> segments
       (map to-events)
       (apply concat)
       (sort compare-events)))

(defn insert-into-map [tree-map {y :y :as segment}]
  (when-not (.containsKey tree-map y)
    (.put tree-map y #{}))
  (.put tree-map y (conj (.get tree-map y) segment)))

(defn remove-from-map [tree-map {y :y :as segment}]
  (.put tree-map y (disj (.get tree-map y) segment)))

(defmulti process-event (fn [event _ _] (:type event)))

(defmethod process-event :start [event tree-map _]
  (insert-into-map tree-map (:segment event)))

(defmethod process-event :end [event tree-map _]
  (remove-from-map tree-map (:segment event)))

(defmethod process-event :vertical [event tree-map callback]
  (let [{[lower upper] :y x :x} (:segment event)]
    (->> (.subMap tree-map lower true upper true)
         (.values)
         (apply concat)
         (map (fn [{y :y}] [x y]))
         (map callback)
         (doall))))


(defn process-events [events callback]
  (let [tree-map (TreeMap.)]
    (doseq [event events] (process-event event tree-map callback))))

(defn find-intersections [segments callback]
  (-> (to-events-seq segments)
      (process-events callback)))


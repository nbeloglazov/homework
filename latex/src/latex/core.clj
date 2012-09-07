(ns latex.core
  (:require [clojure.string :as string]))

(defn remove-comments-and-whitespaces [text]
  (->> (string/split-lines text)
       (map #(string/replace % #"/\*.*?\*/" ""))
       (map string/trim)
       (remove empty?)))

(defn parse-data [data]
  (map #(->> % (replace {\} \] \{ \[}) (apply str) read-string) data))

(defmacro dyn-destruct [data forms & body]
  (let [pairs (partition 2 forms)
        dat-sym (gensym "data")
        make-entry (fn [[var obj]]
                     (if (= \! (first (str var)))
                       [var obj]
                       [[var dat-sym] (list 'split-at obj dat-sym)]))
        bindings (->> forms (partition 2) (mapcat make-entry))]
    `(let [~dat-sym ~data
           ~@bindings]
       ~@body)))

(defn build-graph [data]
  (dyn-destruct
   data
   [!parse-bools (fn [coll]  (map #(= 1 %) coll))
    [n m] 2
    edges m
    [layers-n] 1
    !total-x (* m layers-n)
    layers !total-x
    single-restr !total-x
    !single-restr (!parse-bools single-restr)
    inter-restr m
    !inter-restr (!parse-bools inter-restr)
    [eq-n] 1
    eq-coeffs (* eq-n !total-x)
    goal-coeffs !total-x
    _ (* m layers-n)
    single-restr-right (count (filter true? !single-restr))
    inter-restr-right (count (filter true? !inter-restr))
    ver-sum (* n layers-n)
    eq-right eq-n]
   {:n n
    :m m
    :edges edges
    :layers-n layers-n
    :layers (->> layers !parse-bools (partition m))
    :single-restr (partition m !single-restr)
    :inter-restr !inter-restr
    :eq-n eq-n
    :eq-coeffs eq-coeffs
    :goal-coeffs goal-coeffs
    :single-restr-right single-restr-right
    :inter-restr-right inter-restr-right
    :ver-sum (partition n ver-sum)
    :eq-right eq-right}))

(defn read-graph []
  (-> (slurp "input.txt")
      remove-comments-and-whitespaces
      parse-data
      build-graph))


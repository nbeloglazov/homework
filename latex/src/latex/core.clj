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
    :eq-coeffs (map #(partition m %) (partition !total-x eq-coeffs))
    :goal-coeffs (partition m goal-coeffs)
    :single-restr-right single-restr-right
    :inter-restr-right inter-restr-right
    :ver-sum (partition n ver-sum)
    :eq-right eq-right}))

(defn read-graph []
  (-> (slurp "input.txt")
      remove-comments-and-whitespaces
      parse-data
      build-graph))

(defn stringify [obj]
  (apply str (if (coll? obj) (flatten obj) obj)))

(defn join-lines [coll]
  (string/join \newline coll))

(defn header [_]
  "\\documentclass{article}
\\usepackage{amssymb,amsmath}
\\usepackage[english,russian]{babel}
\\usepackage{graphicx}
\\begin{document}
\\begin{center}")

(defn bs
  ([_] (bs))
  ([]  "\\bigskip"))

(defn footer [_]
  "
\\end{center}
\\end{document}
")

(defn make-x [ind coef indices layer]
  (if (zero? coef) ""
      (format "%s%sx_{%s}^{%s}"
              (if (or (zero? ind) (neg? coef)) "" "+")
              (cond (= 1 coef) ""
                    (= -1 coef) "-"
                    :else coef)
              (apply str indices)
              (inc layer))))

(defn goal [{:keys [goal-coeffs edges]}]
  ["$"
   (map-indexed
    (fn [layer coeffs]
      (map (fn [ind coef x]
             (make-x ind coef x layer))
           (range) coeffs edges))
    goal-coeffs)
   "\\to\\min$\\\\"])

(defn vertices-sum [{:keys [edges ver-sum]}]
  (letfn [(vertex-sum [n layer sum]
            ["$" (->> edges
                      (filter #((set %) n))
                      (map-indexed (fn [ind [a b]]
                                     (make-x ind
                                             (if (= n a) 1 -1)
                                             [a b]
                                             layer))))
             "="
             sum
             "$\\\\"])
          (vertices-sum-layer [layer ver-sum]
            (->> ver-sum
                 (map-indexed #(vertex-sum (inc %1) layer %2))
                 (map stringify)
                 join-lines))]
    (->> ver-sum
         (map-indexed vertices-sum-layer)
         (interpose (bs))
         join-lines)))

(defn equations [{:keys [eq-right eq-coeffs edges]}]
  (letfn [(single-equation [coeffs value]
            ["$" (map-indexed
                  (fn [layer coeffs]
                    (map
                     (fn [ind coeff edge]
                       (make-x (if (and (zero? layer) (zero? ind)) 0 1)
                               coeff
                               edge
                               layer))
                     (range) coeffs edges))
                  coeffs)
             "="
             value
             "$\\\\"])]
    (->> (map single-equation eq-coeffs eq-right)
         (map stringify)
         (interpose (bs))
         join-lines)))

(def structure
  [header
   goal
   bs
   vertices-sum
   bs
   equations
   footer])


(defn build-tex [graph]
  (->> (map #(% graph) structure)
       (map #(if (coll? %) (flatten %) %))
       (map #(apply str %))
       (string/join \newline)
       (spit "output.tex")))
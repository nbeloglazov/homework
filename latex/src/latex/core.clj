(ns latex.core
  (:require [clojure.string :as string]))

(defn remove-comments-and-whitespaces [text]
  (->> (string/split-lines text)
       (map #(string/replace % #"/\*.*?\*/" ""))
       (map string/trim)
       (remove empty?)))

(defn parse-data [data]
  (map #(->> % (replace {\} \] \{ \[}) (apply str) read-string) data))

(defn read-data []
  (-> (slurp "input.txt")
      remove-comments-and-whitespaces
      parse-data))
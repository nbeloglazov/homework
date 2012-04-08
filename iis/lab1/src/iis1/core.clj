(ns iis1.core
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))


(def known (atom {}))
(def rules (atom []))
(def ask-callback (atom nil))
(def misc (atom {}))

(declare resolve-attribute)

(defn get-attribute [attribute]
  (when-not (contains? @known attribute)
    (swap! known assoc attribute (resolve-attribute attribute)))
  (@known attribute))

(defn resolve-rule [{cnd :if [_ value] :than :as rule}]
  (swap! rules #(remove (fn [x] (= x rule)) %))
  (if (every? true? (map #(= (second %)
                            (get-attribute (first %)))
                         cnd))
    value
    nil))

(defn resolve-attribute [attribute]
  (letfn [(rule-matches? [{ [rule-attribute _] :than}]
            (= attribute rule-attribute))]
    (->> (lazy-cat @rules [:ask_user])
         (map #(if (= % :ask_user)
                 (@ask-callback attribute (attribute @misc))
                 (when (rule-matches? %) (resolve-rule %))))
         (remove nil?)
         (first))))


(defn parse-attribute [attribute]
  (->> (string/trim attribute)
       (#(string/split % #"[()]"))
       (map keyword)))

(defn parse-rule [rule]
  (let [[left right] (string/split rule #"->")]
    {:if (->> (string/split left #",")
              (map parse-attribute))
     :than (parse-attribute right)}))


(defn add-yes-no-options [attributes]
  (concat attributes
          (->> (filter #(#{:yes :no} (second %)) attributes)
               (map vec)
               (map #(update-in % [1] {:yes :no :no :yes})))))

(defn build-misc [rules]
  (let [attributes (->> (map (fn [{:keys [if than]}] (conj if than))
                             rules)
                        (reduce concat)
                        (add-yes-no-options))]
    (merge {:attributes (set (map first attributes))}
           (->> (map (fn [[a b]] {a #{b}}) attributes)
                (apply merge-with clojure.set/union)))))

(defn available-attributes []
  (:attributes @misc))


(defn init [rules-str ask-cb]
  (reset! known {})
  (reset! rules (->> (map string/trim rules-str)
                     (remove empty?)
                     (map parse-rule)))
  (reset! misc (build-misc @rules))
  (reset! ask-callback ask-cb))


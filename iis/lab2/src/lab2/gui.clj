(ns lab2.gui
  (:require [seesaw.core :as sc]
            [lab2.core :as core])
  (:gen-class))

(def database "database.clj")
(def data (atom nil))

(defn read-data []
  (read-string (slurp database)))

(defn create-checkboxes []
  (->> @data
       :attrs
       (map name)
       (mapv #(sc/checkbox :text %))))

(defn selected-attrs [checkboxes]
  (->> (filter sc/selection checkboxes)
       (map #(.getText %))
       (map keyword)
       (set)))

(defn good-output [res]
  (->> (sort-by (comp - second) res)
       (map (fn [[class val]] (format "%6s %.2f" (name class) val)))
       (interpose \newline)
       (apply str)))

(defn classify [attrs]
  (->> (core/similarities attrs @data)
       good-output
       (sc/alert)))

(defn create-panel []
  (let [cb (create-checkboxes)
        but (sc/button :text "Classify"
                       :listen [:action (fn [_] (classify (selected-attrs cb)))])]
    (sc/vertical-panel :items (conj cb but))))

(defn create-frame []
  (sc/frame :title "Classifier"
            :content (create-panel)
            :on-close :dispose))

(defn start []
  (reset! data (-> (read-data) core/parse-data core/teach))
  (println @data)
  (-> (create-frame)
      sc/pack!
      sc/show!))

(defn -main [& args]
  (start))

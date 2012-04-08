(ns iis1.gui
  (:require [seesaw.core :as sc]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [iis1.core :as core])
  (:gen-class))

(def *database* "database.txt")

(defn humanize [sym]
  (-> (name sym)
      (string/capitalize)
      (string/replace #"[-_]" " ")))

(defn ask-user [attribute choices]
  (sc/input (format "Specify '%s', if you know it" (humanize attribute))
            :choices choices
            :to-string humanize))

(defn start []
  (core/init (line-seq (io/reader *database*))
             ask-user)
  (if-let [attribute (sc/input "What do you want do know?"
                               :choices (core/available-attributes)
                               :to-string humanize)]
    (sc/alert (humanize (core/resolve-attribute attribute)))
    (sc/alert "No attribute is selected.")))

(defn -main [& args]
  (start))

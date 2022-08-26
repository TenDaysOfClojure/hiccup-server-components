(ns ^:no-doc ten-d-c.hiccup-server-components.markup-helpers
  (:require [clojure.string :as string]))


(defn css-classes [& classes]
  (->> classes
       (flatten)
       (remove nil?)
       (map name)
       (map #(string/split % #"\.|\,|\s+"))
       (apply concat)
       (map string/trim)
       (remove string/blank?)
       (string/join " ")))

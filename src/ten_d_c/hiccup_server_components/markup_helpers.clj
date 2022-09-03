(ns ^:no-doc ten-d-c.hiccup-server-components.markup-helpers
  (:require [clojure.string :as string]
            [hiccup2.core :as hiccup]))


(defn- escape-replacement-part [string-part]
  (->> (string/split string-part #"")
       (map #(str "\\" %))
       (string/join)))


(defn- replacement-pattern [start-tag value end-tag]
  (str (escape-replacement-part start-tag)
       (name value)
       (escape-replacement-part end-tag)))


(defn- build-replacement-pattern [tags variable-name]
  (let [var-name (-> (name variable-name)
                     (string/escape {\? "\\?"
                                     \! "\\!"
                                     \* "\\*"}))]
    (->> tags
         (map (fn [[start-tag end-tag]]
                (replacement-pattern
                 start-tag var-name end-tag)))
         (string/join "|")
         (re-pattern))))


(defn raw-html [& html]
  (hiccup/raw (string/join html)))


(defn javascript [& javascript]
  (let [sanitised (-> javascript
                      (string/join)
                      (string/replace #"\s+\n+\s+|\n+\s+|\s+\n+|\n+" " "))]
    (hiccup/raw sanitised)))



(defn string-template [variables content]
  (reduce
   (fn [updated-content [variable-name value]]
     (let [replacement-pattern (build-replacement-pattern
                                [["{{" "}}"]
                                 ["{" "}"]

                                 ["<<" ">>"]
                                 ["<" ">"]

                                 ["!!" "!!"]
                                 ["!" "!"]

                                 ["$$" "$$"]
                                 ["$" "$"]]
                                variable-name)

           substitute-value    (if (keyword? value)
                                 (name value)
                                 (str value))]

       (string/replace updated-content replacement-pattern substitute-value)))
   content
   variables))


(defn css-classes [& options-and-classes]
  (let [includes-variables? (map? (first options-and-classes))

        variables           (if includes-variables?
                              (first options-and-classes)
                              {})

        effective-classes   (if includes-variables?
                              (rest options-and-classes)
                              options-and-classes)]
    (->> effective-classes
         (flatten)
         (remove nil?)
         (map name)
         (map #(string/split % #"\.|\,|\s+"))
         (apply concat)
         (map string/trim)
         (remove string/blank?)
         (string/join " ")
         (string-template variables))))

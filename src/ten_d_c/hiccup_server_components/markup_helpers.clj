(ns ^:no-doc ten-d-c.hiccup-server-components.markup-helpers
  (:require [clojure.string :as string]))


(defn replacement-pattern [& parts]
  (apply str parts))


(defn string-template [vars content]
  (reduce
   (fn [updated-content [var value]]
     (let [;; Cater for question marks in the variable name :display-legend?
           var-name           (-> (name var)
                                  (string/escape {\? "\\?"
                                                  \! "\\!"
                                                  \* "\\*"}))

           substitute-value    (if (keyword? value)
                                 (name var)
                                 (str value))

           replacement-pattern (re-pattern
                                (string/join
                                 "|"
                                 [(replacement-pattern
                                   "\\{\\{" var-name "\\}\\}")

                                  (replacement-pattern
                                   "\\{" var-name "\\}")

                                  (replacement-pattern
                                   "\\<\\<" var-name "\\>\\>")
                                  (replacement-pattern
                                   "\\<" var-name "\\>")

                                  (replacement-pattern
                                   "\\!\\!" var-name "\\!\\!")
                                  (replacement-pattern
                                   "\\!" var-name "\\!")

                                  (replacement-pattern
                                   "\\$\\$" var-name "\\$\\$")
                                  (replacement-pattern
                                   "\\$" var-name "\\$")]))]

       (string/replace updated-content replacement-pattern
                       substitute-value)))
   content
   vars))


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

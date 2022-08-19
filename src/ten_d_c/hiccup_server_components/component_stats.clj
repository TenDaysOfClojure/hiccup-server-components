(ns ^:no-doc ten-d-c.hiccup-server-components.component-stats
  (:require [ten-d-c.hiccup-server-components.components :as components]))


(defn component-counts
  ([] (component-counts (components/all-components)))

  ([all-components]
   (->> all-components
        (map :element-name)
        (group-by namespace)
        (mapv (fn [[component-namespace components]]
                {:component-namespace component-namespace
                 :total-components (count components)}))
        (sort-by :component-namespace))))

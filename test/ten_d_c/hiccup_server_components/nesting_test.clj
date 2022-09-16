(ns ten-d-c.hiccup-server-components.nesting-test
  (:require  [clojure.test :refer :all]
             [ten-d-c.hiccup-server-components.core :as hc]))

(deftest nesting-test

  (testing "Nested components"

    (hc/clear-components)

    (hc/reg-component
     :ux/one
     (fn [main]
       [:div.one
        main
        [:ux/one-1 "from-one"]]))


    (hc/reg-component
     :ux/one-1
     (fn [text]
       [:div.one-1
        text
        [:ux/one-1-2 "from-one-1"]]))


    (hc/reg-component
     :ux/one-1-2
     (fn [text]
       [:div.one-1-2
        text
        [:ux/one-1-2-3 "from-one-1-2"]]))


    (hc/reg-component
     :ux/one-1-2-3
     (fn [text]
       [:div.one-1-2-3
        text
        [:ux/one-1-2-3-4 "from-one-1-2-3"]]))


    (hc/reg-component
     :ux/one-1-2-3-4
     (fn [text]
       [:div.one-1-2-3-4
        text
        "TERMINITE"]))


    (is (= [:main
            [:div.one
             "START"
             [:div.one-1
              "from-one"
              [:div.one-1-2
               "from-one-1"
               [:div.one-1-2-3
                "from-one-1-2"
                [:div.one-1-2-3-4
                 "from-one-1-2-3" "TERMINITE"]]]]]]
           (hc/->hiccup
            [:main
             [:ux/one "START"]])))))

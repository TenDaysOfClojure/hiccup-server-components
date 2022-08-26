(ns ten-d-c.hiccup-server-components.markup-helpers-test
  (:require [ten-d-c.hiccup-server-components.markup-helpers :as markup-helpers]
            [clojure.test :refer :all]))

(deftest markup-helpers-test

  (testing "css-classes"

    (is (= "one two three"
           (markup-helpers/css-classes :one.two.three)))

    (is (= "one two three"
           (markup-helpers/css-classes "one.two.three")))

    (is (= "one two three"
           (markup-helpers/css-classes "one two three")))

    (is (= "one two three"
           (markup-helpers/css-classes "one    two     three")))

    (is (= "one two three"
           (markup-helpers/css-classes "one,two,three")))

    (is (= "one two three"
           (markup-helpers/css-classes "one,  two   ,three")))

    (is (= "one two three"
           (markup-helpers/css-classes "      one    ,  two   ,  three")))

    (is (= "one two three four five six seven eight"
           (markup-helpers/css-classes [:one.two.three]
                                       :four
                                       nil
                                       :five
                                       [:six "seven" "eight"]
                                       ""
                                       nil)))

    (is (= "two three"
           (markup-helpers/css-classes (when false :one)
                                       (when true :two)
                                       :three)))

    (is (= ""
           (markup-helpers/css-classes nil)))

    (is (= ""
           (markup-helpers/css-classes "")))

    (is (= ""
           (markup-helpers/css-classes " ")))

    (is (= ""
           (markup-helpers/css-classes
            nil nil nil "" " " "      " nil)))))

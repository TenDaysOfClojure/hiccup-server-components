(ns ten-d-c.hiccup-server-components.markup-helpers-test
  (:require [ten-d-c.hiccup-server-components.markup-helpers :as markup-helpers]
            [clojure.test :refer :all]))


(deftest markup-helpers-test

  (testing "string-template"

    (let [replacement-pattern "bg-{{colour}}-400 text-{{colour}}-600 text-{{text-colour}}"
          expected-output     "bg-stone-400 text-stone-600 text-white"]

      (is (= expected-output
             (markup-helpers/string-template
              {:colour :stone :text-colour :white}
              replacement-pattern)))

      (is (= expected-output
             (markup-helpers/string-template
              {:colour "stone" :text-colour "white"}
              replacement-pattern))))


    (is (= "bg-stone-400 text-stone-600 text-white"
           (markup-helpers/string-template
            {:colour :stone
             :text-colour :white
             :light-number 400
             :dark-number 600}
            "bg-stone-{{light-number}} text-stone-{{dark-number}} text-white")))


    (let [expected-output "Hello Bob Smith your email address is bobsmith@mailinator.com"]

      (is (= expected-output
             (markup-helpers/string-template
              {:name "Bob Smith" :email-address "bobsmith@mailinator.com"}
              "Hello {{name}} your email address is {{email-address}}")))


      (is (= expected-output
             (markup-helpers/string-template
              {:name "Bob Smith" :email-address "bobsmith@mailinator.com"}
              "Hello {name} your email address is {email-address}")))


      (is (= expected-output
             (markup-helpers/string-template
              {:name "Bob Smith" :email-address "bobsmith@mailinator.com"}
              "Hello <<name>> your email address is <<email-address>>")))


      (is (= expected-output
             (markup-helpers/string-template
              {:name "Bob Smith" :email-address "bobsmith@mailinator.com"}
              "Hello <name> your email address is <email-address>")))


      (is (= expected-output
             (markup-helpers/string-template
              {:name "Bob Smith" :email-address "bobsmith@mailinator.com"}
              "Hello !!name!! your email address is !!email-address!!")))


      (is (= expected-output
             (markup-helpers/string-template
              {:name "Bob Smith" :email-address "bobsmith@mailinator.com"}
              "Hello !name! your email address is !email-address!")))


      (is (= expected-output
             (markup-helpers/string-template
              {:name "Bob Smith" :email-address "bobsmith@mailinator.com"}
              "Hello $$name$$ your email address is $$email-address$$")))


      (is (= expected-output
             (markup-helpers/string-template
              {:name "Bob Smith" :email-address "bobsmith@mailinator.com"}
              "Hello $name$ your email address is $email-address$")))))

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


    (is (= "" (markup-helpers/css-classes nil)))

    (is (= "" (markup-helpers/css-classes "")))

    (is (= "" (markup-helpers/css-classes " ")))

    (is (= "" (markup-helpers/css-classes
            nil nil nil "" " " "      " nil))))


  (testing "css-classes variable substitution"

    (is (= "bg-red-300 text-red-500"
           (markup-helpers/css-classes
            {:colour "red"}
            "bg-{{colour}}-300.text-{{colour}}-500")))


    (is (= "border-true radius-false x-border-true x-radius-false y-border-true y-radius-false"
           (markup-helpers/css-classes
            {:include-border? true :do-not-include-radius! false}
            "border-{{include-border?}} radius-{{do-not-include-radius!}}"
            "x-border-<include-border?> x-radius-<do-not-include-radius!>"
            :y-border-<include-border?>.y-radius-<do-not-include-radius!>)))


    (is (= "bg-red-300 text-red-500"
           (markup-helpers/css-classes
            {:colour "red"}
            "bg-<colour>-300.text-<colour>-500")))


    (is (= "bg-red-300 text-red-500"
           (markup-helpers/css-classes
            {:colour "red"}
            :bg-<colour>-300.text-<colour>-500)))


    (is (= "bg-red-300 text-blue-500"
           (markup-helpers/css-classes
            {:bg-colour "red" :text-colour "blue"}
            :bg-<bg-colour>-300.text-<text-colour>-500)))


    (is (= "bg-stone-400 text-stone-600 text-white"
           (markup-helpers/css-classes
            {:colour :stone
             :text-colour :white
             :light-number 400
             :dark-number 600}
            "bg-stone-{{light-number}} text-stone-{{dark-number}} text-white")))


    (is (= "bg-red-300 text-red-500 bg-red-300 text-red-400 hover:text-red-500"
           (markup-helpers/css-classes
            {:colour "red" :light-number 300 :darker-number 500}
            :bg-<colour>-<light-number>.text-<colour>-<darker-number>
            "bg-{{colour}}-300 text-{{colour}}-400"
            "hover:text-<colour>-500")))))

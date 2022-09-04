(ns ten-d-c.hiccup-server-components.built-in-components-test
  (:require [ten-d-c.hiccup-server-components.built-in-components ]
            [clojure.test :refer :all]
            [ten-d-c.hiccup-server-components.core :as hc]))


(defn assert-built-in-component-exists [element-name]
  (let [metadata (hc/get-component-meta-data element-name)]

    (is (= "ten_d_c.hiccup_server_components.built_in_components"
           (:namespace metadata)))

    (is (true?
         (:hsc/built-in? metadata)))))


(deftest built-in-components-test

  (testing ":ux/html5-doc"

    (hc/clear-components)

    (assert-built-in-component-exists :ux/html5-doc)

    (let [html-document      [:ux/html5-doc
                              [:head
                               [:title "Test HTML document"]]
                              [:body
                               [:h1 "Hello world"]]]

          [doctype document] (hc/->hiccup html-document)]

      (is (= hiccup.util.RawString (type doctype)))
      (is (= "<!DOCTYPE html>" (str doctype)))

      (is (= [:html
              '([:head
                 [:title "Test HTML document"]]
                [:body
                 [:h1 "Hello world"]])]
             document))

      (is (= (str
              "<!DOCTYPE html><html><head><title>Test HTML document</title>"
              "</head><body><h1>Hello world</h1></body></html>")
             (hc/->html html-document)))

      (hc/reg-component :ux/test-page
                        (fn [title]
                          [:ux/html5-doc
                           [:head
                            [:title title]]
                           [:body
                            [:h1 "Hello world"]
                            [:p "This is a test"]]]))

      (let [html-document      [:ux/test-page "My test page"]
            [doctype document] (hc/->hiccup html-document)]

        (is (= hiccup.util.RawString (type doctype)))
        (is (= "<!DOCTYPE html>" (str doctype)))

        (is (= [:html
                '([:head [:title "My test page"]]
                  [:body [:h1 "Hello world"]
                   [:p "This is a test"]])]
               document))

        (is (= (str
                "<!DOCTYPE html><html><head><title>My test page</title></head>"
                "<body><h1>Hello world</h1><p>This is a test</p></body></html>")
               (hc/->html html-document))))))


  (testing ":ux/fragment"

    (hc/clear-components)

    (assert-built-in-component-exists :ux/fragment)

    (hc/reg-component
     :ux/paragraph-fragment
     (fn []
       [:ux/fragment
        [:p "This is paragraph one"]
        [:p "This is paragraph two"]
        [:p "This is paragraph three"]]))

    (is (= [:div
            '([:p "This is paragraph one"]
              [:p "This is paragraph two"]
              [:p "This is paragraph three"])]
           (hc/->hiccup
            [:div
             [:ux/paragraph-fragment]])))

    (hc/reg-component
     :ux/list-fragment
     (fn []
       [:ux/fragment
        [:li "List item 1"]
        [:li "List item 2"]
        [:li "List item 3"]]))

    (is (= [:div
            [:ul
             '([:li "List item 1"]
               [:li "List item 2"]
               [:li "List item 3"])]
            [:ol
             '([:li "List item 1"]
               [:li "List item 2"]
               [:li "List item 3"])]]
           (hc/->hiccup
            [:div
             [:ul [:ux/list-fragment]]
             [:ol [:ux/list-fragment]]])))

    (hc/reg-component
     :ux/thead-fragment
     (fn []
       [:ux/fragment
        [:th "Header 1"]
        [:th "Header 2"]
        [:th "Header 3"]]))

    (hc/reg-component
     :ux/tbody-fragment
     (fn []
       [:tbody
        [:ux/fragment
         [:td "List item 1"]
         [:td "List item 2"]
         [:td "List item 3"]]]))

    (is (= [:div
            [:table
             [:thead
              '([:th "Header 1"]
                [:th "Header 2"]
                [:th "Header 3"])]
             [:tbody
              [:tbody
               '([:td "List item 1"]
                 [:td "List item 2"]
                 [:td "List item 3"])]]]]
           (hc/->hiccup
            [:div
             [:table
              [:thead [:ux/thead-fragment]]
              [:tbody [:ux/tbody-fragment]]]]))))


  (testing ":ux/javascript"

    (hc/clear-components)

    (assert-built-in-component-exists :ux/javascript)

    (let [js (hc/->hiccup [:ux/javascript "alert('Hello world')"])]

      (is (= hiccup.util.RawString (type js)))

      (is (= "alert('Hello world')" (str js))))


    (let [js (hc/->hiccup
              [:ux/javascript
               "document.addEventListener('readystatechange', event => {
                  console.log('Document ready state changed');
                });"])]

      (is (= hiccup.util.RawString (type js)))

      (is (= (str "document.addEventListener('readystatechange', event =>"
                  " { console.log('Document ready state changed'); });")
             (str js))))


    (let [js (hc/->hiccup [:ux/javascript
                           "alert('one');"
                           "alert('two');"
                           "alert('three');"])]

      (is (= hiccup.util.RawString (type js)))

      (is (= "alert('one');alert('two');alert('three');" (str js)))))


  (testing ":ux/execute-javascript"

    (hc/clear-components)

    (assert-built-in-component-exists :ux/execute-javascript)

    (let [[script-tag js] (hc/->hiccup
                           [:ux/execute-javascript "alert('Hello world')"])]

      (is (= :script script-tag))
      (is (= hiccup.util.RawString (type js)))
      (is (= "alert('Hello world')" (str js))))


    (let [[script-tag js] (hc/->hiccup
                           [:ux/execute-javascript
                            "document.addEventListener('readystatechange', event => {
                               console.log('Document ready state changed');
                            });"])]

      (is (= :script script-tag))
      (is (= hiccup.util.RawString (type js)))
      (is (= (str "document.addEventListener('readystatechange', event =>"
                  " { console.log('Document ready state changed'); });")
             (str js))))


    (let [[script-tag js] (hc/->hiccup [:ux/execute-javascript
                                        "alert('one');"
                                        "alert('two');"
                                        "alert('three');"])]

      (is (= :script script-tag))
      (is (= hiccup.util.RawString (type js)))
      (is (= "alert('one');alert('two');alert('three');" (str js)))))


  (testing ":ux/html"

    (hc/clear-components)

    (assert-built-in-component-exists :ux/html)

    (let [html "Hello world <strong>this is a test</strong>"]

      (testing "Escaped by default"

        (is (= "<div>Hello world &lt;strong&gt;this is a test&lt;/strong&gt;</div>"
               (hc/->html [:div html]))))

      (testing "Unescaped"

        (is (= "<div>Hello world <strong>this is a test</strong></div>"
               (hc/->html [:div
                           [:ux/html html]]))))))


  (testing ":ux/css-classes"

    (hc/clear-components)

    (assert-built-in-component-exists :ux/css-classes)

    (let [expected-output [:div {:class "one two three"}
                           "This is a test"]]
      (is (= expected-output
             (hc/->hiccup
              [:div {:class [:ux/css-classes :one.two.three]}
               "This is a test"])))

      (is (= expected-output
             (hc/->hiccup
              [:div {:class [:ux/css-classes :one :two :three]}
               "This is a test"]))))

    (is (= [:div
            {:class "one two three four five six seven eight"}
            "This is a test"]
           (hc/->hiccup
            [:div {:class [:ux/css-classes
                           [:one.two.three]
                           :four
                           nil
                           :five
                           [:six "seven" "eight"]
                           ""
                           nil]}
             "This is a test"])))

    (is (= [:div {:class "bg-red-300 text-blue-500"}
            "This is a test"]
           (hc/->hiccup
            [:div {:class [:ux/css-classes
                           {:bg-colour "red" :text-colour "blue"}
                           :bg-<bg-colour>-300.text-<text-colour>-500]}
             "This is a test"])))

    (is (= [:div {:class "bg-stone-400 text-stone-600 text-white"}
            "This is a test"]
           (hc/->hiccup
            [:div {:class
                   [:ux/css-classes
                    {:colour :stone
                     :text-colour :white
                     :light-number 400
                     :dark-number 600}
                    "bg-stone-{{light-number}} text-stone-{{dark-number}} text-white"]}
             "This is a test"])))


    (is (= [:div
            {:class
             "bg-red-300 text-red-500 bg-red-300 text-red-400 hover:text-red-500"}
            "This is a test"]
           (hc/->hiccup
            [:div {:class
                   [:ux/css-classes
                    {:colour "red" :light-number 300 :darker-number 500}
                    :bg-<colour>-<light-number>.text-<colour>-<darker-number>
                    "bg-{{colour}}-300 text-{{colour}}-400"
                    "hover:text-<colour>-500"]}
             "This is a test"]))))


  (testing ":ux/string-template"

    (hc/clear-components)

    (assert-built-in-component-exists :ux/css-classes)

    (let [expected-output [:div
                           "Your name is Bob Smith and your email address is bobsmith@mailinator.com"]]
      (is
       (= expected-output
          (hc/->hiccup
           [:div
            [:ux/string-template
             {:name "Bob Smith" :email-address "bobsmith@mailinator.com"}
             "Your name is {{name}} and your email address is {{email-address}}"]])))

      (is
       (= expected-output
          (hc/->hiccup
           [:div
            [:ux/string-template
             {:name "Bob Smith" :email-address "bobsmith@mailinator.com"}
             "Your name is {{name}} and"
             "your email address is {{email-address}}"]])))

      (is
       (= expected-output
          (hc/->hiccup
           [:div
            [:ux/string-template
             {:name "Bob Smith" :email-address "bobsmith@mailinator.com"}
             "Your name is"
             "{{name}} and"
             "your email address is {{email-address}}"]])))

      (is
       (= expected-output
          (hc/->hiccup
           [:div
            [:ux/string-template
             {:name "Bob Smith" :email-address "bobsmith@mailinator.com"}
             "Your name is <name> and your email address is <email-address>"]])))))


  (testing ":ux/html-template"

    (hc/clear-components)

    (assert-built-in-component-exists :ux/css-classes)

    (let [expected-content "Your name is <strong>Bob Smith</strong> and your email address is <em>bobsmith@mailinator.com</em>"]

      (let [[div content] (hc/->hiccup
                           [:div
                            [:ux/html-template
                             {:name "Bob Smith" :email-address "bobsmith@mailinator.com"}
                             "Your name is <strong>{{name}}</strong> and your email address is <em>{{email-address}}</em>"]])]

        (is (= :div div))
        (is (= hiccup.util.RawString (type content)))
        (is (= expected-content (str content))))

      (let [[div content] (hc/->hiccup
                           [:div
                            [:ux/html-template
                             {:name "Bob Smith" :email-address "bobsmith@mailinator.com"}
                             "Your name is <strong>{{name}}</strong> and"
                             "your email address is <em>{{email-address}}</em>"]])]

        (is (= :div div))
        (is (= hiccup.util.RawString (type content)))
        (is (= expected-content (str content))))

      (let [[div content] (hc/->hiccup
                           [:div
                            [:ux/html-template
                             {:name "Bob Smith" :email-address "bobsmith@mailinator.com"}
                             "Your name is"
                             "<strong>{{name}}</strong> and"
                             "your email address is <em>{{email-address}}</em>"]])]

        (is (= :div div))
        (is (= hiccup.util.RawString (type content)))
        (is (= expected-content (str content))))

      (let [[div content] (hc/->hiccup
                           [:div
                            [:ux/html-template
                             {:name "Bob Smith" :email-address "bobsmith@mailinator.com"}
                             "Your name is <strong><name></strong> and your email address is <em><email-address></em>"]])]

        (is (= :div div))
        (is (= hiccup.util.RawString (type content)))
        (is (= expected-content (str content) ))))))

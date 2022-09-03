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

  (testing "HTML 5 Doc"
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


  (testing "Fragment"

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


  (testing "Javascript"

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


  (testing "Execute Javascript"

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
      (is (= "alert('one');alert('two');alert('three');" (str js))))))

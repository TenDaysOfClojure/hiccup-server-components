(ns ten-d-c.hiccup-server-components.built-in-components
  (:require [ten-d-c.hiccup-server-components.components :as components]
            [hiccup2.core :as hiccup]))


(components/reg-component
 :ux/html5-doc
 {:doc "An HTML 5 document with the correct DOCTYPE with the given `document`
        representing child elements (e.g head, body) of the <html> tag."

  :example [:ux/html5-doc
            [:head
             [:title "Test HTML document"]]
            [:body
             [:h1 "Hello world"]]]

  :hsc/built-in? true}
 (fn [& document]
   (list
    (hiccup/raw "<!DOCTYPE html>")
    [:html document])))


(components/reg-component
 :ux/fragment

 {:doc
  "Allows for returning multiple elements without the need for a parent element."

  :examples {"Multiple paragraphs"
             [:ux/fragment
              [:p "one"]
              [:p "Two"]
              [:p "Three"]]

             "Multiple list items without `ol` or `ul`"
             [:ux/fragment
              [:li "one"]
              [:li "Two"]
              [:li "Three"]]

             "Multiple table columns"
             [:ux/fragment
              [:td "one"]
              [:td "Two"]
              [:td "Three"]]}

  :hsc/built-in? true}

 (fn [& elements]
   (apply list elements)))


#_(components/reg-component
 :ux/javascript
 {:hsc/built-in? true :example [:ux/fragment
                                [:p "one"]
                                [:p "Two"]
                                [:p "Three"]]}
 ma/javascript)


#_(components/reg-component
 :ux/execute-javascript
 {:hsc/built-in? true}
 (fn [& javascript-lines]
   [:script
    (apply hc/javascript javascript-lines)]))


#_(components/reg-component
 :ux/html
 {:hsc/built-in? true}
 hc/raw-html)


#_(components/reg-component
 :ux/css-classes
 {:hsc/built-in? true}
 hc/css-classes)


#_(components/reg-component
 :ux/string-template
 {:hsc/built-in? true}
 hc/string-template)





#_(hc/->html-file
 "/Users/ghostdog/Desktop/frag.html"
 [:ux/html5-doc
  [:head
   [:title "Hello"]]
  [:body

   [:div {:class [:ux/css-classes
                  {:colour "red"}
                  :bg-<colour>-300.text-<colour>-600
                  :one.two :three :four.five]} "Hi"]
   [:ux/html "Hello <b>WORLD</b> <i>Italics</i>"]
   [:ux/html
    [:ux/string-template
     {:name "FGuilio" :email-address "dasda@ds.net"}
     "Helllo {{name}} your email address is <b>!!email-address!!</b>"]]
   ;;[:ux/execute-javascript "alert('ddd')"]
   [:div
    [:ux/fragment
     [:p "one"]
     [:p "Two"]
     [:p "Three"]]]]])

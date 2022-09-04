(ns ten-d-c.hiccup-server-components.built-in-components
  (:require [ten-d-c.hiccup-server-components.components :as components]
            [ten-d-c.hiccup-server-components.markup-helpers
             :as markup-helpers]))


(components/reg-component
 :ux/html5-doc
 {:doc
  "An HTML 5 document with the correct DOCTYPE with the given `document`
   representing child elements (e.g head, body) of the <html> tag."

  :example [:ux/html5-doc
            [:head
             [:title "Test HTML document"]]
            [:body
             [:h1 "Hello world"]]]

  :hsc/built-in? true}
 (fn [& document]
   (list
    (markup-helpers/raw-html "<!DOCTYPE html>")
    [:html document])))

markup-helpers/raw-html

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


(components/reg-component
 :ux/javascript
 {:doc
  "**Warning:** Never provide strings that come from external sources or user
   defined input as they could include malicious JavaScript and cause cross-site
   scripting (XSS) attacks - those strings should remain escaped.

   Converts provided `javascript-lines` into an unescaped string, allowing the
   javascript to be executed by the browser if wrapped in a <script> tag.

   Used for including executable javascript in Hiccup data."

  :examples {"Single line" [:ux/javascript
                            "alert('Hello world');"]

             "Multiple lines" [:ux/javascript
                               "alert('Hello world');"
                               "alert('Goodbye world');"]}
  :hsc/built-in? true}
 markup-helpers/javascript)


(components/reg-component
 :ux/execute-javascript
 {:doc
  "**Warning:** Never provide strings that come from external sources or user
   defined input as they could include malicious JavaScript and cause cross-site
   scripting (XSS) attacks - those strings should remain escaped.

   Converts provided `javascript-lines` into an unescaped string and wraps
   it in a <script> tag, executing the javascript in the web browser.

   Used for including executable javascript in Hiccup data."

  :examples {"Single line" [:ux/javascript
                            "alert('Hello world');"]

             "Multiple lines" [:ux/javascript
                               "alert('Hello world');"
                               "alert('Goodbye world');"]}

  :hsc/built-in? true}
 (fn [& javascript-lines]
   [:script
    (apply markup-helpers/javascript javascript-lines)]))


(components/reg-component
 :ux/html
 {:doc
  "**Warning:** Never provide strings that come from external sources or user
   defined input as they could include malicious JavaScript and cause cross-site
   scripting (XSS) attacks - those strings should remain escaped.

   Since by default all strings are escaped, this component converts the provided
   `html` into an unescaped string, allowing the HTML to be rendered by the
   browser.

   Used for including strings that contain HTML markup that should not be
   escaped and should be rendered by the browser."

  :example [:ux/html
            "This is a <strong>test</strong> with <em>embeded html</em>."]

  :hsc/built-in? true}
 markup-helpers/raw-html)


(components/reg-component
 :ux/css-classes
 {:doc
  "Constructs a list of css classes that can be used as the \"class\" attribute
   of HTML elements.

   Optionally, if the first parameter is a map, it will be used for variable
   substitution in subsequent parameters which represent css classes.

   Variable substitution supports the same options as the [[string-template]]
   function.

   The variable list of css classes provided as subsequent parameters can be
   keywords (single or period seperated) or strings (space, comma or period
   seperated) and result in a consistent, space separated string that represents
   the css classes of an HTML element.

   Ignores nils and blank strings allowing for conditional construction of
   css classes."

  :examples {"Space seperated keywords"
             [:div
              {:class [:ux/css-classes :one :two :three]}
              "This is a test"]

             "Dot seperated keywords"
             [:div
              {:class [:ux/css-classes :one.two.three.four]}
              "This is a test"]

             "Space seperated strings"
             [:div
              {:class [:ux/css-classes "one two three"]}
              "This is a test"]

             "Dot seperated strings"
             [:div
              {:class [:ux/css-classes "one.two.three.four"]}
              "This is a test"]

             "Multiple strings"
             [:div
              {:class [:ux/css-classes
                       "one" "two" "three"]}
              "This is a test"]

             "Multiple dot seperated keywords"
             [:div
              {:class [:ux/css-classes
                       :one.two.three.four
                       :five.six.seven]}
              "This is a test"]


             "Variable substituion with keywords"
             [:div
              {:class [:ux/css-classes
                       {:colour "red"}
                       :bg-<colour>-300.text-<colour>-600]}
              "This is a test"]

             "Variable substituion with strings"
             [:div
              {:class [:ux/css-classes
                       {:colour "blue"}
                       "bg-{{colour}}-300 text-{{colour}}-600 shadow-{{colour}}"]}
              "This is a test"]}

  :hsc/built-in? true}
 markup-helpers/css-classes)


(components/reg-component
 :ux/string-template
 {:doc
  "Returns a string where interpolated variables are replaced using values in
  the map provided by `variables` allowing for templated strings.

  Interpolated variables can be tags enclosed as follows (where `my-value` is
  the name of the variable to replace):

  - `\"Hello {{my-value}}\"`
  - `\"Hello {my-value}\"`
  - `\"Hello <<my-value>>\"`
  - `\"Hello <my-value>\"`
  - `\"Hello !!my-value!!\"`
  - `\"Hello !my-value!\"`
  - `\"Hello $$my-value$$\"`
  - `\"Hello $my-value$\"`"

  :example [:ux/string-template
            {:name "Bob" :email-address "bobsmith@mailinator.com"}
            "Hello {{name}}, your email address is {{email-address}}"]

  :hsc/built-in? true}
 markup-helpers/string-template)


(components/reg-component
 :ux/html-template
 {:doc ""

  :examples {"Single template string"
             [:ux/html-template
              {:name "Bob Smith" :email-address "bobsmith@mailinator.com"}
              "Your name is <strong>{{name}}</strong> and your email address is <em>{{email-address}}</em>"]

             "Variable template string"
             [:div
              [:ux/html-template
               {:name "Bob Smith" :email-address "bobsmith@mailinator.com"}
               "Your name is <strong>{{name}}</strong> and"
               "your email address is <em>{{email-address}}</em>"]]}

  :hsc/built-in? true}
 markup-helpers/html-template)

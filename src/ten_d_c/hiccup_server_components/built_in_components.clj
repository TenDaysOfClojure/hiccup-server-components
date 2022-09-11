(ns ^:no-doc ten-d-c.hiccup-server-components.built-in-components
  (:require [ten-d-c.hiccup-server-components.components :as components]
            [ten-d-c.hiccup-server-components.markup-helpers
             :as markup-helpers]))


(components/reg-component
 :ux/html5-doc
 {:doc
  "Constructs a HTML5 document with the correct DOCTYPE using the given `document`
   as child elements (e.g head, body) of the &lt;html> tag."

  :arglists '([& document])


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


(components/reg-component
 :ux/fragment

 {:doc
  "Allows for returning multiple child elements without the need for a
   parent element."

  :arglists '([& elements])

  :examples {"Multiple paragraphs"
             [:ux/fragment
              [:p "one"]
              [:p "Two"]
              [:p "Three"]]

             "Multiple list items without parent `ol` or `ul`"
             [:ux/fragment
              [:li "one"]
              [:li "Two"]
              [:li "Three"]]

             "Multiple table cells"
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
  "Used for including unescaped, executable javascript in Hiccup data.

   Converts provided `javascript-lines` (one or more strings) into an unescaped
   string, allowing the javascript to be executed by the browser if wrapped
   in a &lt;script> tag.

   **Warning:** Never provide strings that come from external sources or user
   defined input as they could include malicious JavaScript and cause cross-site
   scripting (XSS) attacks - those strings should remain escaped."

  :arglists '([& javascript-lines])

  :examples {"Single line" [:ux/javascript
                            "alert('Hello world');"]

             "Multiple lines" [:ux/javascript
                               "alert('Hello world');"
                               "alert('Goodbye world');"]

             "Confirm example"
             [:ux/javascript
              "if (confirm('Press a button!') == true) {
    alert('You pressed OK!);
  } else {
    alert('You canceled!');
  }"]}
  :hsc/built-in? true}
 markup-helpers/javascript)


(components/reg-component
 :ux/javascript-tag
 {:doc
  "Wraps the supplied `javascript-lines` in a &lt;script> tag which executes the javascript in the
   web browser.

   **Warning:** Never provide strings that come from external sources or user
   defined input as they could include malicious JavaScript and cause cross-site
   scripting (XSS) attacks - those strings should remain escaped."

  :arglists '([& javascript-lines])

  :examples {"Single line" [:ux/javascript-tag
                            "alert('Hello world');"]

             "Multiple lines" [:ux/javascript-tag
                               "alert('Hello world');"
                               "alert('Goodbye world');"]}

  :hsc/built-in? true}
 (fn [& javascript-lines]
   [:script
    (apply markup-helpers/javascript javascript-lines)]))


(components/reg-component
 :ux/html
 {:doc
  "Used for including strings that contain HTML markup, this component converts
   the provided `html` into an unescaped string, allowing the HTML to be
   rendered by the browser.

   **Warning:** Never provide strings that come from external sources or user
   defined input as they could include malicious JavaScript and cause cross-site
   scripting (XSS) attacks - those strings should remain escaped."

  :arglists '([& html])

  :examples {"Single HTML string"
             [:ux/html
              "This is a <strong>test</strong> with <em>embeded html</em>."]

             "Multiple HTML strings"
             [:ux/html
              "This is a <strong>test</strong> with <em>embeded html</em>."
              "Another line with a <h1>Header element</h1> and link"
              "<a href=\"https://example.com\">Test link</a>"]}

  :hsc/built-in? true}
 markup-helpers/raw-html)


(components/reg-component
 :ux/css-classes
 {:doc
  "Constructs a list of one or more css classes that can be used as the
   \"class\" attribute of HTML elements.

   Optionally, if the first parameter (`variable-substitution-map`) is a map,
   it will be used for variable substitution in subsequent parameters which
   represent css classes.

   Variable substitution supports the same options as the :ux/string-template
   component.

   The variable list of css classes provided can be keywords (single or
   period seperated) or strings (space, comma or period seperated) and result
   in a consistent, space separated string that represents the css classes of
   an HTML element.

   Ignores nils and blank strings allowing for conditional construction of
   css classes."

  :arglists '([& css-classes]
              [variable-substitution-map & css-classes])

  :examples {"Space seperated keywords"
             [:div
              {:class [:ux/css-classes :one :two :three]}
              "This is a test"]

             "Dot seperated keywords"
             [:div
              {:class [:ux/css-classes :one.two.three.four]}
              "This is a test"]

             "Conditional css classes"
             '(let [selected?          true
                    confirming-delete? true]
                [:a
                 {:class [:ux/css-classes
                          :sidebar-link
                          :menu-link
                          (when selected? :active)
                          (when confirming-delete? :danger-link)]}
                 "This is a test"])

             "Ignores nils and blank strings"
             [:a
              {:class [:ux/css-classes
                       :sidebar-link :menu-link
                       nil
                       "" " " "   "
                       :site-link]}
              "This is a test"]

             "Space seperated strings"
             [:div
              {:class [:ux/css-classes "one two three"]}
              "This is a test"]

             "Dot seperated strings"
             [:div
              {:class [:ux/css-classes "one.two.three.four"]}
              "This is a test"]

             "Comma seperated strings"
             [:div
              {:class [:ux/css-classes "one, two, three, four"]}
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
   the map provided by `variable-substitution-map` allowing for templated strings.

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

  :arglists '([variable-substitution-map & string-template])

  :examples {"Single template string"
             [:ux/string-template
              {:name "Bob" :email-address "bobsmith@mailinator.com"}
              "Hello {{name}}, your email address is {{email-address}}."]

             "Variable template string"
             [:ux/string-template
              {:name "Bob" :email-address "bobsmith@mailinator.com"}
              "Hello {{name}}, your "
              "email address is {{email-address}}."]}

  :hsc/built-in? true}
 markup-helpers/string-template)


(components/reg-component
 :ux/html-template
 {:doc
  "**Warning:** Never provide strings that come from external sources or user
   defined input as they could include malicious JavaScript and cause cross-site
   scripting (XSS) attacks - those strings should remain escaped.

   Returns an unescaped HTML string where interpolated variables are replaced using values in
   the map provided by `variable-substitution-map` allowing for templated HTML strings.

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

  :arglists '([variable-substitution-map & html-string-template])

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


(components/reg-component
 :ux/style-tag
 {:doc
  "A &lt;style> tag used to define style information (CSS) for a document."

  :examples
  {"Single line"
   [:ux/style-tag
    ".danger {color: red;}
.success {color: green;}"]

   "Multiple lines"
   [:ux/style-tag
    ".danger {color: red;}"
    ".success {color: green;}"
    ".info {color: blue;}"]}

  :hsc/built-in? true}
 (fn [& lines]
   [:style
    [:ux/html lines]]))

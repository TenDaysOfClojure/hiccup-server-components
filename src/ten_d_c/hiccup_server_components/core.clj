(ns ten-d-c.hiccup-server-components.core
  "A server-side rendering (SSR) library for Clojure web applications that
  facilitates **defining**, **composing**, **organising**, and **unit testing**
  user interface components, as well as **generating the associated HTML**.
  Based on the [Hiccup library](https://github.com/weavejester/hiccup)

  Components represent modular, abstract pieces of the user interface which
  are composed into a larger, complex applications with a high degree of
  abstraction.

  Can be used seamlessly with HTTP routing libraries such as
  [Reitit](https://github.com/metosin/reitit),
  [Compojure](https://github.com/weavejester/compojure), and directly with
  various [Clojure ring implementations](https://github.com/ring-clojure/ring)
  for generating HTML responses. Can also be used to generate static HTML files.

  See [full README](https://github.com/TenDaysOfClojure/hiccup-server-components/blob/master/README.md)
  for examples and further details."
  (:require [ten-d-c.hiccup-server-components.components :as components]
            [ten-d-c.hiccup-server-components.compiler :as compiler]
            [clojure.string :as string]
            [hiccup2.core :as hiccup]
            [ten-d-c.hiccup-server-components.component-stats :as component-stats]
            [clojure.string :as str]
            [ten-d-c.hiccup-server-components.http-server :as http-server]
            [ten-d-c.hiccup-server-components.markup-helpers :as markup-helpers])
  (:gen-class))


(defn ^:no-doc -main [& args])


;; -- Public API --

(defn all-components
  "Lists all registered components and their associated metadata"
  []
  (components/all-components))


(defn clear-components
  "Clears all components that where registered using `->reg-component`"
  []
  (components/clear-components))


(defn get-component-meta-data
  "Gets component metadata that was associated with the component when
  registered via `reg-component`"
  [element-name]
  (components/get-meta-data element-name))


(defn get-component-docs
  "Gets component documentation (a map including a `doc` and `example` / `examples`
   key if availible) from it's metadata that was associated with the component
   when registered via `reg-component`"
  [element-name]
  (components/get-docs element-name))


(defn reg-component
  "Registers a component with the given `element-name` (must be a qualified
   keyword e.g `:my-components/login-form`) where the given `component` can be
   either a pure function that returns Hiccup data, a vector (representing
   Hiccup data) or a string.

   Once a component is registered it can be referenced in Hiccup data by its
   qualified keyword.

   Supported parameters:

   - `element-name` (Required): The element name (must be a qualified keyword
     e.g :my-components/login-form) that describes the component e.g.
     `:ux.elements/ordered-list`, `:ux.elements/unordered-list`.

   - `component-meta-data` (Optional): A clojure map representing metadata of
                           the component. Should include the following keys:

       - `:doc`: A Docstring describing the component as well as its parameters.

       - `:example`: Hiccup data representing a single example of referencing
                     the component.

       - `:examples`: Allows for providing multiple examples, should be a map
                      with the key being the description of the example and the
                      value being Hiccup data representing an example of
                      referencing the component.

   - `component` (Required): Can be either a pure function that returns Hiccup
                 data, a vector (representing Hiccup data) or a string.

   Example:

   ```clojure
   (hc/reg-component
    ;; Keyword to uniquely identify the component:
    :ux.layouts/html-doc

    ;; Include meta data in the form of a map including `doc` and `examples` keys:
    {:doc
     \"The main HTML document including a HEAD (with required CSS and Javascript
       included) and BODY section.

       This component is the basis for all top-level pages in the application.

       The first parameter (a map) represents the component's options, followed by
       a variable list of `child-elements` in the form of Hiccup data that will be
       placed in the BODY.

       Component options:

       - `title`: The title of the HTML document (will populate the title tag
          in the HEAD of the document)\"

     :examples {\"With single child element\"
                [:ux.layouts/html-doc
                 {:title \"One child element\"}
                 [:div \"Hello world\"]]

                \"With multiple child elements\"
                [:ux.layouts/html-doc
                 {:title \"Multiple child element\"}
                 [:h1 \"Hello world\"]
                 [:p \"This is a test\"]
                 [:a {:href \"/search\"} \"Try searching for more results\"]]}}

    ;; Pure function implementing the responsibilities of the component:
    (fn [{:keys [title] :as options} & child-elements]
      [:html
       [:head
        [:meta {:charset \"UTF-8\"}]
        [:meta {:content \"width=device-width, initial-scale=1.0\"
                :name \"viewport\"}]
        ;; Include application CSS and any javascript
        [:link {:rel \"stylesheet\" :href \"/css/main.css\"}]
        [:script {:src \"/js/app-bundle.js\"}]
        ;; Include the title of the document
        [:title title]]
       ;; Variable child elements included in body element
       [:body child-elements]]))
   ```"
  ([element-name component-meta-data component]

   (components/reg-component element-name
                             component-meta-data
                             component))

  ([element-name component]
   (reg-component element-name {} component)))


(defn ->hiccup
  "Processes components in the provided `hiccup-data` (vectors describing HTML)
   and returns expanded Hiccup data (vectors describing HTML).

  - `hiccup-data`: The hiccup data (vectors describing HTML) to process which
                   includes references to components.

  - `local-components`: (Optional) A map of component configuration. Components
                        defined here will overwrite components registered with
                        `->reg-component` for this function call only."
  ([hiccup-data local-components]
   (compiler/->hiccup hiccup-data local-components))

  ([hiccup-data]
   (compiler/->hiccup hiccup-data {})))


(defn ->html
  "Takes `hiccup-data` (vectors describing HTML), that can include component
   references, and returns the generated HTML.

  - `hiccup-data`: The hiccup data (vectors describing HTML) to process which
                   may include references to components.

  - `local-components`: (Optional) A map of component configuration. Components
                        defined here will overwrite components registered with
                        `->reg-component` for this function call only."
  ([hiccup-data local-components]
   (compiler/->html hiccup-data local-components))

  ([hiccup-data] (compiler/->html hiccup-data)))


(defn ->html-file
  "Takes a `file-path` and `hiccup-data` (vectors describing HTML), that can
  include component references, and saves the generated HTML to the given `file-path`.

  - `file-path`: The file path to save the outputed HTML to.

  - `hiccup-data`: The hiccup data (vectors describing HTML) to process which
                   may include references to components.

  - `local-components`: (Optional) A map of component configuration. Components
                        defined here will overwrite components registered with
                        `->reg-component` for that function call only."
  ([file-path hiccup-data]
   (compiler/->html-file file-path hiccup-data))

  ([file-path hiccup-data local-components]
   (compiler/->html-file file-path hiccup-data local-components)))


(defn component->html
  "Generates and returns HTML of a component with the given `component-element-name`.

  Useful for generating the HTML of top-level web pages granted they are
  registered as component.

  - `component-element-name`: The qualified keyword of the component element name.

  - `params`: (Optional) The params to pass to the component if it has parameters.

  - `local-components`: (Optional) A map of component configuration. Components
                        defined here will overwrite components registered with
                        `->reg-component` for that function call only."
  ([component-element-name]
   (component->html component-element-name nil {}))

  ([component-element-name params]
   (component->html component-element-name params {}))

  ([component-element-name params local-components]
   (compiler/component->html
    component-element-name params local-components)))


(defn component->html-file
  "Generates the HTML of a component with the given `component-element-name` and
  saves the output to the given `file-path`.

  Useful for generating HTML files of top-level web pages granted they are
  registered as component.

  - `file-path`: The file path to save the outputed HTML to.

  - `component-element-name`: The qualified keyword of the component element name.

  - `params`: (Optional) The params to pass to the component if it has parameters.

  - `local-components`: (Optional) A map of component configuration. Components
                        defined here will overwrite components registered with
                        `->reg-component` for that function call only."
  ([file-path component-element-name]
   (component->html-file file-path component-element-name nil {}))

  ([file-path component-element-name params]
   (component->html-file file-path component-element-name params {}))

  ([file-path component-element-name params local-components]
   (compiler/component->html-file
    file-path component-element-name params local-components)))


(defn component-counts
  "Provides a summary for component counts grouped by component namespace

  Returns a list of maps that include a `component-namespace` and
  `total-components` key:

  ```
  ({:component-namespace \"ux.components\", :total-components 2}
   {:component-namespace \"ux.layouts\", :total-components 2})
  ```"
  ([] (component-stats/component-counts))

  ([all-components]
   (component-stats/component-counts all-components)))


 (defn raw-html
   "Converts provided `html` into an unescaped string, allowing the HTML to be
   rendered by the browser. Used for including strings that contain HTML markup
   that should not be escaped."
   [& html]
   (hiccup/raw (string/join html)))


 (defn javascript
   "Converts provided `javascript` into an unescaped string, allowing the
   javascript to be executed by the browser. Used for including executable
   javascript in Hiccup data."
   [& javascript]
   (hiccup/raw (string/join javascript)))


(defn wrap-response-middleware
  "Ring middleware that will generate HTML using Hiccup server components conventions
   and set the `:body` of the response to the generated HTML.

   Works with [Compojure](https://github.com/weavejester/compojure) and
   [Reitit](https://github.com/metosin/reitit) routing libraries as well
   as Ring compatible HTTP servers.

   HTTP route handlers configured with this middleware can return a map including
   the following keys which will result in HTML being set on the response body:

   - `:hsc/component`: The qualified keyword of the component to use to generate
                       HTML that will be set as the `:body` of the response.
                       Component params can be supplied with the `:hsc/params`
                       key.

   - `:hsc/params` (Optional): Used in conjunction with the `:hsc/component` key,
                   represents params that will be passed to the component.

   - `:hsc/html`: Hiccup data (vectors describing HTML), that can include
                  component references, that will be used to generate HTML that
                  will be set as the `:body` of the response.

  Example of [Compojure](https://github.com/weavejester/compojure) routes with
  middleware configured:

  ```clojure
  (ns http-routing.compojure
    (:require [compojure.core :refer :all]
              [ten-d-c.hiccup-server-components.core :as hc]))

  (compojure.core/defroutes app

  ;; Generates and returns the HTML for the `:ux.pages/home` component, no
  ;; component params are provided.
  (GET \"/\" []
       {:hsc/component :ux.pages/home})


  ;; Generates and returns the HTML for the `:ux.pages/dashboard` component,
  ;; passing the component the `hsc/params` key as params.
  (GET \"/dashboard\" []
       {:hsc/component :ux.pages/dashboard
        :hsc/params {:username \"bobsmith\"
                     :email-address \"bobsmith@somemail.net\"}})


  ;; Generates and returns the HTML from Hiccup data (which can include
  ;; component refererences) in the `:hsc/html` key.
  (GET \"/testing\" []
       {:hsc/html [:ux.layouts/html-doc {:title \"A test page\"}
                   [:div
                    [:h1.text-3xl \"Hello world From HTML\"]
                    [:p \"This is a test\"]]]}))


  (def web-app (-> app
                   (hc/wrap-response-middleware)))
  ```

  Example of [Reitit](https://github.com/metosin/reitit) routes with
  middleware configured:

  ```clojure
  (ns http-routing.reitit
    (:require [reitit.ring :as ring]
              [ten-d-c.hiccup-server-components.core :as hc]))


  (def web-app
    (ring/ring-handler
     (ring/router
      [[\"/\" {:handler (fn [_]
                        {:hsc/component :ux.pages/home})}]

       [\"/dashboard\"
        {:get
         {:handler
          (fn [_]
            {:hsc/component :ux.pages/dashboard
             :hsc/params {:username \"bobsmith\"
                          :email-address \"bobsmith@somemail.net\"}})}}]

       [\"/testing\"
        {:get
         {:handler
          (fn [_]
            {:hsc/html [:ux.layouts/html-doc {:title \"A test page\"}
                        [:div
                         [:h1.text-3xl \"Hello world From HTML\"]
                         [:p \"This is a test\"]]]})}}]]

      {:data {:middleware [hc/wrap-response-middleware]
              :enable true}})))
  ```"
  [request]
  (http-server/wrap-response-middleware request))


(defn css-classes
  "Constructs a list of css classes that can be used as the \"class\" attribute
  of HTML elements.

  Optionally, if the first parameter is a map, it will be used for variable
  substitution in subsequent parameters which represent css classes.

  Variable substitution supports the same options as the [[string-template]]
  function.

  Example with variable substitution:

  ```clojure
  ;; `{{colour}}` will be substituted with \"red\"
  (css-classes {:colour \"red\"}
               \"bg-{{colour}}-400 text-{{colour}}-600\")

  ;; => \"bg-red-400 text-red-600\"


  ;; `<colour>` will be substituted with \"red\"
  (css-classes {:colour \"red\"}
               :bg-<colour>-400.text-<colour>-600)

  ;; => \"bg-red-400 text-red-600\"
  ```

 Example without variable substitution:

  ```clojure
  ;; `{{colour}}` will be substituted with \"red\"
  (css-classes \"bg-red-400\" \"text-red-600\")

  ;; => \"bg-red-400 text-red-600\"

  ;; `<colour>` will be substituted with \"red\"
  (css-classes :bg-red-400.text-red-600)

  ;; => \"bg-red-400 text-red-600\"
  ```

  The variable list of css classes provided as subsequent parameters can be
  keywords (single or period seperated) or strings (space, comma or period
  seperated) and result in a consistent, space seperated string that
  represents the css classes of an HTML element.

  Ignores nils and blank strings allowing for conditional contruction of
  css classes.

  Examples:

  ```clojure
  ;; Dot seperated keyword
  (css-classes :one.two.three) ;; => \"one two three\"

   (css-classes :one.two.three
                :four.five
                \"six seven\") ;; => \"one two three four five six seven\"

  ;; Dot seperated string
  (css-classes \"one.two.three\") ;; => \"one two three\"

  ;; Dot seperated string
  (css-classes \"one.two.three\") ;; => \"one two three\"

  ;; Comma seperated string
  (css-classes \"one,two,three\") ;; => \"one two three\"

  ;; Keywords
  (css-classes :one :two :three) ;; => \"one two three\"

  ;; Strings
  (css-classes \"one\" \"two\" \"three\") ;; => \"one two three\"

  ;; Nils are ignored allowing for conditional statements
  (css-classes :one
               :two
               :three
               ;; returns nil
               (when (= 1 2) :four)) ;; => \"one two three\"
  ```

  Example usage with Hiccup data:
  ```clojure
  [:div {:class (css-classes :one.two.three :four :five)}
    \"Hello world\"]

  ;; => \"<div class=\"one two three four five\">Hello world</div>\"

  ;; Conditional css classes

  (let [error?  true
        message \"This is a test\"]
    [:div {:class (css-classes :alert
                               (if error?
                                 :error-alert
                                 :info-alert))}
      message])

  ;; => \"<div class=\"alert error-alert\">This is a test</div>\"
  ```"
  [& options-and-classes]
  (apply markup-helpers/css-classes options-and-classes))


(defn string-template
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
  - `\"Hello $my-value$\"`

  Examples:

  ```clojure
  ;; All examples below result in the following string being returned:

  ;; =>> \"Hello Bob Smith your email address is bobsmith@mailinator.com\"]

  (string-template
    {:name \"Bob Smith\" :email-address \"bobsmith@mailinator.com\"}
    \"Hello {{name}} your email address is {{email-address}}\")

  (string-template
    {:name \"Bob Smith\" :email-address \"bobsmith@mailinator.com\"}
    \"Hello {name} your email address is {email-address}\")

  (string-template
    {:name \"Bob Smith\" :email-address \"bobsmith@mailinator.com\"}
    \"Hello <<name>> your email address is <<email-address>>\")

  (string-template
    {:name \"Bob Smith\" :email-address \"bobsmith@mailinator.com\"}
    \"Hello <name> your email address is <email-address>\")

  (string-template
    {:name \"Bob Smith\" :email-address \"bobsmith@mailinator.com\"}
    \"Hello !!name!! your email address is !!email-address!!\")

  (string-template
    {:name \"Bob Smith\" :email-address \"bobsmith@mailinator.com\"}
    \"Hello !name! your email address is !email-address!\")

  (string-template
    {:name \"Bob Smith\" :email-address \"bobsmith@mailinator.com\"}
    \"Hello $$name$$ your email address is $$email-address$$\")

  (string-template
    {:name \"Bob Smith\" :email-address \"bobsmith@mailinator.com\"}
    \"Hello $name$ your email address is $email-address$\"))
  ```"
  [variables string-template]
  (markup-helpers/string-template variables string-template))

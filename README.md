[![Clojars Project](https://img.shields.io/clojars/v/net.clojars.t_d_c/hiccup-server-components.svg)](https://clojars.org/net.clojars.t_d_c/hiccup-server-components)

# Hiccup Server Components

A server-side rendering (SSR) library for Clojure web applications that facilitates **defining**, **composing**, **organising**, and **unit testing** user interface components, as well as **generating the associated HTML**. Based on the [Hiccup library](https://github.com/weavejester/hiccup)

The goal of this library is to **facilitate rapid web application development** and **increase maintainability of user interface code** by providing conventions and tools to model user interfaces.

**Components** represent **modular**, **abstract pieces of the user interface** which are **composed into a larger**, **complex applications** with a high degree of abstraction.

Can be used seamlessly with HTTP routing libraries such as [Reitit](https://github.com/metosin/reitit), [Compojure](https://github.com/weavejester/compojure), and directly with various [Clojure ring implementations](https://github.com/ring-clojure/ring) for generating HTML responses. Can also be used to generate static HTML files.

# Table of contents

- [Installation](#installation)
- [Introduction](#introduction)
- [Getting started: Building an example login page](#getting-started-building-an-example-login-page)
- [Defining components](#defining-components)
- [Composing components](#composing-components)
- [Organising components](#organising-components)
- [Generating HTML](#generating-html)
- [HTTP routing middleware](#http-routing-middleware)
- [Full API documentation](https://tendaysofclojure.github.io/hiccup-server-components-api-docs/ten-d-c.hiccup-server-components.core.html)

# Installation

[![Clojars Project](https://img.shields.io/clojars/v/net.clojars.t_d_c/hiccup-server-components.svg)](https://clojars.org/net.clojars.t_d_c/hiccup-server-components)

Add the following dependancy to your Clojure projects to get the latest version:

#### Clojure CLI/deps.edn:

```clojure
net.clojars.t_d_c/hiccup-server-components {:mvn/version "0.16.0"}
```

#### Leiningen/Boot:

```clojure
[net.clojars.t_d_c/hiccup-server-components "0.16.0"]
```

[back to top](#table-of-contents)

# Introduction

### Using Hiccup to represent HTML

The [Hiccup library](https://github.com/weavejester/hiccup) is used for representing HTML in Clojure. It uses vectors to represent elements, and maps to represent an element's attributes.

The below Hiccup data represents the HTML for a typical login webpage:

```Clojure
;; HTML document
[:html
 [:head
  [:meta {:charset "UTF-8"}]
  [:meta {:content "width=device-width, initial-scale=1.0", :name "viewport"}]

  ;; Include CSS and javascript assets
  [:link {:rel "stylesheet", :href "/css/main.css"}]
  [:script {:src "/js/app-bundle.js"}]
  [:title "Login now"]]
 [:body

  ;; HTML form which posts to /login
  [:form.input-form {:action "/login" :method "POST"}
   [:h1.form-title "Login now"]
   [:p.form-intro-text
    "Enter your email address and password to access your personal dashboard"]

   ;; Email input field with label
   [:div.form-field
    [:label.form-label {:for "email-address"} "Email address"]
    [:input.text-input
     {:id "email-address",
      :name "email-address",
      :autofocus true,
      :type "text"}]]

   ;; Password field with label
   [:div.form-field
    [:label.form-label {:for "password"} "Password"]
    [:input.text-input
     {:id "password", :name "password", :autofocus false, :type "password"}]]

   ;; Form action buttons included primary button and cancel button
   [:div.form-action-buttons
    [:button.primary-submit-button {:type "submit"} "Login now"]

    ;; Cancel button navigates back to '/home' using javascript
    [:button.cancel-button
     {:type "button", :onclick "javascript:window.location='/home'"}
     "Cancel"]]]]]
```

### Further abstraction with Hiccup server components

Hiccup server components allows developers to represent web pages and user interface components with a high level of abstraction, leveraging the [Hiccup library](https://github.com/weavejester/hiccup).

 Using Hiccup server components We can represent the typical login webpage as follows:

```clojure
[:ux.layouts/html-doc {:title "Login now"}

  [:ux.forms/form
   {:title "Login now"
    :intro-text "Enter your email address and password to access your personal dashboard"
    :action "/login"}

   [:ux.forms/text-input
    {:label-text "Email address"
     :element-name "email-address"
     :auto-focus? true}]

   [:ux.forms/text-input
    {:label-text "Password"
     :element-name "password"
     :input-type "password"}]

   [:ux.forms/action-buttons
    [:ux.forms/primary-submit-button "Login now"]
    [:ux.forms/cancel-button "Back" "/home"]]]]

```

Which would produce the following web page:

![](https://tendaysofclojure.github.io/hiccup-server-components-api-docs/images/login-screen.png)

With the following HTML:

```html
<html>
  <head>
      <meta charset="UTF-8"/>
      <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
      <link href="/css/main.css" rel="stylesheet"/>
      <script src="/js/app-bundle.js"></script>
      <title>Login now</title>
  </head>
  <body>
      <form action="/login" method="POST" class="input-form">
          <h1 class="form-title">Login now</h1>
          <p class="form-intro-text">
            Enter your email address and password to access your personal
            dashboard.
          </p>
          <div class="form-field">
              <label class="form-label" for="email-address">Email address</label>
              <input autofocus="autofocus" class="text-input"
                     id="email-address"
                     name="email-address" type="text"/>
          </div>
          <div class="form-field">
              <label class="form-label" for="password">Password</label>
              <input class="text-input" id="password" name="password"
                  type="password"/>
          </div>
          <div class="form-action-buttons">
              <button class="primary-submit-button" type="submit">Login now</button>
              <button class="cancel-button"
                      onclick="javascript:window.location=&apos;/home&apos;"
                      type="button">Cancel</button>
          </div>
      </form>
  </body>
</html>
```

[back to top](#table-of-contents)

# Getting started: Building an example login page

The goal of this introductory example is to demonstrate 95% of the features provided by this library. A basic understanding of the [Hiccup library](https://github.com/weavejester/hiccup) is a prerequisite.

The following example shows **how to build a simple login page**.

### Step 1: Requiring the namespace

The public API for Hiccup Server Components is provided by the `ten-d-c.hiccup-server-components.core` namespace.

The first step, after including the library in your project, is to require the namespace:

```clojure
(ns introductory-example.core
  (:require [ten-d-c.hiccup-server-components.core :as hc]))
```

### Step 2: Register your first component

The first component we'll register is an HTML document which involves the following steps:

 - Using the [`reg-component` function](https://tendaysofclojure.github.io/hiccup-server-components-api-docs/ten-d-c.hiccup-server-components.core.html#var-reg-component) to register a new component using a qualified keyword.

 - Include `doc` and `examples` metadata (in the form of a clojure map) when registering the component to document the component.

 - Implement the components responsibilities using a pure function.

```clojure
(hc/reg-component
 ;; Keyword to uniquely identify the component:
 :ux.layouts/html-doc

 ;; Include meta data in the form of a map including `doc` and `examples` keys:
 {:doc
  "The main HTML document including a HEAD (with required CSS and Javascript
    included) and BODY section.

    This component is the basis for all top-level pages in the application.

    The first parameter (a map) represents the component's options, followed by
    a variable list of `child-elements` in the form of Hiccup data that will be
    placed in the BODY.

    Component options:

    - `title`: The title of the HTML document (will populate the title tag
       in the HEAD of the document)"

  :examples {"With single child element"
             [:ux.layouts/html-doc
              {:title "One child element"}
              [:div "Hello world"]]

             "With multiple child elements"
             [:ux.layouts/html-doc
              {:title "Multiple child element"}
              [:h1 "Hello world"]
              [:p "This is a test"]
              [:a {:href "/search"} "Try searching for more results"]]}}

 ;; Pure function implementing the responsibilities of the component:
 (fn [{:keys [title] :as options} & child-elements]
   [:html
    [:head
     [:meta {:charset "UTF-8"}]
     [:meta {:content "width=device-width, initial-scale=1.0"
             :name "viewport"}]
     ;; Include application CSS and any javascript
     [:link {:rel "stylesheet" :href "/css/main.css"}]
     [:script {:src "/js/app-bundle.js"}]
     ;; Include the title of the document
     [:title title]]
    ;; Variable child elements included in body element
    [:body child-elements]]))
```

The [`->html` function](https://tendaysofclojure.github.io/hiccup-server-components-api-docs/ten-d-c.hiccup-server-components.core.html#var--.3Ehtml) can be used to convert Hiccup data, which references the component using its qualified keyword, to HTML:

```clojure
(hc/->html
 [:ux.layouts/html-doc
  {:title "Multiple child element"}
  [:h1 "Hello world"]
  [:p "This is a test"]
  [:a {:href "/search"} "Try searching for more results"]])
```

The following HTML is returned:

```html
<html>
<head>
    <meta charset="UTF-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <link href="/css/main.css" rel="stylesheet"/>
    <script src="/js/app-bundle.js"></script>
    <title>Multiple child element</title>
</head>
<body>
    <h1>Hello world</h1>
    <p>This is a test</p>
    <a href="/search">Try searching for more results</a>
</body>
</html>
```

### Step 3: Register components related to HTML forms

The next step is to register components that will be responsible for the HTML form which involves the following steps:

- Using the [`reg-component` function](https://tendaysofclojure.github.io/hiccup-server-components-api-docs/ten-d-c.hiccup-server-components.core.html#var-reg-component) to register a new component using a qualified keyword.

- Including `doc` and `example` metadata (in the form of a clojure map) when registering the component to document the component.

- Using a pure function, implement the components responsibilities that include:

  - Building a HTML form with an `action` url

  - Includes a standardised `title` and `intro-text` which enrich the all forms with text prompts.

  - Allows for a variable amount of form elements which will be child elements of the form.


```clojure
(hc/reg-component
 ;; Keyword to uniquely identify the component:
 :ux.forms/form

 ;; Include meta data:
 {:doc
  "Builds an HTML form which will perform an HTTP POST to the given `action` URL
   and provides a standardised `title`, optional `intro-text` which will be
   displayed before form elements.

   Along with the above options supports a variable amount of form elements.

  Component options:

  - `:action`: Specifies the URL where to send the form-data when a form is
               submitted. e.g. \"/auth/login\"

  - `:title`: (Required) The title text to be displayed at the top of the form
              e.g. \"Login now\"

  - `intro-text`: (Optional) Introduction text providing a summary for what the
                  form is capturing as well as what happens on submission
                  e.g. \"Enter your details to login and view your dashboard\"."

  :example [:ux.forms/form
            {:action "/auth/login"             
             :title "Login now"
             :intro-text "Provide your details to login and view your personal
                          dashboard"}
            [:div
             [:label {:for "email-address"} "Email address"]
             [:input
              {:name "email-address" :id "email-address" :type "email"}]]]}

 ;; Pure function implementing the responsibilities of the component:              
 (fn [{:keys [action title intro-text]} & form-elements]
   [:form.input-form {:action action :method "POST"}
    (when title [:h1.form-title title])
    (when intro-text [:p.form-intro-text intro-text])
    form-elements]))
```

The [`->html` function](https://tendaysofclojure.github.io/hiccup-server-components-api-docs/ten-d-c.hiccup-server-components.core.html#var--.3Ehtml) can be used to convert Hiccup data, which references the component using its qualified keyword, to HTML:

```clojure
(hc/->html
 [:ux.forms/form
  {:action "/login"
   :title "Login now"
   :intro-text "Provide your details to login and view your personal
                dashboard"}
  [:div
   [:label {:for "email-address"} "Email address"]
   [:input
    {:name "email-address" :id "email-address" :type "email"}]]])
```

The following HTML is returned:

```html
<form action="/login" method="POST" class="input-form">
    <h1 class="form-title">Login now</h1>
    <p class="form-intro-text">Provide your details to login and view your personal
                              dashboard</p>
    <div>
        <label for="email-address">Email address</label>
        <input id="email-address" name="email-address" type="email"/>
    </div>
</form>
```

### Step 4: Register components for form input

The next step is to register components that will be responsible for text input
which, as with previous examples, involves using the [`reg-component` function](https://tendaysofclojure.github.io/hiccup-server-components-api-docs/ten-d-c.hiccup-server-components.core.html#var-reg-component), including `doc` and `examples` metadata, and
using a pure function, implement the components responsibilities that include:

```Clojure
(hc/reg-component
 ;; Keyword to uniquely identify the component:
 :ux.forms/text-input

 ;; Include meta data:
 {:doc
  "A single-line text field of the given `input-type` that includes a label.

   Component options:

   - `:element-name`: Specifies the name of an input element which will be used
                       as the name of the field when posting a FORM. Also used
                       as the elements id.

   - `label-text`: The text used for the associated label element.

   - `input-type`: The type of form element e.g \"text\", \"email\",
                   \"password\" etc.

   - `auto-focus?`: Boolean value indicating that an element should be focused
                    on page load."

  :examples {"Email field" [:ux.forms/text-input
                            {:auto-focus? true
                             :label-text "Email address"
                             :element-name "email-address"
                             :input-type "email"}]

             "Password field" [:ux.forms/text-input
                               {:label-text "Password"
                                :element-name "password"
                                :input-type "password"}]}}

 ;; Pure function implementing the responsibilities of the component:
 (fn [{:keys [element-name label-text input-type auto-focus?]
      :or {input-type "text" auto-focus? false}}]
   [:div.form-field
    [:label.form-label {:for element-name} label-text]
    [:input.text-input
     {:id element-name :name element-name
      :autofocus auto-focus? :type input-type}]]))
```

The [`->html` function](https://tendaysofclojure.github.io/hiccup-server-components-api-docs/ten-d-c.hiccup-server-components.core.html#var--.3Ehtml) can be used to convert Hiccup data, which references the component using its qualified keyword, to HTML:

```clojure
[:ux.forms/text-input
  {:auto-focus? true
   :label-text "Email address"
   :element-name "email-address"
   :input-type "email"}]
```

The following HTML is returned:

```html
<div class="form-field">
  <label class="form-label" for="email-address">Email address</label>
  <input autofocus="autofocus" class="text-input"
         id="email-address" name="email-address"
         type="email"/>
</div>
```

### Step 5: Form buttons

The next step is to register components that will be responsible for form buttons.

We'll register two buttons  `:ux.forms/primary-submit-button` and `:ux.forms/cancel-button`
with metadata that document the button components:

```clojure
(hc/reg-component
 :ux.forms/primary-submit-button

 {:doc
  "Represents the button that will take the primary action which is submitting
   the form. Button text is specified as the first and only parameter."

  :examples {"Login submit button" [:ux.forms/primary-submit-button
                                    "Login now"]

             "Save submit button" [:ux.forms/primary-submit-button
                                   "Save"]}}

 (fn [button-text]
   [:button.primary-submit-button {:type "submit"} button-text]))


(hc/reg-component
 :ux.forms/cancel-button

 {:doc "Represents a button that is used to cancel an action and not submit a
        form, for example going back to a previous page."

  :examples {"Login submit button" [:ux.forms/cancel-button
                                    "Login now"]

             "Save submit button" [:ux.forms/cancel-button
                                   "Save"]}}

 (fn [button-text url-to-redirect-to]
   [:button.cancel-button
    {:type "button"
     :onclick (str "javascript:window.location='"
                   url-to-redirect-to "'")}
    button-text]))
```

In addition to the submit and cancel button components we need a container element for form buttons
that will centre align and space the buttons consistently:

```clojure
(hc/reg-component
 :ux.forms/action-buttons
 {:doc
  "A container that will centre and consistently space buttons which represent
   actions that can be taken on the form such as submitting the form or
   going back to a previous page.

   Usually placed at the bottom of a form after input fields.

   Designed to work with form buttons such as `:ux.forms/primary-submit-button`
   and `:ux.forms/cancel-button`"

  :example [:ux.forms/action-buttons
            [:ux.forms/primary-submit-button "Login now"]
            [:ux.forms/cancel-button "Back" "/home"]]}
 (fn [& children]
   [:div.form-action-buttons
    children]))
```

The [`->html` function](https://tendaysofclojure.github.io/hiccup-server-components-api-docs/ten-d-c.hiccup-server-components.core.html#var--.3Ehtml) can be used to convert Hiccup data, which references the component using its qualified keyword, to HTML:

```
(hc/->html
  [:ux.forms/action-buttons
    [:ux.forms/primary-submit-button "Login now"]
    [:ux.forms/cancel-button "Back" "/home"]])
```

The following HTML is returned:

```HTML
<div class="form-action-buttons">
    <button class="primary-submit-button" type="submit">Login now</button>

    <button class="cancel-button"
            onclick="javascript:window.location=&apos;/home&apos;"
            type="button">
            Back
    </button>
</div>
```

### Step 6: Composing all form elements into a login screen and generating HTML

We have now registered components for the following form components:

- `:ux.layouts/html-doc` - Builds the main HTML document.
- `:ux.forms/form` - Builds the HTML form element.
- `:ux.forms/text-input` - Builds a single-line text field.
- `:ux.forms/action-buttons`- Layout container for buttons at the bottom of a form.
- `:ux.forms/cancel-button` - A less pronounced button used for cancel actions.
- `:ux.forms/primary-submit-button` - The primary button of the form.

From this catalogue of form components we can compose another component that
represents a typical login page a user would use to authenticate:


```clojure
(hc/reg-component
 :ux.pages/login

 {:doc "The main login page for user authentication"
  :example [:ux.pages/login]}

 (fn []
   [:ux.layouts/html-doc {:title "Login now"}

    [:ux.forms/form
     {:title "Login now"
      :intro-text "Enter your email address and password to access your personal dashboard"
      :action "/login"}

     [:ux.forms/text-input
      {:label-text "Email address"
       :element-name "email-address"
       :auto-focus? true}]

     [:ux.forms/text-input
      {:label-text "Password"
       :element-name "password"
       :input-type "password"}]

     [:ux.forms/action-buttons
      [:ux.forms/primary-submit-button "Login now"]
      [:ux.forms/cancel-button "Cancel" "/home"]]]]))
```

We can then generate the HTML by using the [`component->html` function](https://tendaysofclojure.github.io/hiccup-server-components-api-docs/ten-d-c.hiccup-server-components.core.html#var-component-.3Ehtml):

```clojure
(hc/component->html :ux.pages/login)
```

Which produces the following HTML:

```html
<html>
  <head>
      <meta charset="UTF-8"/>
      <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
      <link href="/css/main.css" rel="stylesheet"/>
      <script src="/js/app-bundle.js"></script>
      <title>Login now</title>
  </head>
  <body>
      <form action="/login" method="POST" class="input-form">
          <h1 class="form-title">Login now</h1>
          <p class="form-intro-text">
            Enter your email address and password to access your personal
            dashboard.
          </p>
          <div class="form-field">
              <label class="form-label" for="email-address">Email address</label>
              <input autofocus="autofocus" class="text-input"
                     id="email-address"
                     name="email-address" type="text"/>
          </div>
          <div class="form-field">
              <label class="form-label" for="password">Password</label>
              <input class="text-input" id="password" name="password"
                  type="password"/>
          </div>
          <div class="form-action-buttons">
              <button class="primary-submit-button" type="submit">Login now</button>
              <button class="cancel-button"
                      onclick="javascript:window.location=&apos;/home&apos;"
                      type="button">Cancel</button>
          </div>
      </form>
  </body>
</html>
```

[back to top](#table-of-contents)

# Defining components

**<u>Defining</u>** components is achieved by using the [`reg-component` function](https://tendaysofclojure.github.io/hiccup-server-components-api-docs/ten-d-c.hiccup-server-components.core.html#var-reg-component), which associates a qualified keyword with either a function (for dynamic content) or a vector or string (for static content) that represents a piece of the user interface.

In turn, components can then be referenced by their qualified keyword in Hiccup data, much like HTML elements, allowing for composition.

The Hiccup server components library is included in your code by requiring the `ten-d-c.hiccup-server-components.core` namespace:

```Clojure
(ns your-web-app.ux.core
  (:require [ten-d-c.hiccup-server-components.core :as hc]))
```

### Registering component functions

When a component returns dynamic content and accepts parameters that affect its output, it can be registered using a function.

The below example registers a component `:ux.components/primary-submit-button` which abstracts a HTML submit button:

```clojure
(hc/reg-component :ux.components/primary-submit-button
                  (fn [text attributes]
                    [:button.primary-button.submit-button
                     (assoc attributes :type "submit")
                     text]))
```

The above component function accepts a `text` parameter (representing the button text) as well as an `attributes` parameter (representing arbitrary attributes of the button). In addition, a `primary-button` & `submit-button` css class are added to the button using the [Hiccup CSS-like shortcut](https://github.com/weavejester/hiccup/wiki/Syntax#css-style-sugar).

When registering a component in this manner the function should meet the following requirements:

1.) Should be a [pure function](https://practical.li/clojure/thinking-functionally/pure-functions.html):

  - A pure function is free of side effects, does not change any other part of the system, and is not affected by any other part of the system.

  - A pure function's return values are identical for identical arguments.

2.) Should have an intuitive argument structure and provide sensible defaults. This will be covered in more detail later.

3.) Should return either a vector representing Hiccup data or a string.

To expand on the previous example and highlight a different approach to function arguments, we could define a `:ux.components/cancel-button` as follows:

```clojure
(hc/reg-component
  :ux.components/cancel-button
  (fn [& {:keys [text on-cancel]}]
    [:button.cancel-button
     {:type "button" :onclick on-cancel}
     text]))
```

The above component function destructures a `text` parameter (representing the button text) as well as an `on-cancel` handler (representing the client-side `onclick` handler of the button). In addition, a `cancel-button` css class is added to the button.

### Component metadata

When a component is registered using the [`reg-component` function](https://tendaysofclojure.github.io/hiccup-server-components-api-docs/ten-d-c.hiccup-server-components.core.html#var-reg-component), metadata in the form of a map can be included as the second parameter to help document the component and its parameters as well as provide examples of component usage.

While arbitrary metadata can be provided, the convention of component metadata includes the following keys:

- `:doc` - a docstring describing the component and its parameters,
- `:example` - Hiccup data showing a single example usage of the component
- `:examples` - A map with keys that describe the example and values that are Hiccup data, allowing for multiple examples to be provided.

The below example registers a `:ux.layouts/centred-container` component with metadata including a `doc` and a `example` key:

```clojure
(hc/reg-component
 :ux.layouts/centred-container

 {:doc
   "A simple container to horizontally centre content. Accepts one or more child
    elements."
  :example [:ux.layouts/centred-container
            [:h1 "Hello world"]]}

 (fn [& children]
   [:div.centred-container children]))
```

The below example registers a `:ux.layouts/centred-container` component with metadata including a `doc` and a `examples` key:

```clojure
(hc/reg-component
 :ux.layouts/centred-container

 {:doc
   "A simple container to horizontally centre content. Accepts a variable amount
    of child elements."

  :examples
  {"With a single child element" [:ux.layouts/centred-container
                                  [:h1 "Hello world"]]

   "With multiple child elements" [:ux.layouts/centred-container
                                   [:h1 "Hello world"]
                                   [:p "Hello this is a test"]
                                   [:p "This is a second paragraph"]]}}

 (fn [& children]
   [:div.centred-container children]))
```


### Registering component vectors or strings

When a component returns static content and does not require any input parameters that affect its output, it can be registered using either a vector (representing Hiccup data) or a string (representing either plain text or an HTML string).

The below example registers a component `:ux.components/sale-banner` with a vector representing Hiccup data:

```clojure
(hc/reg-component
  :ux.components/sale-banner
  [:div.sale-banner
   [:strong "50% off sale!"] " Buy any item on promotion and get 50% off. "
   [:a {:href "/promotions"} "View promotions"]])
```

The below examples register plain text components representing page titles. Plain text components can be useful for having text content available as a component:

```clojure
(hc/reg-component :ux.page-titles/login-page "Login now")
(hc/reg-component :ux.page-titles/dashboard-page "Welcome to your dashboard")
(hc/reg-component :ux.page-titles/register-page "Register your account now")
```

By default, all strings are escaped, however, components can be registered as raw HTML strings that won't be escaped by including the `:hiccup-server-components/raw-html? true` option as the second parameter to `reg-component`.

Using Hiccup data structure is always preferred over raw HTML strings, but this option is included for completeness:

```clojure
(hc/reg-component
  :ux.components/sale-banner-raw
  ;; Including this option will prevent HTML from being escaped
  {:hiccup-server-components/raw-html? true}
  "<div class='sale-banner'>
     <strong>50% off sale!</strong> Buy any item on promotion and get 50% off.
     <a href='/promotions'>Go to promotions</a>
   </div>")
```

[back to top](#table-of-contents)

# Composing components

Once components have been registered they can be **referenced in Hiccup data by their qualified keywords** much like any other HTML element **allowing for composition**.

The below example shows a centred container div with a "side bar" (containing a list of links representing a menu) and a "main content" area displaying content based on what is selected in the side bar:

```clojure
[:div.centred-container
  [:div.side-bar
    [:ul
      [:li.active [:a {:href "/dashboard"} "Dashboard"]]
      [:li [:a {:href "/your-profile"} "Your profile"]]
      [:li [:a {:href "/manage-users"} "Manage users"]
      [:li [:a {:href "/logout"} "Logout"]]]]]

  [:div.main-content
   [:h1.main-title "Welcome to your dashboard"]
   [:p "This is your dashboard, this is a test."]]]
```

In the above example `div`, `ul`, `li`, `a`, `h1` and `p` HTML elements are composed into an abstract structure that represents the "side bar" and "main content" areas, which is centred by a parent container.

As a first step, we can extract both the "side bar" and the "main content" areas into vector components that initially represent static content:

```clojure
(hc/reg-component
 :ux.components/side-bar
 [:div.side-bar
  [:ul
   [:li.active [:a {:href "/dashboard"} "Dashboard"]]
   [:li [:a {:href "/your-profile"} "Your profile"]]
   [:li [:a {:href "/manage-users"} "Manage users"]
   [:li [:a {:href "/logout"} "Logout"]]]]])


(hc/reg-component
 :ux.components/main-content
 [:div.main-content
  [:h1.main-title "Welcome to your dashboard"]
  [:p "This is your dashboard, this is a test."]])
```

We can then reference these components by their qualified keyword in Hiccup data (much like any other HTML element), effectively providing a level of abstraction:

```clojure
[:div.centred-container
  [:ux.components/side-bar]
  [:ux.components/main-content]]
```

Taking it a step further, we can also abstract the "centred container" div element. Parent/container elements often have a variable number of child elements that are accommodated by using a [variadic function](https://clojure.org/guides/learn/functions#_variadic_functions) (i.e. a function with a variable number of arguments):

```clojure
(hc/reg-component
 :ux.layouts/centred-container
 {:doc "A simple container to horizontally center content"
  :examples
  {"With a single child element" [:ux.layouts/centred-container
                                   [:p "This is a test"]]

   "With multiple child elements" [:ux.layouts/centred-container
                                    [:h1 "Hello world"]
                                    [:p "Hello this is a test"]
                                    [:p "This is a second paragraph"]]}}
 ;; Variadic function with variable arguments representing child elements.
 (fn [& children]
   [:div.centred-container children]))
```

We can then further abstract the example as follows:

```clojure
[:ux.layouts/centred-container
  [:ux.components/side-bar]
  [:ux.components/main-content]]
```

While for the sake of example, the "side bar" and "main content" components were initially registered as static content (using a vector representing Hiccup data), these components should become dynamic by using component functions.

### Converting to a dynamic side bar component

To convert the "side bar" into a dynamic component we can take the following steps:

1. **Define the component's responsibilities**: The component is responsible for constructing a "side bar" element that includes a list of links representing a menu. It is also responsible for determining which of the links in the menu is "active".

2. **Define component parameters**: The component accepts a map as parameter that includes two keys: `active-url` representing the url of the menu link that should be marked as active and `menu-items` which is a vector of maps that include a `href` and `label` key which represent the menu items.

    An example of referencing the component with parameters:

    ```clojure
    [:ux.components/side-bar
      {:active-url "/dashboard"
       :menu-items [{:href "/dashboard" :label "Dashboard"} ;; marked as active
                    {:href "/your-profile" :label "Your profile"}
                    {:href "/manage-users" :label "Manage users"}
                    {:href "/logout" :label "Logout"}]}]
    ```


 3. **Register a component** that meets the above requirements:

    ```clojure
    (hc/reg-component
     :ux.components/side-bar
     ;; Accepts `active-url` and `menu-items`
     (fn [{:keys [active-url menu-items]}]
       [:div.side-bar
        [:ul
         ;; Builds a list of menu items
         (for [{:keys [href label]} menu-items]
           ;; Determines if the menu item is active based on the `active-url`
           ;; and assigns a "active" CSS class
           (let [css-class (when (= href active-url)
                             "active")]
             [:li {:class css-class}
              [:a {:href href} label]]))]]))
    ```

4. When a component is registered, meta data in the form of a map can be included as the second parameter to help document the component as well as its parameters.

   While arbitrary metadata can be provided, based on the developers needs, the two common keys are `doc` which is a string describing the component and it's parameters and `example` which is Hiccup data showing example usage of the component.

   Below is an expanded example of the side bar that includes `doc` and `example` metadata:

    ```clojure
    (hc/reg-component
     :ux.components/side-bar
     {:doc
       "Responsible for constructing a \"side bar\" element that includes a list
        of links representing a menu. It is also responsible for determining
        which of the menu items are \"active\".

        Supported parameters:

        `:active-url` (optional) - The url of the menu item that should be
                                   marked as active. e.g. \"/your-profile\"

        `:menu-items` (required) - A vector of maps that include a `href` and
                                   `label` key which represent the menu items.
                                   e.g. `[{:href \"/your-profile\"
                                           :label \"Your profile\"}
                                          {:href \"/manage-users\"
                                           :label \"Manage users\"}]`"

      :example [:ux.components/side-bar
                {:active-url "/dashboard"
                 :menu-items [{:href "/dashboard" :label "Dashboard"}
                              {:href "/your-profile" :label "Your profile"}
                              {:href "/manage-users" :label "Manage users"}
                              {:href "/logout" :label "Logout"}]}]}
     (fn [{:keys [active-url menu-items]}]
       [:div.side-bar
        [:ul
         ;; Builds a list of menu items
         (for [{:keys [href label]} menu-items]
           ;; Determines if the menu item is active based on the `active-url`
           ;; and assigns a "active" CSS class
           (let [css-class (when (= href active-url)
                             "active")]
             [:li {:class css-class}
              [:a {:href href} label]]))]]))
    ```


### Converting to a dynamic main content component

To convert the "main content" into a dynamic component we can do the following:

1. **Define the components responsibilities**: The component is responsible for constructing a "main content" element that includes a title element (`h1`) as well as variable amount of child elements.

2. **Define component parameters**: The component accepts a map as its first parameter that includes a `title` keys representing the title shown in the main area. The second parameter is a list of variable child elements (using Clojure's [variadic function](https://clojure.org/guides/learn/functions#_variadic_functions) syntax) that will be displayed within the main content area.

    An example of referencing the components with parameters:

    ```clojure
    [:ux.components/main-content
      ;; Component options
      {:title "Welcome to your dashboard"}
      ;; Variable child elements
      [:p "This is your dashboard, this is a test."]
      [:p "Use the following link to take an action "
        [:a {:href "/some-action"} "Take action now"]]]
    ```

 3. Register a component (including `doc` and `example` metadata) that meets the above requirements:

    ```clojure
    (hc/reg-component
     :ux.components/main-content
     {:doc
       "Responsible for constructing a \"main content\" element that includes a
        title element (`h1`) as well as variable amount of child elements.

        Accepts a map as the first parameter that should include a `title` keys
        representing the title shown in the main area.

        The second parameter is a list of variable child elements that will be
        displayed within the main content area."

      :example [:ux.components/main-content
                {:title "Welcome to your dashboard"}
                [:p "This is your dashboard, this is a test."]
                [:p "Use the following link to take an action "
                 [:a {:href "/some-action"} "Take action now"]]]}
     (fn [{:keys [title]} & children]
       [:div.main-content
        ;; Title always shown
        [:h1.main-title title]
        ;; Child elements defined by consumer of component.
        children]))
    ```        

With both the "side bar" and "main content" components now being dynamic we can rewrite the original Hiccup data using the new components which results in a higher level of abstraction.

Before:

```clojure
[:div.centred-container
  [:div.side-bar
    [:ul
      [:li.active [:a {:href "/dashboard"} "Dashboard"]]
      [:li [:a {:href "/your-profile"} "Your profile"]]
      [:li [:a {:href "/manage-users"} "Manage users"]
      [:li [:a {:href "/logout"} "Logout"]]]]]

  [:div.main-content
   [:h1.main-title "Welcome to your dashboard"]
   [:p "This is your dashboard, this is a test."]]]
```

After:

```clojure
[:ux.layouts/centred-container

  [:ux.components/side-bar
    {:active-url "/dashboard"
     :menu-items [{:href "/dashboard" :label "Dashboard"}
                  {:href "/your-profile" :label "Your profile"}
                  {:href "/manage-users" :label "Manage users"}
                  {:href "/logout" :label "Logout"}]}]

  [:ux.components/main-content
    {:title "Welcome to your dashboard"}
    [:p "This is your dashboard, this is a test."]
    [:p "Use the following link to take an action "
      [:a {:href "/some-action"} "Take action now"]]]]
```


Would produce the following HTML:

```html
<div class="centred-container">
  <div class="side-bar">
    <ul>
      <li class="active">
          <a href="/dashboard">Dashboard</a>
      </li>      
      <li>
          <a href="/your-profile">Your profile</a>
      </li>
      <li>
          <a href="/manage-users">Manage users</a>
      </li>
      <li>
          <a href="/logout">Logout</a>
      </li>
    </ul>
  </div>
  <div class="main-content">
    <h1 class="main-title">Welcome to your dashboard</h1>
    <p>This is your dashboard, this is a test.</p>
    <p>
      Use the following link to take an action
      <a href="/some-action">Take action now</a>
    </p>
  </div>
</div>
```

[back to top](#table-of-contents)

# Organising components

Organising components is done through the use of **qualified keywords**, **Clojure namespaces**, and **source code structure** to create a component catalogue.

When registering a component a qualified keyword must be used. Qualified keywords make components unique, provide a means of logical categorisation, and allow component references to be quickly identified when they are composed into Hiccup data.

### Convention for organising components

The following namespace and source code structure is used to organise components:

1. A top-level/parent directory (usually `src/ux`) with a `src/ux/core.clj` file representing the top level component namespace `ux.core`, the main entry point for all components. Requiring this namespace in other areas of your application will make all components available to consuming code.

2. Child source files in the top-level/parent directory representing secondary component namespaces to group logically related components:

    Examples of secondary namespaces in parent directory `src/ux`:

    - Namespace: `ux.layouts` in file `src/ux/layouts.clj`
    - Namespace `ux.buttons` in file `src/ux/buttons.clj`
    - Namespace `ux.pages` in file `src/ux/pages.clj`

3. Secondary component namespaces are made available by requiring them in the main component namespace:

    ```clojure
    ;; File src/ux/core.clj

    (ns ux.core ;; Main component namespace
      ;; Require secondary components, making them generally available.
      (:require [ux.layouts]
                [ux.buttons]
                [ux.pages]))
    ```

4. Tertiary component namespaces can be used for further separation of components and have their own subdirectory under the main parent directory.

    - Example: namespace: `ux.layouts.html-doc` in file `src/ux/layouts/html_doc.clj`

5. Tertiary component namespaces are made available by requiring them in the relevant secondary component namespace:

    ```clojure
    ;; File src/ux/layouts.clj

    (ns ux.layouts ;; Secondary component namespace
      ;; Include tertiary components
      (:require [ux.layouts.html-doc]))
    ```

This convention should cover 90% of component organisation needs and allow developers to build a component catalogue.

Below is an example directory structure showing component source file organisation:

```bash
ux                   # Top-level/parent directory
├── core.clj         # File: Main component namespace `ux.core`
├── layouts.clj      # File: Secondary `ux.layouts` namespace
├── layouts          # Directory: Tertiary component namespaces for layouts
│   └── html_doc.clj # File: Tertiary `ux.layouts.html-doc` namespace
├── buttons.clj      # File: Secondary `ux.buttons` namespace
└── pages.clj        # File: Secondary `ux.pages` namespace
```

### Main entry point namespace for components

In order to make your components generally available in your application we'll employ standard Clojure source code and namespace organisation techniques.

As an example we'll create a `ux` directory containing a file `src/ux/core.clj` which represents the top level component namespace `ux.core` and main entry point for all your components. Requiring this file in other namespaces in your application will make all components available to consuming code.

Initially the file includes only the namespace definition:

```clojure
;; File src/ux/core.clj
(ns ux.core)
```

### Secondary component namespaces and source files

Once the top-level component namespace has been created the next step is to create Clojure source files representing secondary component namespaces.

For example if we have layout components we would have a `ux.layouts` namespace in file `src/ux/layouts.clj`. This file represents the main entry point for layout components.

Below we define the `ux.layouts` namespace by creating the `src/ux/layouts.clj` file and registering a couple of components in the `:ux.layouts` namespace:

```clojure
;; File src/ux/layouts.clj

(ns ux.layouts
  (:require [ten-d-c.hiccup-server-components.core :as hc]))

(hc/reg-component :ux.layouts/html-doc "...")
(hc/reg-component :ux.layouts/centred-container "...")
(hc/reg-component :ux.layouts/centred-screen "...")
(hc/reg-component :ux.layouts/section "...")
(hc/reg-component :ux.layouts/form "...")
(hc/reg-component :ux.layouts/footer "...")

```

To expand on this we could have a `ux.buttons` namespace in the `src/ux/buttons.clj` file which represents the main entry point for button components:

```clojure
;; File src/ux/buttons.clj

(ns ux.buttons
  (:require [ten-d-c.hiccup-server-components.core :as hc]))

(hc/reg-component :ux.buttons/primary-button "...")
(hc/reg-component :ux.buttons/secondary-button "...")
(hc/reg-component :ux.buttons/tertiary-button "...")
(hc/reg-component :ux.buttons/info-button "...")
(hc/reg-component :ux.buttons/success-button "...")
(hc/reg-component :ux.buttons/danger-button "...")
(hc/reg-component :ux.buttons/warning-button "...")

```

We could have a `ux.pages` namespace in the `src/ux/pages.clj` file which represents the main entry point for high level webpages:

```clojure
;; File src/ux/pages.clj

(ns ux.pages
  (:require [ten-d-c.hiccup-server-components.core :as hc]))

(hc/reg-component :ux.pages/home "...")
(hc/reg-component :ux.pages/dashboard "...")
(hc/reg-component :ux.pages/your-profile "...")
(hc/reg-component :ux.pages/manage-users "...")
```

### Including secondary component namespaces in main entry point file

To make these components available to the application we would then require these namespaces (`ux.layouts` `ux.buttons` and `ux.pages`) in the main component entry point namespace `ux.core` defined in file `src/ux/core.clj`:

```clojure
;; File src/ux/core.clj

(ns ux.core
  ;; Require second level of components, making them generally available.
  (:require [ux.layouts]
            [ux.buttons]
            [ux.pages]))
```

### Tertiary component namespaces and source files

Some components may be complex enough or have thorough documentation to warrant their own namespace or it might be the developers preference to create a high degree of separation between components.

For example, lets say we had a `:ux.layouts/html-doc` component that supported many options, included thorough documentation and was more complex than other layouts. We could create a further separation by creating a dedicated `ux.layouts.html-doc` namespace and source file `src/ux/layouts/html_doc.clj`

```clojure
;; File src/ux/layouts/html_doc.clj

(ns ux.layouts.html-doc
  (:require [ten-d-c.hiccup-server-components.core :as hc]))

(hc/reg-component :ux.layouts/html-doc "..." )
```

We could then require this tertiary source file in the layout namespace `ux.layouts` to make it generally available.

Since the `ux.layouts` namespace is already required in the main component entry point namespace `ux.core`, the tertiary component `:ux.layouts/html-doc` will automatically become available:

```clojure
;; File src/ux/layouts.clj

(ns ux.layouts
  (:require [ux.layouts.html-doc] ;; include the html-doc
            [ten-d-c.hiccup-server-components.core :as hc]))

;; `:ux.layouts/html-doc` component now reginsred in `ux.layouts.html-doc` namespace.

(hc/reg-component :ux.layouts/centred-container "...")
(hc/reg-component :ux.layouts/centred-screen "...")
(hc/reg-component :ux.layouts/section "...")
(hc/reg-component :ux.layouts/form "...")
(hc/reg-component :ux.layouts/footer "...")            
```

[back to top](#table-of-contents)

# Generating HTML

The below functions are provided to generate HTML from Hiccup data that can include component references:

- [`->html`](https://tendaysofclojure.github.io/hiccup-server-components-api-docs/ten-d-c.hiccup-server-components.core.html#var--.3Ehtml) Takes `hiccup-data`, that can include component references, and returns the generated HTML.

- [`->html-file`](https://tendaysofclojure.github.io/hiccup-server-components-api-docs/ten-d-c.hiccup-server-components.core.html#var--.3Ehtml-file) Takes a `file-path` and `hiccup-data`, that can include component references, and saves the generated HTML to the given `file-path`.

- [`component->html`](https://tendaysofclojure.github.io/hiccup-server-components-api-docs/ten-d-c.hiccup-server-components.core.html#var-component-.3Ehtml) Generates and returns HTML of a component with the given `component-element-name`.

- [`component->html-file`](https://tendaysofclojure.github.io/hiccup-server-components-api-docs/ten-d-c.hiccup-server-components.core.html#var-component-.3Ehtml-file) Generates the HTML of a component with the given `component-element-name` and
saves the output to the given `file-path`.

See [API docs](https://tendaysofclojure.github.io/hiccup-server-components-api-docs/ten-d-c.hiccup-server-components.core.html) for details.

[back to top](#table-of-contents)

# HTTP routing middleware

Ring middleware that will generate HTML using Hiccup server components conventions
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
  (GET "/" []
       {:hsc/component :ux.pages/home})


  ;; Generates and returns the HTML for the `:ux.pages/dashboard` component,
  ;; passing the component the `hsc/params` key as params.
  (GET "/dashboard" []
       {:hsc/component :ux.pages/dashboard
        :hsc/params {:username "bobsmith"
                     :email-address "bobsmith@somemail.net"}})


  ;; Generates and returns the HTML from Hiccup data (which can include
  ;; component refererences) in the `:hsc/html` key.
  (GET "/testing" []
       {:hsc/html [:ux.layouts/html-doc {:title "A test page"}
                   [:div
                    [:h1.text-3xl "Hello world From HTML"]
                    [:p "This is a test"]]]}))


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
    [["/" {:handler (fn [_]
                      {:hsc/component :ux.pages/home})}]

     ["/dashboard"
      {:get
       {:handler
        (fn [_]
          {:hsc/component :ux.pages/dashboard
           :hsc/params {:username "bobsmith"
                        :email-address "bobsmith@somemail.net"}})}}]

     ["/testing"
      {:get
       {:handler
        (fn [_]
          {:hsc/html [:ux.layouts/html-doc {:title "A test page"}
                      [:div
                       [:h1.text-3xl "Hello world From HTML"]
                       [:p "This is a test"]]]})}}]]

    {:data {:middleware [hc/wrap-response-middleware]
            :enable true}})))
```

See [`wrap-response-middleware` documentation](https://tendaysofclojure.github.io/hiccup-server-components-api-docs/ten-d-c.hiccup-server-components.core.html#var-wrap-response-middleware) for more details.


[back to top](#table-of-contents)

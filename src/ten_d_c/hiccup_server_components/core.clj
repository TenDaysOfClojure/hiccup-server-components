(ns ten-d-c.hiccup-server-components.core
  (:require [ten-d-c.hiccup-server-components.components :as components]
            [ten-d-c.hiccup-server-components.compiler :as compiler]
            [clojure.string :as string]
            [hiccup2.core :as hiccup]
            [ten-d-c.hiccup-server-components.component-stats :as component-stats]
            [clojure.string :as str])
  (:gen-class))

;; -- Public API --

(defn all-components
  "Lists all components and their associated metadata"
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
  "Gets component documentation (a map including a `doc` and `example`/`examples` key if availible) from
  it's metadata that was associated with the component when registered via `reg-component`"
  [element-name]
  (components/get-docs element-name))


(defn reg-component
  "Registers a component with the given `element-name` (must be a qualified keyword e.g `:my-components/login-form`)
   which a `component` which can be either a pure function that returns Hiccup data, a vector (representing Hiccup data) or a strings

   - `element-name` (Required): The element name (must be a qualified keyword e.g :my-components/login-form) that describes the component which can be used in Hiccup e.g `::ordered-list`, `::unordered-list`. Recommended to use fully qualified keywords to help organise components.

   - `component-meta-data` (Optional): Associates arbitrary metadata with the component.

   - `component` (Required): Can be either a pure function that returns Hiccup data, a vector (representing Hiccup data) or a string"
  ([element-name component-meta-data component]

   (components/reg-component element-name
                             component-meta-data
                             component))

  ([element-name component]
   (reg-component element-name {} component)))


(defn raw-html
  "Converts provided `html` into an unescaped string, allowing the HTML to be rendered by the browser.
  Used for including strings that contain HTML markup"
  [& html]
  (hiccup/raw (string/join html)))


(defn javascript
  "Converts provided `javascript` into an unescaped string, prevents escaped javascript strings
  allowing the javascript to be executed by the browser.  Used for including javascript in Hiccup data."
  [& javascript]
  (hiccup/raw (string/join javascript)))


(defn ->hiccup
  "Processes components in the provided `hiccup-data` and returns expanded Hiccup data.

  - `hiccup-data`: The hiccup data to process which includes references to components.

  - `local-components`: (Optional) A map of component configuration. Components defined here will overwrite components registered with `->reg-component` for this function call only."
  ([hiccup-data local-components]
   (compiler/->hiccup hiccup-data local-components))

  ([hiccup-data]
   (compiler/->hiccup hiccup-data {})))


(defn ->html
  "Takes a Hiccup data, that can include component references, and returns the generated HTML.

  - `hiccup-data`: The hiccup data to process which may include references to components.

  - `local-components`: (Optional) A map of component configuration. Components defined here will overwrite components registered with `->reg-component` for this function call only."
  ([hiccup-data local-components]
   (compiler/->html hiccup-data local-components))

  ([hiccup-data] (compiler/->html hiccup-data)))


(defn ->html-file
  "Takes a `file-path` and a `hiccup-data `, that can include component references, and saves the generated HTML to the given `file-path`.

  - `file-path`: The file path to save the outputed HTML to.

  - `hiccup-data`: The hiccup data to process which may include references to components.

  - `local-components`: (Optional) A map of component configuration. Components defined here will overwrite components registered with `->reg-component` for that function call only."
  ([file-path hiccup-data]
   (compiler/->html-file file-path hiccup-data))

  ([file-path hiccup-data local-components]
   (compiler/->html-file file-path hiccup-data local-components)))


(defn component->html
  "Provides the convenience of not needing to construct hiccup data by generating HTML of a component with the given `component-element-name`.

  Useful for generating the HTML of top-level web pages granted they are registered as component.

  - `component-element-name`: The qualified keyword of the component element name.

  - `params`: (Optional) The params to pass to the component if it has parameters.

  - `local-components`: (Optional) A map of component configuration. Components defined here will overwrite components registered with `->reg-component` for that function call only."
  ([component-element-name]
   (component->html component-element-name nil {}))

  ([component-element-name params]
   (component->html component-element-name params {}))

  ([component-element-name params local-components]
   (let [hiccup-data (if (not (nil? params))
                       [component-element-name params]
                       [component-element-name])]
     (->html hiccup-data local-components))))


(defn component->html-file
  "Provides the convenience of not needing to construct hiccup data by generating HTML of a component with the given `component-element-name` and saving the output to the given `file-path`.

  Useful for generating HTML files of top-level web pages granted they are registered as component.

  - `file-path`: The file path to save the outputed HTML to.

  - `component-element-name`: The qualified keyword of the component element name.

  - `params`: (Optional) The params to pass to the component if it has parameters.

  - `local-components`: (Optional) A map of component configuration. Components defined here will overwrite components registered with `->reg-component` for that function call only."
  ([file-path component-element-name]
   (component->html-file file-path component-element-name nil {}))

  ([file-path component-element-name params]
   (component->html-file file-path component-element-name params {}))

  ([file-path component-element-name params local-components]
   (spit file-path
         (component->html component-element-name
                          params
                          local-components))))


(defn component-counts
  "Provides a summary for component counts grouped by component namespace

  Returns a list of maps that include a `component-namespace` and `total-components` key:

  ```
  ({:component-namespace \"ux.components\", :total-components 2}
   {:component-namespace \"ux.layouts\", :total-components 2})
  ```"
  ([] (component-stats/component-counts))

  ([all-components]
   (component-stats/component-counts all-components)))

(ns ^:no-doc ten-d-c.hiccup-server-components.components
  (:require [clojure.string :as string]
            [hiccup2.core :as hiccup]))

(defonce global-components (atom {}))

(defonce global-component-metadata (atom {}))


(defn get-component
  "Searches both `local-components` and global components for a `element-name`
   key and returns the component (which can be a function, vector or string)
   or nil if not found.

   If the `element-name` is found in `local-components` it will take preference
   over global components."
  [element-name local-components]
  (get (merge @global-components
              (or local-components {}))
       element-name))


(defn is-component?
  "Checks if the given `element-name` is a component by
   searching `local-components` and global components.
  `element-name` must be a qaulified keyword."
  [element-name local-components]
  (and (qualified-keyword? element-name)
       (not
        (nil? (get-component element-name local-components)))))



(defn component-definition?
  "Checks if the `element` is a component function definition which is a vector
   with the first element being a map which includes the
   `:hiccup-server-components/element-name` key and the rest of the items in the vector
   being parameters.

   For example:
  `[{:hiccup-server-components/element-name :unordered-list}}
    [\"one\" \"two\" \"three\"]]`"
  [element]
  (and (coll? element)
       (map? (first element))
       (contains? (first element)
                  :hiccup-server-components/element-name)))


(defn component-reference? [part]
  (and (map? part)
       (contains? part :hiccup-server-components/element-name)))


(defn references-component?
  [element]
  (and (coll? element)
       (not
        (nil? (some component-reference? element)))))


(defn ->component-params
  "Will return items after the first item of a component function definition which
   represents the paramters of a component function.

   Given a component function definition of:

   `[{hiccup-server-components/element-name :unordered-list}} [\"one\" \"two\" \"three\"]]`

  `[\"one\" \"two\" \"three\"]` will be returned as the component function parameters."
  [element] (rest element))


(defn ->component-element-name
  "Will return the `:hiccup-server-components/element-name` key of the first item in a
   component function definition which represents the element-name of the component.

   Given a component function definition of:

   `[{:hiccup-server-components/element-name :unordered-list}} [\"one\" \"two\" \"three\"]]`

   `:unordered-list` will be returned as the element name."
  [element]
  (:hiccup-server-components/element-name (first element)))


(defn extract-component [element local-components]
  (let [element-name (->component-element-name element)

        component    (get-component element-name local-components)

        params       (->component-params element)]

    [element-name component params]))


(defn extract-component-reference [element local-components]
  (let [element-name (:hiccup-server-components/element-name element)

        component    (get-component element-name local-components)]

    [element-name component]))


(defn- stack-trace-element->namespace [stack-trace-element]
  (-> (.getClassName stack-trace-element)
      (string/split #"\$")
      (first)))


(def known-stack-trace-element-to-remove
  ["ten_d_c.hiccup_server_components.core$"
   "ten_d_c.hiccup_server_components.components$"
   "ten_d_c.hiccup_server_components.compiler$"
   "cider."
   "clojure."])


(defn get-calling-strack-trace-element [exception]
  (->> (.getStackTrace exception)
       (filter
        (fn [stack-trace-element]
          (let [class-name (.getClassName stack-trace-element)]

            (every? #(not (string/starts-with? class-name %))
                    known-stack-trace-element-to-remove))))
       (first)))


(defn- derived-component-meta-data
  "Infers useful metadata about the given `component` and returns a map with the
  following keys:

  | -------------------|-------------------------------------------------------|
  | `:element-name`    | The qualified keyword representing the element name.
  | `:component-type`  | Can be either `function`, `vector` or `string`.
  | `:namespace`       | The clojure namespace the component was registered in.
  | `:file-name`       | The file name where the component was registered in.
  | `:line-number`     | The line number where the component is registered."
  [element-name component]

  (let [stack-trace-element (get-calling-strack-trace-element
                             (RuntimeException. "component-meta-data"))

        component-type      (cond (fn? component)
                                  "function"

                                  (vector? component)
                                  "vector"

                                  (string? component)
                                  "string")

        code-namespace      (stack-trace-element->namespace
                             stack-trace-element)]

    {:element-name element-name
     :component-type component-type
     :namespace code-namespace
     :file-name (.getFileName stack-trace-element)
     :line-number (.getLineNumber stack-trace-element)}))

;; -- Public API --


(defn get-meta-data [element]
  (or (get @global-component-metadata element)
      {:element-name element}))


(defn get-docs [element]
  (select-keys (get-meta-data element)
               [:doc :example :examples]))


(defn clear-components []
  "Clears all components and associated meta-data that where
   registered using `->reg-component`"
  (reset! global-components {})
  (reset! global-component-metadata {}))


(defn adjusted-component [meta-data component]
  (if (and (true? (:hiccup-server-components/raw-html? meta-data))
           (string? component))
    (hiccup/raw component)
    component))


(defn reg-component [element-name meta-data component]
  (when (not (qualified-keyword? element-name))
    (throw (ex-info
            (str "Element name of a component must be a qualified keyword"
                 " e.g :my-components/login-form")
            {:element-name element-name :source-file *file*})))

  (swap! global-components
         assoc
         element-name
         (adjusted-component meta-data component))

  (swap! global-component-metadata
         assoc element-name (merge (derived-component-meta-data element-name
                                                                component)
                                   meta-data))

  element-name)


(defn all-components
  "Lists all registered components"
  []
  (->> @global-components
       (map (fn [component]
              (get-meta-data (first component))))
       (sort-by :element-name)))


(defn extract-arity-message [ex]
  (-> (.getMessage ex)
      (string/split #"\:")
      (first)))


(defn run-component-function [element-name component-path component-function component-params]
  (try
    (apply component-function component-params)

    (catch clojure.lang.ArityException ex
      (throw (ex-info (str "Component called with the incorrect amount of arguments: "
                           (extract-arity-message ex)
                           " " element-name)
                      {:called-component element-name
                       :full-component-path (map get-meta-data component-path)})))))

(ns ^:no-doc ten-d-c.hiccup-server-components.compiler
  (:require [clojure.string :as string]
            [hiccup2.core :as hiccup]
            [hiccup.util :as hiccup-util]
            [ten-d-c.hiccup-server-components.components :as components]))


(defn- get-component-output
  "Gets the component output by executing the function (where the component
  is registered with a function) or in the case of a string/vector component returns
  the value provided at registration."
  [element-name component component-params component-path]
  (if (fn? component)
    (components/run-component-function
     element-name
     (conj component-path element-name)
     component
     component-params)

    ;; string/vector component, just return the component.
    component))


(defn- get-recursive-component-output
  [element-name
   component
   component-params
   component-path
   local-components
   apply-to-component-output]
  (let [component-output (get-component-output element-name
                                               component
                                               component-params
                                               component-path)]

    (apply-to-component-output
     component-output
     local-components
     apply-to-component-output
     (conj component-path element-name))))


(defn- map-recursive-component-output
  [parts local-components component-path apply-to-component-output]
  (mapv (fn [part]
          (if (components/component-reference? part)
            (let [[element-name component] (components/extract-component-reference
                                            part local-components)]

              (get-recursive-component-output
               element-name
               component
               nil ;; No component params
               component-path
               local-components
               apply-to-component-output))

            ;; Not a component reference
            part))
        parts))


(defn- process-component
  "Used in conjunction with `clojure.walk/post-walk` checking the `element` and executing
   different logic based on the type of element to achieve expansion of components."
  ([element local-components apply-to-component-output]
   (process-component
    element local-components apply-to-component-output []))

  ([element local-components apply-to-component-output component-path]

   (cond
     ;; If the element is a keyword check if the keyword has been registred
     ;; as a component and if so, replace the keyword with a map that includes a
     ;; `:hiccup-server-components/element-name` key which will get processed later.
     (qualified-keyword? element)
     (if (components/is-component? element local-components)
       {:hiccup-server-components/element-name element}
       element)

     ;; Check if the element represents a component and if so, extract
     ;; the component and get the output.
     ;; The output of the component function is then provided to the `apply-to-component-output`
     ;; function which will recursively expand nested components.
     (components/component-definition? element)
     (let [[element-name
            component
            component-params] (components/extract-component
                               element local-components)

           adjusted-params    (map-recursive-component-output
                               component-params
                               local-components
                               component-path
                               apply-to-component-output)]

       (get-recursive-component-output
        element-name
        component
        adjusted-params
        component-path
        local-components
        apply-to-component-output))


     ;; Check if the element is a collection and includes references to a
     ;;  component and if so, extract the component and get the output.
     ;; The output of the component function is then provided to the
     ;; `apply-to-component-output` function which will recursively
     ;; expand nested components.
     (components/references-component? element)
     (map-recursive-component-output
      element
      local-components
      component-path
      apply-to-component-output)


     ;; Otherwise return the element as is.
     :else element)))


(defn- apply-components
  "Uses clojure.walk/postwalk in combination with the `process-component` function to
   walk the given `hiccup-data` and expand any component definitions.

   Will use global component definitions as well as any component definitions in `local-components`

   The `apply-to-component-output` function (last argument) will be applied to component output and
   allows for handling nested components."
  ([hiccup-data local-components apply-to-component-output]
   (apply-components hiccup-data local-components apply-to-component-output []))

  ([hiccup-data local-components apply-to-component-output component-path]

   (clojure.walk/postwalk
    #(process-component
      % local-components apply-to-component-output component-path)
    hiccup-data)))


(defn- ->hiccup-recursive
  "Recurses over `hiccup-data` and handles nested components.

   Calls the `apply-components` function and passes in the same
  `apply-components` function as the last argument which is applied
  to component output to handle nested component structures."
  [hiccup-data local-components]

  (apply-components hiccup-data
                    local-components
                    apply-components))


;; -- Public API --

(defn ->hiccup
  ([hiccup-data local-components]
   (->hiccup-recursive hiccup-data local-components))

  ([hiccup-data]
   (->hiccup hiccup-data {})))


(defn ->html
  ([hiccup-data] (->html hiccup-data {}))

  ([hiccup-data local-components]
   (str
    (hiccup/html (->hiccup hiccup-data local-components)))))


(defn ->html-file
  ([file-path hiccup-data]
   (->html-file file-path hiccup-data {}))

  ([file-path hiccup-data local-components]
   (spit file-path
         (->html hiccup-data local-components))))


(defn component->html
  [component-element-name params local-components]
  (let [hiccup-data (if (not (nil? params))
                      [component-element-name params]
                      [component-element-name])]
    (->html hiccup-data local-components)))


(defn component->html-file
  [file-path component-element-name params local-components]
  (spit file-path
        (component->html component-element-name
                         params
                         local-components)))

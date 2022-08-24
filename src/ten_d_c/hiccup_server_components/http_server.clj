(ns ^:no-doc ten-d-c.hiccup-server-components.http-server
  (:require [ten-d-c.hiccup-server-components.compiler :as compiler]))


(defn hiccup-server-components-response? [response]
  (and (map? response)
       (or (contains? response :hsc/component)
           (contains? response :hsc/html))))


(defn wrap-response-middleware [handler]
  (fn [request]
    (let [response (handler request)]
      (if (hiccup-server-components-response? response)
        (let[html (if-let [component (:hsc/component response)]
                    (compiler/component->html component
                                              (:hsc/params response)
                                              {})
                    (compiler/->html (:hsc/html response)))]
          (assoc response :body html))
        response))))

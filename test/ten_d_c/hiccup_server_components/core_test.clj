(ns ten-d-c.hiccup-server-components.core-test
  (:require [clojure.test :refer :all]
            [ten-d-c.hiccup-server-components.core :as hc]
            [clojure.string :as string]))


(defn register-primary-button []
  (hc/reg-component
   :ux.buttons/primary-button
   (fn [{:keys [text] :as options}]
     [:button.primary (dissoc options :text)
      text])))


(defn register-cancel-button []
  (hc/reg-component
   :ux.buttons/cancel-button
   (fn [{:keys [text] :as options}]
     [:button.cancel (dissoc options :text)
      text])))


(defn assert-no-user-defined-components []
  (is (empty?
       (hc/user-defined-components))))


(deftest core-test

  (testing "all-components"

    (hc/clear-components)

    (assert-no-user-defined-components)

    (register-primary-button)

    (is (= 1 (count (hc/user-defined-components))))

    (is (= :ux.buttons/primary-button
           (:element-name (first (hc/user-defined-components)))))

    (register-cancel-button)

    (is (= 2 (count (hc/user-defined-components))))

    (is (= '(:ux.buttons/cancel-button :ux.buttons/primary-button)
           (sort
            (map :element-name (hc/user-defined-components))))))


  (testing "clear-components"

    (hc/clear-components)

    (assert-no-user-defined-components)

    (hc/reg-component :ux.buttons/primary-button
                      (fn [{:keys [text] :as options}]
                        [:button.primary (dissoc options :text)
                         text]))

    (is (= 1 (count (hc/user-defined-components))))

    (hc/reg-component :ux.buttons/cancel-button
                      (fn [{:keys [text] :as options}]
                        [:button.cancel (dissoc options :text)
                         text]))

    (is (= 2 (count (hc/user-defined-components))))

    (hc/clear-components)

    (assert-no-user-defined-components))


  (testing "get-component-meta-data"

    (hc/clear-components)

    (is (= {:element-name :ux.buttons/primary-button}
           (hc/get-component-meta-data :ux.buttons/primary-button)))

    (is (= {:element-name :ux.buttons/cancel-button}
           (hc/get-component-meta-data :ux.buttons/cancel-button)))

    (register-primary-button)
    (register-cancel-button)

    (testing "Primary button"

      (let [{:keys [element-name
                    component-type
                    namespace
                    file-name
                    line-number] :as meta-data}
            (hc/get-component-meta-data :ux.buttons/primary-button)]

        (is (= '(:element-name
                 :component-type
                 :namespace
                 :file-name
                 :line-number) (keys meta-data)))
        (is (= :ux.buttons/primary-button element-name))
        (is (= "function" component-type))
        (is (= "ten_d_c.hiccup_server_components.core_test" namespace))
        (is (= "core_test.clj" file-name))
        (is (= 8 line-number))))


    (testing "Cancel button"

      (let [{:keys [element-name
                    component-type
                    namespace
                    file-name
                    line-number] :as meta-data}
            (hc/get-component-meta-data :ux.buttons/cancel-button)]

        (is (= '(:element-name
                 :component-type
                 :namespace
                 :file-name
                 :line-number) (keys  meta-data)))
        (is (= :ux.buttons/cancel-button element-name))
        (is (= "function" component-type))
        (is (= "ten_d_c.hiccup_server_components.core_test" namespace))
        (is (= "core_test.clj" file-name))
        (is (= 16 line-number))))


    (testing "Component type"

      (hc/reg-component :ux.test/func (fn [] [:div "Okay"]))

      (is (= "function"
             (:component-type
              (hc/get-component-meta-data :ux.test/func))))

      (hc/reg-component :ux.test/vec [:div "Yes okay cool"])

      (is (= "vector"
             (:component-type
              (hc/get-component-meta-data :ux.test/vec))))

      (hc/reg-component :ux.test/string "Hello world")

      (is (= "string"
             (:component-type
              (hc/get-component-meta-data :ux.test/string))))))


  (testing "reg-component"

    (hc/clear-components)

    (assert-no-user-defined-components)

    (is (= {:element-name :ux.buttons/primary-button}
           (hc/get-component-meta-data :ux.buttons/primary-button)))

    (is (= :ux.buttons/primary-button
           (hc/reg-component :ux.buttons/primary-button
                             (fn [{:keys [text] :as options}]
                               [:button.primary (dissoc options :text)
                                text]))))

    (is (not
         (nil? (hc/get-component-meta-data :ux.buttons/primary-button))))

    (is (= '(:ux.buttons/primary-button)
           (map :element-name (hc/user-defined-components))))

    (is (= {:element-name :ux.buttons/cancel-button}
           (hc/get-component-meta-data :ux.buttons/cancel-button)))

    (is (= :ux.buttons/cancel-button
           (hc/reg-component :ux.buttons/cancel-button
                             (fn [{:keys [text] :as options}]
                               [:button.cancel (dissoc options :text)
                                text]))))

    (is (not
         (nil? (hc/get-component-meta-data :ux.buttons/cancel-button))))

    (is (= '(:ux.buttons/cancel-button :ux.buttons/primary-button)
           (sort
            (map :element-name (hc/user-defined-components)))))))

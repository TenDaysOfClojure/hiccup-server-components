(ns ^:no-doc build
  (:require [clojure.tools.build.api :as build]))

(def base-dir "target")
(def class-dir (str base-dir "/classes"))
(def basis (build/create-basis {:project "deps.edn"
                                :extra "deps_build.edn"}))
(def uber-file (str base-dir "/hiccup-server-components.jar"))


(defn clean [_]
  (build/delete {:path base-dir})
  (println "Build: Deleted base directory:" base-dir))


(defn uber [_]
  (clean nil)

  (build/copy-dir {:src-dirs ["src" "resources"]
                   :target-dir class-dir})

  (println "Build: Compiling uberjar" uber-file)

  (build/compile-clj {:basis basis
                      :src-dirs ["src"]
                      :class-dir class-dir})

  (build/uber {:class-dir class-dir
               :uber-file uber-file
               :basis basis
               :main 'ten-d-c.hiccup-server-components.core})

  (println "Build: Compile complete"))

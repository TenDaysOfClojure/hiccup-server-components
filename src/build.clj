(ns ^:no-doc build
  (:require [clojure.tools.build.api :as build]))

(def lib 'net.clojars.t_d_c/hiccup-server-components)

(def major-version 0)
(def minor-version 18)
(def patch-version (build/git-count-revs nil))

(def version (format "%s.%s.%s"
                    major-version minor-version patch-version))

(def base-dir "target")
(def class-dir (str base-dir "/classes"))
(def basis (build/create-basis {:project "deps.edn" :extra "deps_build.edn"}))
(def uber-file (str base-dir "/hiccup-server-components.jar"))


(defn clean [_]
  (build/delete {:path base-dir})
  (println "Build: Deleted base directory:" base-dir))


(defn prep [_]
  (println "Build: Preping pom.xml and copying source to" class-dir)

  (build/write-pom
   {:class-dir class-dir
    :lib lib
    :version version
    :basis basis
    :scm {:url "https://github.com/TenDaysOfClojure/hiccup-server-components"
          :connection "scm:git:https://github.com/TenDaysOfClojure/hiccup-server-components.git"
          :developerConnection "scm:git:https://github.com/TenDaysOfClojure/hiccup-server-components.git"}
    :src-dirs ["src"]})

  (build/copy-dir {:src-dirs ["src" "resources"]
                   :target-dir class-dir}))


(defn uber [_]
  (println "Build: Compiling uberjar" uber-file)

  (build/compile-clj {:basis basis
                      :src-dirs ["src"]
                      :class-dir class-dir})

  (build/uber {:class-dir class-dir
               :uber-file uber-file
               :basis basis
               :main 'ten-d-c.hiccup-server-components.core})

  (println "Build: Compile complete"))


(defn all [_]
  (clean nil)
  (prep nil)
  (uber nil))

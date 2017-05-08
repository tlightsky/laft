(ns laft.settings
  (:gen-class)
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [me.raynes.fs :as fs])
  (:import  [java.io File FileNotFoundException]))

(defonce sep File/separator)
(def setting-fname "setting.edn")
(def setting (atom {}))

(defn home-path [p]
  (str (System/getProperty "user.dir") sep p))

(defn load-settings! []
  (try
    (let [p (home-path setting-fname)
          s (slurp p)]
      (reset! setting (clojure.edn/read-string s)))
   (catch FileNotFoundException e
     (println "no settings yet,maybe first time open"))))

(defn save-settings! []
  (let [p (home-path setting-fname)
        s (prn-str @setting)]
    (spit p s)))

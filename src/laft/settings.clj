(ns laft.settings
  (:gen-class)
  (:use [laft global])
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [clojure.core.async :as async])
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

(defn add-sync-list! [folder]
  (let [sync-list (or (:sync-list @setting) {})]
    (if (contains? sync-list folder)
      (async/put! message-dialog-chan ["Notice" "Folder already exist"])
      (do
        (swap! setting assoc :sync-list (assoc sync-list folder {}))
        (save-settings!)))))

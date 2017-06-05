(ns laft.settings
  (:gen-class)
  (:use [laft global utils])
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
  (println "saving config...")
  (let [p (home-path setting-fname)
        s (prn-str @setting)]
    (spit p s)))

(defn map-base-name [files]
  (map fs/base-name files))

(defn add-sync-list! [folder]
  (let [sync-list (or (:sync-list @setting) {})]
    (cond
      (contains? sync-list folder)
        (async/put! message-dialog-chan ["Notice" "Folder already exist"])
      (in? (map-base-name (keys sync-list)) (fs/base-name folder))
        (async/put! message-dialog-chan ["Notice" "Folder name already exist"])
      :else
      (do
        (swap! setting assoc :sync-list (assoc sync-list folder {}))
        (save-settings!)))))

(ns laft.folder
  (:gen-class)
  (:use [laft utils watcher global settings]))


(def stop-monitor-folder! stop-monitor!)
(def start-monitor-folder! start-monitor!)

(def folder-sync-flag (atom false))

(defn folder-changed [event filename]
  ;; arrange a rescan all folder for changed files upload
  ;; set a resync flag
  (println event)
  (println filename)
  (swap! folder-sync-flag true))

(defn folder-sync! []
  (when @folder-sync-flag
    (println "syncing...")))

(defn start-sync-interval []
  (set-interval folder-sync! folder-sync-delay))

(defn watch-folders []
 (let [sync-list (:sync-list @setting)]
   (doseq [folder (keys sync-list)]
     (start-monitor-folder! folder folder-changed))))

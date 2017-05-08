(ns laft.watcher
  (:gen-class)
  (:require [clojure-watch.core :refer [start-watch]]))

(defn monitor-dir
  [path]
  (println "monitoring directory: " path)
  (start-watch [{:path path
                 :event-types [:create :modify :delete]
                 :bootstrap (fn [path] (println "Starting to watch " path))
                 :callback (fn [event filename] (println event filename))
                 :options {:recursive true}}]))

(defn start-monitor []
  (let [stop-watch (monitor-dir "E:\\sync")]
  ; (Thread/sleep 20000) ; Manipulate files on the path
  ; (stop-watch)
  ))

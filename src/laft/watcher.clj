(ns laft.watcher
  (:gen-class)
  (:require [clojure-watch.core :refer [start-watch]]))

(defn monitor-dir
  [path f]
  (println "monitoring directory: " path)
  (start-watch [{:path path
                 :event-types [:create :modify :delete]
                 :bootstrap (fn [path] (println "Starting to watch " path))
                 :callback (fn [event filename] (f event filename))
                 :options {:recursive true}}]))

(def monitor-stops (atom {}))

(defn start-monitor [path f]
  (let [stop-watch (monitor-dir path f)]
    (swap! monitor-stops assoc path stop-watch)))

(defn stop-monitor [path]
  (println "stoping monitor " path)
  ((@monitor-stops path)))

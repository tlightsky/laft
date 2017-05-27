(ns laft.core
  (:gen-class)
  (:use [seesaw core swingx keymap util options]
        [laft adapter watcher utils settings global])
  (:require [seesaw.bind :as b]
            [seesaw.dev :as dev]
            [seesaw.dnd :as dnd]
            [me.raynes.fs :as fs]
            [clojure.core.async :as async]))

; (defn add-behaviors [f]
;   (let [{:keys [launch probar]} (group-by-id f)]
;     (listen launch :action
;       (fn [_]
;         (let [s (auto-inc-dec)]
;           (set-interval
;             #(.setValue probar (s)) animation-delay))))))

(defn tab1 []
  (border-panel
    :hgap 5 :vgap 5 :border 5
    :center (web-progress-bar :id :probar :min 0 :max 100)
    :south  (button :id :launch :text "Launch")))

(defn refresh-list! []
  (let [{:keys [folder-list]} (group-by-id @rootpane)
        sync-list (:sync-list @setting)
        model (.getModel folder-list)]
    (.removeAllElements model)
    (doseq [folder (keys sync-list)]
      (.addElement model folder))))

(defn list-box-pane []
  (doto (web-listbox :id :folder-list
    :model []
    :drag-enabled? true
    :drop-mode :insert
    :transfer-handler
    (dnd/default-transfer-handler
      :import [dnd/file-list-flavor (fn [{:keys [target data]}]
                                      ; data is always List<java.io.File>
                                      (doseq [file data]
                                        ;; only accept folder
                                        ;; should confirm
                                        ;; add file path to settings
                                        (if (fs/directory? file)
                                          (do
                                            (add-sync-list! (.getAbsolutePath file))
                                            (refresh-list!))
                                          ; (.. target getModel (addElement file))
                                          (async/put! message-dialog-chan ["Notice" "Only folder could be add."]))
                                        ))]
      :export {
        :actions (constantly :copy)
        :start   (fn [c]
                   (let [file (selection c)]
                     [dnd/file-list-flavor [file]]))
        ; No :finish needed
      }))))

(defn list-tab []
  (border-panel :id :ltab
    :size [300 :by 400]
    :center (scrollable (list-box-pane))))

(defn exit-fn [e]
  (println "saving config...")
  (let [b (.getBounds @rootpane)
        b [(.-x b) (.-y b) (.-width b) (.-height b)]]
    (swap! setting assoc :bounds b))
  (save-settings!)
  (System/exit 0))

(defn start-loops []
  ; (start-monitor)
  (start-message-dialog-loop))

(defn -main
  [& args]
  (println "Hello, Laft!")
  (start-loops)
  (invoke-later
    (web-install)
    (load-settings!)
    (reset! rootpane
      (web-frame :title "Laft"
        ;  :content "Hello, Seesaw"
         :content
         (tabbed-panel
           :tabs [{:title "FolderSync" :content (list-tab)}])
         :on-close :dispose))
    (doto @rootpane
      (listen :window-closed
        exit-fn)
      pack!
      (load-location!)
      show!)
    (refresh-list!)))

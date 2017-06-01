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

(defn delete-action! [e]
  (let [{:keys [folder-list]} (group-by-id @rootpane)
        sync-list (:sync-list @setting)
        item (selection folder-list)]
    (swap! setting assoc :sync-list (dissoc sync-list item))
    (stop-monitor item)
    (save-settings!)
    (refresh-list!)))

(defn open-containing-folder! [e]
  (let [{:keys [folder-list]} (group-by-id @rootpane)
        item (selection folder-list)]
    (desktop-open! (fs/file item))))

(def static-popup
  (web-popup :items
    [["Open containing folder" open-containing-folder!]
     ["Delete" delete-action!]]))

(defn folder-changed []
  ;; rescan all folder for change files to upload
  )

(defn watch-folders []
 (let [sync-list (:sync-list @setting)]
   (doseq [folder (keys sync-list)]
     (start-monitor folder folder-changed))))

(defn list-box-pane []
  (doto
    (web-listbox :id :folder-list
      :model []
      :selection-mode :single
      :drag-enabled? true
      :drop-mode :insert
      :transfer-handler
      (dnd/default-transfer-handler
        :import [dnd/file-list-flavor (fn [{:keys [target data]}]
                                        ; data is always List<java.io.File>
                                        (doseq [file data]
                                          ;; should confirm
                                          (if (fs/directory? file)
                                            (do
                                              (add-sync-list! (.getAbsolutePath file))
                                              (start-monitor (.getAbsolutePath file) folder-changed)
                                              (refresh-list!))
                                            (async/put! message-dialog-chan ["Notice" "Only folder could be add."]))
                                          ))]
        :export {
          :actions (constantly :copy)
          :start   (fn [c]
                     (let [file (selection c)]
                       [dnd/file-list-flavor [file]]))
          ; No :finish needed
        }))
      (web-popup-option-handler static-popup)))

(defn list-tab []
  (border-panel :id :ltab
    :size [300 :by 400]
    :center (scrollable (list-box-pane))))

(defn exit-fn [e]
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
  (invoke-later
    (web-install)
    (load-settings!)
    (start-loops)
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

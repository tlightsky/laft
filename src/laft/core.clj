(ns laft.core
  (:gen-class)
  (:use [seesaw core swingx keymap util options]
        [laft adapter watcher utils settings])
  (:require [seesaw.bind :as b]
            [seesaw.dev :as dev]))

; (defn add-behaviors [f]
;   (let [{:keys [launch probar]} (group-by-id f)]
;     (listen launch :action
;       (fn [_]
;         (let [s (auto-inc-dec)]
;           (set-interval
;             #(.setValue probar (s)) animation-delay))))))
(def rootpane (atom nil))

(defn tab1 []
  (border-panel
    :hgap 5 :vgap 5 :border 5
    :center (web-progress-bar :id :probar :min 0 :max 100)
    :south  (button :id :launch :text "Launch")))

(defn list-box-pane []
  (doto (web-listbox :id :list :model [1 2 3 4])
    (.setEditable true)))

(defn list-tab []
  (border-panel :id :ltab
    ; :size [300 :by 400]
    :center (scrollable (list-box-pane))))

(defn exit-fn [e]
  (println "saving config...")
  (let [b (.getBounds @rootpane)
        b [(.-x b) (.-y b) (.-width b) (.-height b)]]
    (swap! setting assoc :bounds b))
  (save-settings!)
  (System/exit 0))

(defn -main
  [& args]
  (println "Hello, Laft!")
  (start-monitor)
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
      (load-location!)
      (listen :window-closed
        exit-fn)
      show!)))

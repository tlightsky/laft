(ns laft.core
  (:gen-class)
  (:use [seesaw core swingx keymap util options]
        [laft adapter watcher utils settings])
  (:require [seesaw.bind :as b]
            [seesaw.dev :as dev]))

(def animation-delay 47)

(defn auto-inc-dec []
  (let [sq (flatten (repeat (concat (range 0 100) (range 100 0 -1))))
        sqn (atom sq)]
    (fn []
      (let [r (first @sqn)]
        (swap! sqn next)
        r))))

(defn add-behaviors [f]
  (let [{:keys [launch probar]} (group-by-id f)]
    (listen launch :action
      (fn [_]
        (let [s (auto-inc-dec)]
          (set-interval
            #(.setValue probar (s)) animation-delay))))))

(defn tab1 []
  (border-panel
    :hgap 5 :vgap 5 :border 5
    :center (web-progress-bar :id :probar :min 0 :max 100)
    :south  (button :id :launch :text "Launch")))

(defn -main
  [& args]
  (println "Hello, World!")
  (start-monitor)
  (invoke-later
    (web-install)
    (.center)
    (load-settings!)
    (doto
      (web-frame :title "Hello"
          ;  :content "Hello, Seesaw"
           :content
           (tabbed-panel
             :tabs [{:title "FolderSync" :content (tab1)}
                    ])
           :size [800 :by 600]
           :on-close :dispose)
     add-behaviors
    ;  (set-bounds! (default-bounds))
     (listen :window-closed
       (fn [e]
           (println "exiting...")
           (save-settings!)
           (System/exit 0)
           ))
     pack!
     show!)))

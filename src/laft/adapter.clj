(ns laft.adapter
  (:gen-class)
  (:use [seesaw core swingx keymap util options]
        [laft utils settings global]
        [seesaw.widget-options :only [widget-option-provider]]
        [seesaw.make-widget :only [make-widget*]])
  (:require [seesaw.bind :as b]
            [seesaw.dev :as dev]
            [seesaw.font :as font]
            [clojure.core.async :as async])
  (:import [com.alee.laf.rootpane WebFrame]
           [com.alee.laf.progressbar WebProgressBar]
           [com.alee.laf WebLookAndFeel]
           [com.alee.laf.menu WebMenu]
           [com.alee.laf.tabbedpane WebTabbedPane]
           [com.alee.laf.list WebList]
           [com.alee.laf.panel WebPanel]
           [com.alee.laf.optionpane WebOptionPane]
           [com.alee.laf.scroll WebScrollPane]
           [com.alee.laf.menu WebPopupMenu WebMenuItem]
           [javax.swing UIManager]
           [java.awt.event WindowAdapter]
           [java.awt Rectangle Desktop]))
;; adapt seesaw to web-ui

(defn setup-font! []
  (set! WebLookAndFeel/globalControlFont (font/font "宋体-PLAIN-12"))
  (set! WebLookAndFeel/globalAlertFont (font/font "宋体-PLAIN-13"))
  (set! WebLookAndFeel/globalMenuFont (font/font "宋体-PLAIN-12"))
  (set! WebLookAndFeel/globalAcceleratorFont (font/font "宋体-PLAIN-12"))
  (set! WebLookAndFeel/globalTitleFont (font/font "宋体-PLAIN-14"))
  (set! WebLookAndFeel/globalTextFont (font/font "宋体-PLAIN-12"))
  (set! WebLookAndFeel/globalTooltipFont (font/font "宋体-PLAIN-12")))

;; use song font if zh
(defn web-install []
  ;; TODO: discover not zh region
  (setup-font!)
  (UIManager/setLookAndFeel (.getCanonicalName WebLookAndFeel)))

(defn set-bounds! [f r]
  (.setBounds f r))

(defn load-location! [frame]
  (let [bounds (:bounds @setting)
        [a b c d] bounds]
    (if bounds
      (.setBounds frame a b c d)
      (.center frame))
  frame))

(defn message-dialog [title msg]
  (WebOptionPane/showMessageDialog @rootpane msg title WebOptionPane/INFORMATION_MESSAGE))

(defn start-message-dialog-loop []
  (async/go-loop []
    (when-let [[title msg] (async/<! message-dialog-chan)]
      (message-dialog title msg)
      (recur))))

(defn desktop-open! [target]
  (.open (Desktop/getDesktop) target))

(defn double-click? [event]
  (> (.getClickCount event) 1))

(defn web-frame
  [& {:keys [width height visible? size]
      :as opts}]
  (cond-doto ^WebFrame (apply-options (construct WebFrame)
                                    (dissoc opts :width :height :visible?))
    (and (not size)
         (or width height)) (.setSize (or width 100) (or height 100))
    true       (.setLocationByPlatform true)
    visible?   (.setVisible (boolean visible?))))

(defn web-progress-bar
  [& {:keys [orientation value min max] :as opts}]
  (let [sl (construct WebProgressBar)]
    (apply-options sl opts)))

(defn web-tabbed-panel
  [& opts]
  (apply-options (construct WebMenu) opts))

(defn web-listbox
  [& args]
  (apply-options (construct WebList) args))

(defn web-popup
  [& opts]
  (apply-options (construct WebPopupMenu) opts))

(defn- ^WebMenuItem to-menu-item
  [item]
  (let [m (WebMenuItem. ^String (first item))]
    (listen m :action (second item))
    m))

(defn- ^javax.swing.JPopupMenu make-popup [target arg event]
  (cond
    (instance? javax.swing.JPopupMenu arg) arg
    (fn? arg)                              (popup :items (arg event))
    :else (illegal-argument "Don't know how to make popup with %s" arg)))

(defn web-popup-option-handler
  [^java.awt.Component target arg]
  (listen target :mouse
    (fn [^java.awt.event.MouseEvent event]
      (when (.isPopupTrigger event)
        (let [p (make-popup target arg event)
              x (.x (.getPoint event))
              y (.y (.getPoint event))]
          (if (instance? WebList target)
            (.setSelectedIndex target (.locationToIndex target (.getPoint event))))
          (.show p (to-widget event) x y))))))

(def web-popup-options
  (merge
    default-options
    (option-map
      (default-option :items
        (fn [^WebPopupMenu menu items]
          (doseq [item items]
            (if-let [menu-item (to-menu-item item)]
              (.add menu menu-item)
              (if (= :separator item)
                (.addSeparator menu)
                (.add menu (make-widget item))))))))))

(widget-option-provider WebPopupMenu web-popup-options)

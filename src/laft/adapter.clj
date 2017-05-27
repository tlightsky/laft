(ns laft.adapter
  (:gen-class)
  (:use [seesaw core swingx keymap util options]
        [laft utils settings global])
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
           [javax.swing UIManager]
           [java.awt.event WindowAdapter]
           [java.awt Rectangle]))
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

(defn web-frame
  "Create a JFrame. Options:

    :id       id of the window, used by (select).

    :title    the title of the window

    :icon     the icon of the frame (varies by platform)

    :width    initial width. Note that calling (pack!) will negate this setting

    :height   initial height. Note that calling (pack!) will negate this setting

    :size     initial size. Note that calling (pack!) will negate this setting

    :minimum-size minimum size of frame, e.g. [640 :by 480]

    :content  passed through (make-widget) and used as the frame's content-pane

    :visible?  whether frame should be initially visible (default false)

    :resizable? whether the frame can be resized (default true)

    :on-close   default close behavior. One of :exit, :hide, :dispose, :nothing
                The default value is :hide. Note that the :window-closed event is
                only fired for values :exit and :dispose

  returns the new frame.

  Examples:

    ; Create a frame, pack it and show it.
    (-> (frame :title \"HI!\" :content \"I'm a label!\")
      pack!
      show!)

    ; Create a frame with an initial size (note that pack! isn't called)
    (show! (frame :title \"HI!\" :content \"I'm a label!\" :width 500 :height 600))

  Notes:
    Unless :visible? is set to true, the frame will not be displayed until (show!)
    is called on it.

    Call (pack!) on the frame if you'd like the frame to resize itself to fit its
    contents. Sometimes this doesn't look like crap.

  See:
    (seesaw.core/show!)
    (seesaw.core/hide!)
    (seesaw.core/move!)
    http://download.oracle.com/javase/6/docs/api/javax/swing/JFrame.html
  "
  [& {:keys [width height visible? size]
      :as opts}]
  (cond-doto ^WebFrame (apply-options (construct WebFrame)
                                    (dissoc opts :width :height :visible?))
    (and (not size)
         (or width height)) (.setSize (or width 100) (or height 100))
    true       (.setLocationByPlatform true)
    visible?   (.setVisible (boolean visible?))))

(defn web-progress-bar
  "Show a progress-bar which can be used to display the progress of long running tasks.

      (progress-bar ... options ...)

  Besides the default options, options can also be one of:

    :orientation   The orientation of the progress-bar. One of :horizontal, :vertical. Default: :horizontal.
    :value         The initial numerical value that is to be set. Default: 0.
    :min           The minimum numerical value which can be set. Default: 0.
    :max           The maximum numerical value which can be set. Default: 100.
    :paint-string? A boolean value indicating whether to paint a string containing
                   the progress' percentage. Default: false.
    :indeterminate? A boolean value indicating whether the progress bar is to be in
                    indeterminate mode (for when the exact state of the task is not
                    yet known). Default: false.

  Examples:

    ; vertical progress bar from 0 to 100 starting with inital value at 15.
    (progress-bar :orientation :vertical :min 0 :max 100 :value 15)

  Returns a JProgressBar.

  Notes:

  See:
    http://download.oracle.com/javase/6/docs/api/javax/swing/JProgressBar.html

"
  [& {:keys [orientation value min max] :as opts}]
  (let [sl (construct WebProgressBar)]
    (apply-options sl opts)))

(defn web-tabbed-panel
  "Create a JTabbedPane. Supports the following properties:

    :placement Tab placement, one of :bottom, :top, :left, :right.
    :overflow  Tab overflow behavior, one of :wrap, :scroll.
    :tabs      A list of tab descriptors. See below

  A tab descriptor is a map with the following properties:

    :title     Title of the tab or a component to be displayed.
    :tip       Tab's tooltip text
    :icon      Tab's icon, passed through (icon)
    :content   The content of the tab, passed through (make-widget) as usual.

  Returns the new JTabbedPane.

  Notes:

  The currently selected tab can be retrieved with the (selection) function.
  It returns a map similar to the tab descriptor with keys :title, :content,
  and :index.

  Similarly, a tab can be programmatically selected with the
  (selection!) function, by passing one of the following values:

    * A number - The index of the tab to select
    * A string - The title of the tab to select
    * A to-widget-able - The content of the tab to select
    * A map as returned by (selection) with at least an :index, :title, or
      :content key.

  Furthermore, you can be notified for when the active tab changes by
  listening for the :selection event:

    (listen my-tabbed-panel :selection (fn [e] ...))

  See:
    http://download.oracle.com/javase/6/docs/api/javax/swing/JTabbedPane.html
    (seesaw.core/selection)
    (seesaw.core/selection!)
  "
  [& opts]
  (apply-options (construct WebTabbedPane) opts))

(defn web-menu
  "Create a new menu. In addition to all options applicable to (seesaw.core/button)
  the following additional options are supported:

    :items Sequence of menu item-like things (actions, icons, JMenuItems, etc)

  Notes:

  See:
    (seesaw.core/button)
    http://download.oracle.com/javase/6/docs/api/javax/swing/JMenu.html"
  [& opts]
  (apply-options (construct WebMenu) opts))

(defn web-listbox
  "Create a list box (JList). Additional options:

    :model A ListModel, or a sequence of values with which a DefaultListModel
           will be constructed.
    :renderer A cell renderer to use. See (seesaw.cells/to-cell-renderer).

  Notes:

    Retrieving and setting the current selection of the list box is fully
    supported by the (selection) and (selection!) functions.

  See:
    http://download.oracle.com/javase/6/docs/api/javax/swing/JList.html
  "
  [& args]
  (apply-options (construct WebList) args))

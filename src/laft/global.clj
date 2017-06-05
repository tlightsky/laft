(ns laft.global
  (:gen-class)
  (:require [clojure.core.async :as async]))

(def rootpane (atom nil))

(def message-dialog-chan (async/chan))

(def animation-delay 47)

(def folder-sync-delay 1000) ;; 

(ns laft.global
  (:gen-class)
  (:require [clojure.core.async :as async]))

(def rootpane (atom nil))

(def message-dialog-chan (async/chan))

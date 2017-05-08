(ns laft.utils
  (:gen-class)
  (:import [java.awt Toolkit Rectangle]))


(defn set-interval [callback ms]
  (future (while true (do (Thread/sleep ms) (callback)))))

(defn screen-size []
  (-> (Toolkit/getDefaultToolkit) .getScreenSize))

(def default-width 700)
(def default-height 350)
(defn default-bounds []
  (let [s (screen-size)
        c (fn [a b] (/ (- a b) 2))]
    (Rectangle. (c (.-width s) default-width) (c (.-height s) default-height) default-width default-height)))
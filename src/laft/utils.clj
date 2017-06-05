(ns laft.utils
  (:gen-class)
  (:import [java.awt Toolkit Rectangle]
           [java.math BigInteger]))

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

(defn auto-inc-dec []
  (let [sq (flatten (repeat (concat (range 0 100) (range 100 0 -1))))
        sqn (atom sq)]
    (fn []
      (let [r (first @sqn)]
        (swap! sqn next)
        r))))

(defn parse-int [s]
   (BigInteger. (re-find  #"\d+" s )))

(defn in?
  "true if coll contains elm"
  [coll elm]
  (some #(= elm %) coll))

(ns laft.sia
  (:gen-class)
  (:use [laft utils])
  (:require [org.httpkit.client :as http]
            [clojure.data.json :as json]))

(def sia-agent "Sia-Agent")
(def http-endpoint "http://localhost:9980/")
(defn sia-url [path]
  (str http-endpoint path))

(defn sia-get [path]
  @(http/get (sia-url path) {:user-agent sia-agent}))

(defn sia-post [path query-params]
  (let [param {:user-agent sia-agent :query-params query-params}]
    @(http/post (sia-url path) param)))

(defn sia-renter-files []
  (let [r (sia-get "renter/files")
        b (:body r)]
    (json/read-str b)))

(defn sia-renter-contracts []
  (let [r (sia-get "renter/contracts")
        b (:body r)]
    (json/read-str b)))

(defn sia-upload [path sia-path]
  (sia-post (str "renter/upload/" sia-path) {"source" path}))

(defn sia-delete [sia-path]
  (sia-post (str "renter/delete/" sia-path)))

(defn sia-wallet []
  (let [r (sia-get "wallet")
        b (:body r)]
    (json/read-str b)))

(defn hasting-to-siacoin [h]
  (* h 1e-24))

(defn sia-balance []
  (hasting-to-siacoin
    (parse-int ((sia-wallet) "confirmedsiacoinbalance"))))

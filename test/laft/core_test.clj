(ns laft.core-test
  (:require [clojure.test :refer :all]
            [laft.core :refer :all]))

(deftest auto-inc-dec-test
  (testing
    (let [c (auto-inc-dec)]
      (and (= 0 (c)) (= 1 (c))))))

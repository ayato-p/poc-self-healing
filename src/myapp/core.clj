(ns myapp.core
  (:gen-class)
  (:require [myapp.calc-average :refer [report]]
            [myapp.candidates]
            [myapp.healing :refer [with-healing]]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (with-healing
    (report [])))


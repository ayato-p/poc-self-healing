(ns myapp.calc-average
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))

(declare clean-bad-data
         calc-average
         display-report)

(defn clean-bad-data [earings]
  (filter number? earings))

(s/def ::earnings
  (s/cat :elements (s/coll-of any?)))

(s/def ::cleaned-earnings
  (s/with-gen
    (s/cat :clean-elements (s/coll-of number?))
    #(gen/return [[1 2 3 4 5]])))

(comment
  (s/exercise ::cleaned-earnings 2)
  ;; => ([[[1 2 3 4 5]] {:clean-elements [1 2 3 4 5]}] [[[1 2 3 4 5]] {:clean-elements [1 2 3 4 5]}])
  )

(s/fdef clean-bad-data
  :args ::earnings
  :ret ::cleaned-earnings)

(defn calc-average [earnings]
  (/ (apply + earnings) (count earnings)))

(s/def ::average number?)

(s/fdef calc-average
  :args ::cleaned-earnings
  :ret ::average)

(s/def ::report-format string?)

(defn display-report [avg]
  (str "The average is " avg))

(s/fdef display-report
  :args (s/cat :elements ::average)
  :ret ::report-format)

(defn report [earnings]
  (-> earnings
      (clean-bad-data)
      (calc-average)
      (display-report)))

(s/fdef report
  :args ::earnings
  :ret ::report-format)

(comment
  (report [1 2 3 4 5])
  ;; => "The average is 3"

  (report [])
  ;; => Execution error (ArithmeticException) at myapp.self-healing/calc-average (form-init6038618853619017788.clj:30).
  ;;    Divide by zero
  )


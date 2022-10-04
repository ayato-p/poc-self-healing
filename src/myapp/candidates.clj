(ns myapp.candidates
  (:require [clojure.spec.alpha :as s]))

(s/def ::numbers (s/cat :elements (s/coll-of number?)))

(s/def ::result number?)

(defn better-calc-average [earings]
  (if (seq earings)
    (/ (apply + earings) (count earings))
    0))

(s/fdef better-calc-average
  :args ::numbers
  :ret ::result)

(defn bad-calc-average [earings]
  0)

(s/fdef bad-calc-average
  :args ::numbers
  :ret ::result)

(defn bad-calc-ave [earings]
  1)

(s/fdef bad-calc-ave
  :args ::numbers
  :ret ::result)


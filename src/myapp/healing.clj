(ns myapp.healing
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))

(defn get-spec-data [spec-symb]
  (let [[_ _ args _ ret _ fn] (s/form spec-symb)]
    {:args args
     :ret ret
     :fn fn}))

(defn failing-function-name [e]
  (as-> (.getStackTrace e) ?
    (map #(.getClassName %) ?)
    (filter #(str/starts-with? % "myapp.calc_average") ?)
    (first ?)
    (str/replace-first ? #"\$" "/")
    (str/replace ? #"_" "-")))

(defn spec-inputs-match? [args1 args2 input]
  (println "****Comparing args" args1 args2 "with input" input)
  (and (s/valid? args1 input)
       (s/valid? args2 input)))

(defn- try-fn [f input]
  (try
    (apply f input)
    (catch Exception e :failed)))

(defn spec-return-match? [fname c-fspec orig-fspec failing-input candidate]
  (let [rcandidate (resolve candidate)
        orig-fn (resolve (symbol fname))
        result-new (try-fn rcandidate failing-input)
        [[seed]] (s/exercise (:args orig-fspec) 1)
        result-old-seed (try-fn rcandidate seed)
        result-new-seed (try-fn orig-fn seed)]
    (println "****Comparing seed " seed "with new function")
    (println "****Result: old" result-old-seed "new" result-new-seed)
    (and (not= :failed result-new)
         (s/valid? (:ret c-fspec) result-new)
         (s/valid? (:ret orig-fspec) result-new)
         (= result-old-seed result-new-seed))))

(defn spec-matching? [fname orig-fspec failing-input candidate]
  (println "---------")
  (println "**Looking at candidate " candidate)
  (let [c-fspec (get-spec-data candidate)]
    (and (spec-inputs-match? (:args c-fspec) (:args orig-fspec) failing-input)
         (spec-return-match? fname c-fspec orig-fspec failing-input candidate))))

(defn find-spec-candidate-match [fname fspec-data failing-input]
  (let [candidates (->> (s/registry)
                        keys
                        (filter #(str/starts-with? (namespace %) "myapp.candidates"))
                        (filter symbol?))]
    (println "Checking candidates " candidates)
    (some #(if (spec-matching? fname fspec-data failing-input %) %) (shuffle candidates))))

(defn self-heal [e input retry-fn]
  (let [fname (failing-function-name e)
        _ (println "ERROR in function" fname (.getMessage e) "-- looking for replacement")
        fspec-data (get-spec-data (symbol fname))
        _ (println "Retriving spec information for function " fspec-data)
        match (find-spec-candidate-match fname fspec-data [input])]
    (if match
      (do
        (println "Found a matching candidate replacement for failing function" fname " for input" input)
        (println "Replacing with candidate match" match)
        (println "--------")
        (eval `(alter-var-root (var ~(symbol fname)) (constantly ~match)))
        (println "Calling function again")
        (let [new-result (retry-fn)]
          (println "Healed function result is:" new-result)
          new-result))
      (println "No suitable replacement for failing function " fname " with input " input ":("))))

(defmacro with-healing [body]
  (let [params (second body)]
    `(let [retry-fn# (fn [] ~body)]
       (try ~body
            (catch Exception e# (self-heal e# ~params retry-fn#))))))

(comment
  (with-healing
    (myapp.calc-average/report [1 2 3 "a"]))
  ;; => "The average is 2" 

  (with-healing
    (myapp.calc-average/report []))
  ;; => "The average is 0"
  )
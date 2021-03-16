(ns bloyot.mtg-analyzer.analysis.core
  (:require
   [cheshire.core :as cheshire]
   [clojure.string :as str]
   [clojure.walk :refer [postwalk]]
   [bloyot.mtg-analyzer.analysis.colors :as colors]))

(defn load-set
  "Load the set json file and return it as a clojure map"
  [set-name]
  (-> (str "resources/sets/" set-name ".json")
      slurp
      (cheshire/parse-string true)))

(defn stringify-vector-keys
  "Recursively transforms all map keys that are vectors to strings."
  [m]
  (let [key-fn (fn [[k v]] (if (vector? k) [(str/join "_" k) v] [(name k) v]))
        walk-fn (fn [x] (if (map? x) (into {} (map key-fn x)) x))]
    (postwalk walk-fn m)))

(defn output
  "Take the output data, serialize it to json, and write it to the file."
  [file-name data]
  (as-> data d
    (stringify-vector-keys d)
    (cheshire/generate-string d {:pretty true})
    (spit (str "resources/output/" file-name ".json") d)))

(defn analyze-set
  "Produce a json file for the given set with the output of the analysis
  we want to run."
  [set-name]
  (let [set-data (:data (load-set set-name))
        cards (:cards set-data)]
    (output
     set-name
     {:color-distribution (colors/color-distribution cards)})))


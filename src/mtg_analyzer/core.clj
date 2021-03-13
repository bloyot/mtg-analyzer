(ns mtg-analyzer.core
  (:require [cheshire.core :as cheshire]
            [clojure.string :as str]
            [clojure.walk :refer [postwalk]]))

(defn load-set
  "Load the set json file and return it as a clojure map"
  [set-name]
  (-> (str "resources/sets/" set-name ".json")
      slurp
      (cheshire/parse-string true)))

(defn type-distribution
  "Return the total number of cards, and the cards counts by type.
  Cards is a sequence of card maps."
  [cards]
  (let [cards-by-type (group-by :types cards)]
    (apply merge
           {:total (count cards)}
           (into {} (map (fn [[type cards]] [type (count cards)]) cards-by-type)))))

(defn color-distribution
  "Return the color distribution for a set of cards. Cards is a sequence
  of card maps."
  [cards]
  (let [cards-by-color (group-by :colorIdentity cards)
        distribution-fn (fn [[colors cards]] [colors (type-distribution cards)])]
    (as-> cards-by-color cards
        (map distribution-fn cards)
        (into {} cards)
        (group-three-plus-colors cards) 
        (clojure.set/rename-keys cards {[] :colorless}))))

(defn stringify-vector-keys
  "Recursively transforms all map keys that are vectors to strings."
  [m]
  (let [key-fn (fn [[k v]] (if (vector? k) [(str/join "_" k) v] [(name k) v]))
        walk-fn (fn [x] (if (map? x) (into {} (map key-fn x)) x))]
    (postwalk walk-fn m)))

(defn group-three-plus-colors
  [distribution]
  (let [three-plus-colors (->> distribution
                              (filter #(<= 3 (count (first %))))
                              vals
                              (apply merge-with +))]
    (as-> distribution d
      (into {} (remove #(<= 3 (count (first %))) d))
      (assoc d :three-plus-colors three-plus-colors))))

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
     {:color-distribution (color-distribution cards)})))


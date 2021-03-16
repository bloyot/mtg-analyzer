(ns bloyot.mtg-analyzer.analysis.colors
  (:require
   [bloyot.mtg-analyzer.analysis.types :as types]
   [clojure.set :as set]))

(defn group-three-plus-colors
  [distribution]
  (let [three-plus-colors (->> distribution
                               (filter #(<= 3 (count (first %))))
                               vals
                               (apply merge-with +))]
    (as-> distribution d
      (into {} (remove #(<= 3 (count (first %))) d))
      (assoc d :three-plus-colors three-plus-colors))))

(defn color-distribution
  "Return the color distribution for a set of cards. Cards is a sequence
  of card maps."
  [cards]
  (let [cards-by-color (group-by :colorIdentity cards)
        distribution-fn (fn [[colors cards]] [colors (types/type-distribution cards)])]
    (as-> cards-by-color cards
      (map distribution-fn cards)
      (into {} cards)
      (group-three-plus-colors cards)
      (set/rename-keys cards {[] :colorless}))))

(ns bloyot.mtg-analyzer.analysis.types)

(defn type-distribution
  "Return the total number of cards, and the cards counts by type.
  Cards is a sequence of card maps."
  [cards]
  (let [cards-by-type (group-by :types cards)]
    (apply merge
           {:total (count cards)}
           (into {} (map (fn [[type cards]] [type (count cards)]) cards-by-type)))))

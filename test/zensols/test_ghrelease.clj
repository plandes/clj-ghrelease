(ns zensols.test-ghrelease
  (:require [clojure.test :refer :all])
  (:require [zensols.ghrelease.dist :refer :all])
  (:require [clojure.math.combinatorics :as combo]))

(defn- no-incorrect-sorts? [correct]
  (->> correct
       combo/permutations
       (map sort-version-tags)
       (map #(= correct %))
       (filter false?)
       empty?))

(deftest test-version-sorting
  (let [correct-same-pos ["v0.0.1" "v0.0.2" "v0.0.3"]
        correct-same-pos2 ["v0.1.0" "v0.2.0" "v0.3.0"]
        multi-level ["v0.0.3" "v0.0.4" "v0.1.0" "v0.1.1" "v0.9.0" "v1.0.2" "v2.0.2"]
        optional-v ["0.0.1" "v0.0.2" "0.0.3"]]
    (is (no-incorrect-sorts? correct-same-pos))
    (is (no-incorrect-sorts? correct-same-pos2))
    (is (no-incorrect-sorts? multi-level))
    (is (no-incorrect-sorts? optional-v))))

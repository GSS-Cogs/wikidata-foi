(ns wikidata-foi.core-test
  (:require [clojure.test :refer :all]
            [wikidata-foi.core :refer :all]))
  ;;(:import [grafter.rdf.protocols Quad]))

(deftest rdf-generation-test
  (let [quads (foi "wikidata.csv")]
    (testing "Generates statements"
      (is (< 0 (count quads)))))) ;;(instance? (first quads) Quad)))))

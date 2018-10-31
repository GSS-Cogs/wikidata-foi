(ns wikidata-foi.core-test
  (:require [clojure.test :refer :all]
            [wikidata-foi.core :refer :all]
            [clojure.java.io :as io]))
  ;;(:import [grafter.rdf.protocols Quad]))

(deftest get-map-test
  (let [map (get-map "http://commons.wikimedia.org/data/main/Data:Glasgow.map")]
    (testing "Returns boundary as Well-Known-Text "
      (is (.startsWith map "MULTIPOLYGON (((-4.19051 55.88871")))))

(deftest rdf-generation-test
  (let [quads (main)]
    (testing "Generates statements"
      (is (< 0 (count quads))))));;(instance? (first quads) Quad)))))

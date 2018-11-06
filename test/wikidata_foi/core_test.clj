(ns wikidata-foi.core-test
  (:require [clojure.test :refer :all]
            [grafter.extra.repository :refer [with-repository-containing]]
            [grafter.rdf.repository :refer [->connection query]]
            [wikidata-foi.core :refer :all]
            [clojure.java.io :as io]))

(def select-austria
  (str "PREFIX foi: <http://publishmydata.com/def/ontology/foi/>
        PREFIX geosparql: <http://www.opengis.net/ont/geosparql#>
        SELECT * WHERE {
         ?f a foi:Feature;
            foi:displayName 'Austria';
            geosparql:hasGeometry [
              a geosparql:Geometry;
              geosparql:asWKT ?wkt
            ]
           .
        }"))

(def select-world
  (str "PREFIX foi: <http://publishmydata.com/def/ontology/foi/>"
       "SELECT * WHERE {"
       "  ?f foi:displayName 'Whole world';"
       "    ?p ?o ."
       "}"))

(deftest pipeline-cord-test
  (with-repository-containing [test-repo (pipeline-cord "test/resources/eg-wikidata.csv")]
    (with-open [connection (->connection test-repo)]
      (testing "Generates complete data for Austria"
        (let [austria (doall (query connection select-austria))]
          (is (not (empty? austria)))))
          ;;(is (= [] austria)))))))
      (testing "Generates partial data for Belgium")
      (testing "Generates only root-data for World"
        (let [world (doall (query connection select-world))]
          (testing "has no parent or within relations"
            (let [world-properties (->> world (map (comp str :p)))]
              (is (empty? (filter #{"http://publishmydata.com/def/ontology/foi/parent"
                                    "http://publishmydata.com/def/ontology/foi/within"} world-properties))))))))))


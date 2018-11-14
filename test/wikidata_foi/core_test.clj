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
            foi:active 'true'^^<http://www.w3.org/2001/XMLSchema#boolean>;
            foi:memberOf <http://gss-data.org.uk/def/collection/country>;
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

(def select-countries
  (str "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
       "PREFIX foi: <http://publishmydata.com/def/ontology/foi/>"
       "PREFIX ui: <http://www.w3.org/ns/ui#>"
       "SELECT * WHERE {"
       "  ?c a foi:AreaCollection;"
       "    rdfs:label 'Countries';"
       "    foi:singularDisplayName 'Country';"
       "    ui:sortPriority 2;"
       "  ."
       "}"))

(deftest pipeline-cord-test
  (with-repository-containing [test-repo (pipeline-cord "test/resources/eg-collections.csv"
                                                        "test/resources/eg-features.csv")]
    (with-open [connection (->connection test-repo)]
      (testing "Generates a Countries collection"
        (let [countries (doall (query connection select-countries))]
          (is (not (empty? countries)))))
      (testing "Generates a complete feature for Austria"
        (let [austria (doall (query connection select-austria))]
          (is (not (empty? austria)))))
          ;;(is (= [] austria)))))))
      (testing "Generates partial data for Belgium")
      (testing "Generates only root-data for World"
        (let [world (doall (query connection select-world))]
          (testing "has no parent relations"
            (let [world-properties (->> world (map (comp str :p)))]
              (is (empty? (filter #{"http://publishmydata.com/def/ontology/foi/parent"} world-properties))))))))))

(comment
  ;; for debugging
  (->> (pipeline-cord "test/resources/eg-wikidata.csv")
       (filter (fn [[s p o c]] (= (str s) "http://gss-data.org.uk/def/concept/ons-trade-areas-cord/AT")))
       (map (fn [[s p o c]] { p o}))))

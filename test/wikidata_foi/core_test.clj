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

(deftest rdf-generation-test
  (with-repository-containing [test-repo (main "test/resources/eg-wikidata.csv" "test/resources/maplinks.csv")]
    (with-open [connection (->connection test-repo)]
      (testing "Generates complete data for Austria"
        (let [austria (doall (query connection select-austria))]
          (is (not (empty? austria)))))
          ;;(is (= [] austria)))))))
      (testing "Generates partial data for Belgium"))))


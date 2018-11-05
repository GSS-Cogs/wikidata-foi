(ns wikidata-foi.core
  (:require [wikidata-foi.foi :as foi]
            [table2qb.util :refer [tempfile create-metadata-source]]
            [table2qb.csv :refer [write-csv]]
            [clojure.java.io :as io]
            [csv2rdf.csvw :as csvw]
            [wikidata-foi.serialise :as serialise])
  (:import [java.io File]
           [java.net URI]))

(defn default-graph [triples graph]
  (map (fn [quad] (update quad :c (fn [c] (or c graph)))) triples))

(defn write-csvw [data file]
  "Writes csvw data to a file"
  (with-open [writer (io/writer file)]
    (write-csv writer data)))

(defn pipeline [codes name-singular name-plural collection-slug sort-priority]
  "Generates FOI RDF given tabular inputs for codes and a map lookup
  together with a name and slug for the collection"
  (let [csvw-data (foi/collection codes)
        csvw-data-file (tempfile "features" ".csv")]
    (write-csvw csvw-data csvw-data-file)
    (let [csvw-metadata (foi/collection-metadata (.toURI csvw-data-file) name-singular name-plural collection-slug sort-priority)
          csvw-metadata-source (create-metadata-source csvw-data-file csvw-metadata)]
      (csvw/csv->rdf nil csvw-metadata-source {:mode :standard}))))

(defn pipeline-cord
  ([]
   (pipeline-cord "resources/cord-geographies-wikidata.csv"))
  ([codes-file]
   (with-open [codes-rdr (io/reader codes-file)]
     (-> (pipeline codes-rdr "CORD Geography" "CORD Geographies" "ons-trade-areas-cord" 1)
         (default-graph (URI. (str "http://gss-data.org.uk/graph/cord-geography-foi")))))))

(defn -main
  ([]
   (serialise/quads-to-file "cord-foi.nq" (pipeline-cord)))
  ([codes-file output-file]
   (serialise/quads-to-file output-file (pipeline-cord codes-file))))
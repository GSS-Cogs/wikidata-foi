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

(defn collections-pipeline [collections]
  "Generates foi:FeatureCollection"
  (let [csvw-data (foi/collections collections)
        csvw-data-file (tempfile "collections" ".csv")]
    (write-csvw csvw-data csvw-data-file)
    (let [csvw-metadata (foi/collections-metadata (.toURI csvw-data-file))
          csvw-metadata-source (create-metadata-source csvw-data-file csvw-metadata)]
      (csvw/csv->rdf nil csvw-metadata-source {:mode :standard}))))

(defn features-pipeline [features scheme-slug]
  "Generates FOI RDF given tabular inputs for codes and a map lookup
  together with a name and slug for the collection"
  (let [csvw-data (foi/features features)
        csvw-data-file (tempfile "features" ".csv")]
    (write-csvw csvw-data csvw-data-file)
    (let [csvw-metadata (foi/features-metadata (.toURI csvw-data-file) scheme-slug)
          csvw-metadata-source (create-metadata-source csvw-data-file csvw-metadata)]
      (csvw/csv->rdf nil csvw-metadata-source {:mode :standard}))))

(defn pipeline-cord
  ([]
   (pipeline-cord "resources/cord-collections-two-level.csv" "resources/cord-features-two-level.csv"))
  ([collections-file features-file]
   (-> (concat (with-open [collections-rdr (io/reader collections-file)]
                 (collections-pipeline collections-rdr))
               (with-open [features-rdr (io/reader features-file)]
                 (features-pipeline features-rdr "ons-trade-areas-cord")))
       (default-graph (URI. (str "http://gss-data.org.uk/graph/cord-geography-foi"))))))

(defn -main
  ([]
   (serialise/quads-to-file "cord-foi.nq" (pipeline-cord)))
  ([collections-file features-file output-file]
   (serialise/quads-to-file output-file (pipeline-cord collections-file features-file))))

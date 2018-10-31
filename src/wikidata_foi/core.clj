(ns wikidata-foi.core
  (:require [wikidata-foi.geometry :as geo]
            [wikidata-foi.foi :as foi]
            [table2qb.util :refer [tempfile create-metadata-source]]
            [table2qb.csv :refer [write-csv]]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clj-http.client :as client]
            [csv2rdf.csvw :as csvw])
  (:import [java.io File]))

(defn get-map [url]
  "Gets GeoJSON boundary from url and transforms to WKT"
  (-> (client/get url)
      :body
      json/read-str
      (get "data")
      json/write-str
      geo/json->wkt))

(defn write-csvw [data file]
  "Writes csvw data to a file"
  (with-open [writer (io/writer file)]
    (write-csv writer data)))

(defn pipeline [codes maps name-singular name-plural collection-slug sort-priority]
  "Generates FOI RDF given tabular inputs for codes and a map lookup
  together with a name and slug for the collection"
  (let [csvw-data (foi/collection codes maps)
        csvw-data-file (tempfile "features" ".csv")]
    (write-csvw csvw-data csvw-data-file)
    (let [csvw-metadata (foi/collection-metadata (.toURI csvw-data-file) name-singular name-plural collection-slug sort-priority)
          csvw-metadata-source (create-metadata-source csvw-data-file csvw-metadata)]
      (csvw/csv->rdf nil csvw-metadata-source {:mode :standard}))))

(defn main []
  (with-open [codes-rdr (io/reader "resources/cord-geographies-wikidata.csv")
              maps-rdr (io/reader "resources/wiki-map.csv")]
    (pipeline codes-rdr maps-rdr "CORD Geography" "CORD Geographies" "cord-geographies" 1)))
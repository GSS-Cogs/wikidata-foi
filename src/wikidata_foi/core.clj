(ns wikidata-foi.core
  (:require [wikidata-foi.geometry :as geo]
            [table2qb.pipelines.codelist :refer [codes]]
            [table2qb.util :refer [tempfile create-metadata-source]]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clj-http.client :as client]
            [csv2rdf.csvw :as csvw]))

(defn get-map [url]
  "Gets GeoJSON boundary from url and transforms to WKT"
  (-> (client/get url)
      :body
      json/read-str
      (get "data")
      json/write-str
      geo/json->wkt))

(defn add-wkt [m]
  "Add a wkt string to a foi-resource"
  m)

(defn ->foi-rdf [m]
  "Convert a foi-resource to rdf"
  [m])



(comment
  (def foi-metadata)

  (let [csvw-data (tempfile "foi-csvw" ".csv")
        csvw-metadata (foi-metadata (.toURI csvw-data))
        metadata-source (create-metadata-source csvw-data csvw-metadata)]
    (csvw/csv->rdf tabular-source metadata-source {:mode :standard})))

(defn foi
  "Generate a sequence of FOI quads from a codelist"
  [codelist-reader map-url-lookup]
  (->> (codes codelist-reader)
       (map add-wkt)))


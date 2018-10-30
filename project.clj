(defproject wikidata-foi "0.1.0-SNAPSHOT"
  :description "Generate PMD Features of Interest (FOI) from wikidata IDs and geoJSON"
  :url "http://gss-data.org.uk"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [grafter/grafter "0.11.2"]
                 [swirrl/table2qb "0.3.1-SNAPSHOT"]
                 [clj-http "3.9.1"]
                 [org.clojure/data.json "0.2.6"]
                 [org.geotools/gt-geojson "17.1"]
                 [org.geotools/gt-jts-wrapper "17.1"]])

(ns wikidata-foi.foi
  (:require [table2qb.csv :refer [read-csv]]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [grafter.rdf.repository :refer [->connection query sparql-repo]]
            [wikidata-foi.geometry :as geo]
            [clojure.tools.logging :as log]))

(def domain-def "http://gss-data.org.uk/def/")

(defn read-codes [rdr]
  (read-csv rdr {"Label" :label
                 "Wikidata ID" :wikidata_id
                 "Notation" :notation
                 "Parent Notation", :parent_notation}))

(defn fmap [f m]
  (into {} (for [[k v] m] [k (f v)])))

(defn get-maplinks []
  "Gets links for all wikidata maps"
  (log/info "Getting map links from query.wikidata.org")
  (let [map-query (str "SELECT ?geo ?map WHERE {"
                       "  SERVICE wikibase:label { bd:serviceParam wikibase:language 'en,en'. }"
                       "?geo wdt:P3896 ?map ."
                       "}")]
    (with-open [connection (->connection (sparql-repo "https://query.wikidata.org/sparql"))]
      (doall (->> (query connection map-query)
                  (map (partial fmap str)))))))


(defn map-lookup
  ([]
   (map-lookup (get-maplinks)))
  ([maplinks]
   (->> maplinks
        (map (fn [row]
               (let [id (second (re-find #"/entity/(.*)" (:geo row)))
                     slug (second (re-find #"/Data:(.*)\.map" (:map row)))]
                 {id {:map-slug slug
                      :map-url (:map row)}})))
        (into {}))))


;;(str "http://www.wikidata.org/entity/" (:wikidata_id row)
(defn add-map-fn []
  (let [map-lookup (map-lookup)]
    (fn [row]
      (let [{:keys [map-slug map-url]} (map-lookup (:wikidata_id row))]
        (-> row
          (assoc :geometry_uri (when map-slug (str "http://gss-data.org.uk/def/wikidata/" map-slug)))
          (assoc :map_url map-url)
          (dissoc :wikidata_id))))))

(defn get-map [url]
  "Gets GeoJSON boundary from url and transforms to WKT"
  (when url
    (log/info (str "Getting map from " url))
    (try
      (-> (client/get url {:cookie-policy :standard})
          :body
          json/read-str
          (get "data")
          json/write-str
          geo/json->wkt)
      (catch clojure.lang.ExceptionInfo e
        (log/warn (str "- " url " not found!"))))))

(defn add-wkt [row]
  (-> row
    (assoc :well_known_text (get-map (:map_url row)))
    (dissoc :map_url)))

(defn collection [codes]
  "Creates a sequence of hashmaps each describing a FOI, ready for csvw translation"
  (let [add-map (add-map-fn)]
    (->> codes
         read-codes
         (map add-map)
         (map add-wkt))))

(defn collection-metadata [file name-singular name-plural collection-slug sort-priority]
  "Creates a hashmap with csvw-metadata for translating a sequence of FOI to RDF"
  (let [collection-uri (str domain-def "concept-scheme/" collection-slug)
        feature-uri (str domain-def "concept/" collection-slug "/{notation}")
        parent-uri (str domain-def "concept/" collection-slug "/{parent_notation}")]
    {"@context" ["http://www.w3.org/ns/csvw" {"@language" "en"}],
     "@id" collection-uri,
     "url" (str file)
     "http://publishmydata.com/def/ontology/foi/singularDisplayName" name-singular,
     "http://publishmydata.com/def/ontology/foi/pluralDisplayName" name-plural,
     "rdf:type" {"@id" "http://publishmydata.com/def/ontology/foi/AreaCollection"},
     "http://www.w3.org/ns/ui#sortPriority" sort-priority
     "tableSchema"
     {"aboutUrl" feature-uri,
      "columns" [{"name" "label",
                  "titles" "label",
                  "datatype" "string",
                  "propertyUrl" "http://publishmydata.com/def/ontology/foi/displayName"}
                 {"name" "notation",
                  "titles" "notation",
                  "datatype" "string",
                  "propertyUrl" "http://publishmydata.com/def/ontology/foi/code"}
                 {"name" "parent_notation",
                  "titles" "parent_notation",
                  "datatype" "string",
                  "propertyUrl" "http://publishmydata.com/def/ontology/foi/parent",
                  "valueUrl" parent-uri}
                 {"name" "geometry_uri"
                  "titles" "geometry_uri"
                  "datatype" "string"
                  "propertyUrl" "http://www.opengis.net/ont/geosparql#hasGeometry"
                  "valueUrl" "{+geometry_uri}"}
                 {"name" "well_known_text"
                  "titles" "well_known_text"
                  "datatype" {"@id" "http://www.opengis.net/ont/geosparql#wktLiteral"}
                  "aboutUrl" "{+geometry_uri}"
                  "propertyUrl" "http://www.opengis.net/ont/geosparql#asWKT"}
                 {"aboutUrl" "{+geometry_uri}"
                  "propertyUrl" "rdf:type"
                  "valueUrl" "http://www.opengis.net/ont/geosparql#Geometry"
                  "virtual" true}
                 {"propertyUrl" "http://publishmydata.com/def/ontology/foi/memberOf",
                  "valueUrl" collection-uri,
                  "virtual" true}
                 {"propertyUrl" "http://publishmydata.com/def/ontology/foi/within",
                  "valueUrl" parent-uri,
                  "virtual" true}
                 {"propertyUrl" "rdf:type"
                  "valueUrl" "http://publishmydata.com/def/ontology/foi/Feature"
                  "virtual" true}]}}))


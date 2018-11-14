(ns wikidata-foi.foi
  (:require [table2qb.csv :refer [read-csv]]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [grafter.rdf.repository :refer [->connection query sparql-repo]]
            [wikidata-foi.geometry :as geo]
            [clojure.tools.logging :as log]
            [clojure.string :as string]))

(def domain-def "http://gss-data.org.uk/def/")

(defn collection-uri-template [slug-var]
  (str domain-def "collection/{" slug-var "}"))

(defn read-collections [rdr]
  (read-csv rdr {"Slug" :slug
                 "Label Singular" :label_singular
                 "Label Plural" :label_plural
                 "Sort Priority" :sort_priority}))

(defn collections [collections]
  (->> collections
       read-collections))

(defn collections-metadata [filename]
  {"@context" ["http://www.w3.org/ns/csvw" {"@language" "en"}]
   "url" (str filename)
   "tableSchema"
   {"aboutUrl" (collection-uri-template "slug")
    "columns" [{"name" "slug"
                "titles" "slug"
                "datatype" "string"
                "suppressOutput" true}
               {"name" "label_singular"
                "titles" "label_singular"
                "datatype" "string"
                "propertyUrl" "http://publishmydata.com/def/ontology/foi/singularDisplayName"}
               {"name" "label_plural"
                "titles" "label_pural"
                "datatype" "string"
                "propertyUrl" "http://publishmydata.com/def/ontology/foi/pluralDisplayName"}
               {"name" "sort_priority"
                "titles" "sort_priority"
                "datatype" "integer"
                "propertyUrl" "http://www.w3.org/ns/ui#sortPriority"}
               {"propertyUrl" "rdf:type"
                "valueUrl" "http://publishmydata.com/def/ontology/foi/AreaCollection"
                "virtual" true}]}})



(defn read-features [rdr]
  (read-csv rdr {"Label" :label
                 "Wikidata ID" :wikidata_id
                 "Notation" :notation
                 "Parent Notation" :parent_notation
                 "Collection Slug" :collection_slug}))

(defn fmap [f m]
  "Map function over values in map"
  (into {} (for [[k v] m] [k (f v)])))

(defn get-maplinks []
  "Gets links for all wikidata maps"
  (log/info "Getting map links from query.wikidata.org")
  (let [map-query (str "SELECT ?geo ?map WHERE {"
                       "  ?geo wdt:P3896 ?map ."
                       "}")]
    (with-open [connection (->connection (sparql-repo "https://query.wikidata.org/sparql"))]
      (doall (->> (query connection map-query)
                  (map (partial fmap str)))))))

(defn map-lookup
  "Build a lookup from the ID to a map slug and url"
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
  "Create map-adding function (closing over query results)"
  (let [map-lookup (map-lookup)]
    (fn [row]
      (let [{:keys [map-slug map-url]} (map-lookup (:wikidata_id row))]
        (-> row
          (assoc :geometry_uri (when map-slug (str "http://gss-data.org.uk/def/wikidata/" map-slug)))
          (assoc :map_url map-url)
          (dissoc :wikidata_id))))))

(defn get-map [url]
  "Get GeoJSON boundary from url transformed into WKT"
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

(defn add-parent-flags [{:keys [parent_notation] :as row}]
  "Adds a flag to indicate whether a parent is present (for conditionally producing hierarchy properties)"
  (let [has-parent? (if (string/blank? parent_notation) "" "yes")]
    (assoc row :parent has-parent? :within has-parent?)))

(defn features [features]
  "Creates a sequence of hashmaps each describing a FOI, ready for csvw translation"
  (let [add-map (add-map-fn)]
    (->> features
         read-features
         (map add-map)
         (map add-wkt)
         (map add-parent-flags))))

(defn features-metadata [file scheme-slug]
  "Creates a hashmap with csvw-metadata for translating a sequence of FOI to RDF"
  (let [collection-uri (collection-uri-template "collection_slug")
        feature-uri (str domain-def "concept/" scheme-slug "/{notation}")
        parent-uri (str domain-def "concept/" scheme-slug "/{parent_notation}")]
    {"@context" ["http://www.w3.org/ns/csvw" {"@language" "en"}],
     "url" (str file)
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
                  "datatype" "string"
                  "suppressOutput" true}
                 {"name" "collection_slug"
                  "titles" "collection_slug"
                  "datatype" "string"
                  "propertyUrl" "http://publishmydata.com/def/ontology/foi/memberOf"
                  "valueUrl" collection-uri}
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
                 {"name" "parent"
                  "titles" "parent"
                  "propertyUrl" "http://publishmydata.com/def/ontology/foi/parent"
                  "valueUrl" parent-uri}
                 {"name" "within"
                  "titles" "within"
                  "propertyUrl" "http://publishmydata.com/def/ontology/foi/within"
                  "valueUrl" parent-uri}
                 {"aboutUrl" "{+geometry_uri}"
                  "propertyUrl" "rdf:type"
                  "valueUrl" "http://www.opengis.net/ont/geosparql#Geometry"
                  "virtual" true}
                 {"propertyUrl" "rdf:type"
                  "valueUrl" "http://publishmydata.com/def/ontology/foi/Feature"
                  "virtual" true}
                 {"propertyUrl" "http://publishmydata.com/def/ontology/foi/active",
                  "default" "true"
                  "datatype" {"base" "boolean", "format" "true|false"},
                  "virtual" true}]}}))


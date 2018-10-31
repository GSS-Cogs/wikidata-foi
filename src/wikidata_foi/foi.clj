(ns wikidata-foi.foi
  (:require [table2qb.csv :refer [read-csv]]))

(def domain-def "http://gss-data.org.uk/def/")

(defn collection [codes maps]
  "Creates a sequence of hashmaps each describing a FOI, ready for csvw translation"
  (read-csv codes {"Label" :label
                   "Wikidata ID" :wikidata_id
                   "Notation" :notation
                   "Parent Notation", :parent_notation}))

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
                 {"propertyUrl" "http://publishmydata.com/def/ontology/foi/memberOf",
                  "valueUrl" collection-uri,
                  "virtual" true}
                 {"propertyUrl" "http://publishmydata.com/def/ontology/foi/within",
                  "valueUrl" parent-uri,
                  "virtual" true}]}}))


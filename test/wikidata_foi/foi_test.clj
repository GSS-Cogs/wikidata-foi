(ns wikidata-foi.foi-test
  (:require [clojure.test :refer :all]
            [wikidata-foi.foi :refer :all]
            [clojure.java.io :as io]))

(deftest map-lookup-test
  (let [lookup (map-lookup [{:geo "http://www.wikidata.org/entity/Q40"
                             :map "http://commons.wikimedia.org/data/main/Data:Austria.map"}])]
    (testing "Returns slug and url for Austrian map"
      (let [Q40 (get lookup "Q40")]
        (are [field value] (= value (field Q40))
          :map-slug "Austria"
          :map-url "http://commons.wikimedia.org/data/main/Data:Austria.map")))
    (testing "Returns nil for missing maps"
      (is (nil? (get lookup "NA"))))))

(deftest get-map-test
  (let [map (get-map "http://commons.wikimedia.org/data/main/Data:Glasgow.map")]
    (testing "Returns boundary as Well-Known-Text "
      (is (.startsWith map "MULTIPOLYGON (((-4.19051 55.88871")))))

(deftest collection-test
  (testing "Returns a collection of features"
    (with-open [codes-rdr (io/reader "test/resources/eg-wikidata.csv")]
      (let [collection (collection codes-rdr)
            feature (nth collection 1)]
        (are [field value] (= value (field feature))
          :label "Austria"
          :geometry_uri "http://gss-data.org.uk/def/wikidata/Austria"
          :notation "AT"
          :parent_notation "D2")
        (is (.startsWith (:well_known_text feature) "POLYGON"))))))
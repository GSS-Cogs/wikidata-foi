(ns wikidata-foi.foi-test
  (:require [clojure.test :refer :all]
            [wikidata-foi.foi :refer :all]
            [clojure.java.io :as io]))

(deftest collection-test
  (testing "Returns a collection of features"
    (with-open [codes-rdr (io/reader "resources/cord-geographies-wikidata.csv")
                maps-rdr (io/reader "resources/wiki-map.csv")]
      (let [collection (collection codes-rdr maps-rdr)
            feature (nth collection 5)]
        (is (= {:label "Austria" :wikidata_id "Q40" :notation "AT" :parent_notation "D2"}
               feature))))))


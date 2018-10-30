(ns wikidata-foi.geometry
  (:import [org.geotools.geojson.feature FeatureJSON]
           [org.geotools.geometry.jts WKTWriter2]
           [java.io StringReader]))

(defn json->geometry [string]
  (with-open [rdr (StringReader. string)]
    (let [feature-collection (.readFeatureCollection (FeatureJSON.) rdr)
          feature (-> feature-collection .features .next)]
      (.getDefaultGeometry feature))))

(defn ->wkt [geometry]
  (let [dimensions 2]
    (.write (WKTWriter2. dimensions) geometry)))

(def json->wkt (comp ->wkt json->geometry))
(ns wikidata-foi.serialise
  (:require [grafter.rdf :as rdf]
            [grafter.rdf.io :refer [rdf-serializer]]
            [grafter.rdf.formats :as formats]
            [grafter.extra.cell.uri :refer [filenameize]]
            [clojure.java.io :as io]))

(defn quads-to-file [filename quads]
  "Serializes nquads to a file."
  (let [serializer (rdf-serializer filename :format formats/rdf-nquads)]
    (rdf/add serializer quads)))

(defn quads-to-folder [folder quads & {:keys [format] :or {format formats/rdf-nquads}}]
  "Serialize quads by appending to files in a folder. Quad context sets filename."
  { :pre [(empty? (.list (io/file folder))) (.isDirectory (io/file folder))]}
  (doseq [file-quads (partition-by rdf/context quads)]
    (let [filename (str folder "/" (filenameize (rdf/context (first file-quads))) "." (.getDefaultFileExtension format))]
      (with-open [wtr (io/writer filename :append true)]
        (let [serializer (rdf-serializer wtr :format format :append true)]
          (rdf/add serializer file-quads))))))

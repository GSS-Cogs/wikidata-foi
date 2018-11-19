# wikidata-foi

This pipeline generates PMD Feature of Interest data for CORD Geographies.

The input is two files:

- *collections* a [csv with one row per area collections](resources/cord-collections-single-parent.csv)
- *features* a [csv with one row per feature (including wikidata IDs)](resources/cord-features-single-parent.csv)

Run the test pipeline:

    $ lein run test/resources/eg-collections.csv test/resources/eg-features.csv cord-foi-test.nq

Run the full pipeline (WIP):

    $ lein run resources/cord-collections-two-level.csv resources/cord-features-two-level.csv cord-foi.nq

The resources folder contains a world boundary taken from [Natural Earth Vector Data](https://www.naturalearthdata.com/downloads/110m-cultural-vectors/) and processed using QGIS:

- Layer > Add Layer > (natural earth)
- Select All (CTRL-A)
- Deselect Antarctica (CTRL-Click)
- Vector > Geoprocessing Tools > Dissolve (creates layer "Dissolved")
- Select "Dissolved" layer then Vector > getWKT

The resources folder also includes a copy of the [source shapefiles](resources/ne_110m_land.zip) and [qgis project](resources/world-map.qgs) from this process.

It is recommended that future iterations of this pipeline ought to adopt the Natural Earth data directly instead of requesting these geometries one-by-one from wikidata. The processing of country boundaries to form aggregates should also take place within the pipeline instead of using a GUI tool.

The [Jenkinsfile](./Jenkinsfile) describes the overall process.

## License

Copyright © 2018 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

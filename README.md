# wikidata-foi

This pipeline generates PMD Feature of Interest data for CORD Geographies.

The input is two files:

- *collections* a [csv with one row per area collections](resources/cord-collections-single-parent.csv)
- *features* a [csv with one row per feature (including wikidata IDs)](resources/cord-features-single-parent.csv)

Run the test pipeline:

    $ lein run test/resources/eg-collections.csv test/resources/eg-features.csv cord-foi-test.nq

Run the full pipeline (WIP):

    $ lein run resources/cord-collections-two-level.csv resources/cord-features-two-level.csv cord-foi.nq

The resources folder also contains a world boundary taken from [Natural Earth Vector Data](https://www.naturalearthdata.com/downloads/110m-cultural-vectors/) and "dissolved" into a single WKT boundary using QGIS.

The [Jenkinsfile](./Jenkinsfile) describes the overall process.

## License

Copyright © 2018 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

# wikidata-foi

This pipeline generates PMD Feature of Interest data for CORD Geographies.

The input is a [csv with codes including wikidata IDs](resources/cord-geographies-wikidata.csv) (this was prepared using OpenRefine).

Run the test pipeline:

    $ lein run test/resources/eg-wikidata.csv cord-foi-test.nq

Run the full pipeline (WIP):

    $ lein run resources/cord-geographies-wikidata.csv cord-foi.nq

## License

Copyright © 2018 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

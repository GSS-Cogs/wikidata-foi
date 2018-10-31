# wikidata-foi

This pipeline generates PMD Feature of Interest data for CORD Geographies.

The inputs are:

- [csv with codes including wikidata IDs](resources/cord-geographies-wikidata.csv) prepared using OpenRefine
- [lookup from wikidata URI to a map URI](resources/wiki-map.csv) extracted with the query as below:
  `curl -H 'Accept: text/csv' https://query.wikidata.org/sparql\?query\=SELECT%20%3Fgeo%20%3Fmap%20WHERE%20%7B%0A%20%20SERVICE%20wikibase%3Alabel%20%7B%20bd%3AserviceParam%20wikibase%3Alanguage%20%22en%2Cen%22.%20%7D%0A%20%20%0A%20%20%3Fgeo%20wdt%3AP3896%20%3Fmap%20.%0A%7D -o resources/wiki-map.csv`

Run the test pipeline:

   $ lein run test/resources/eg-wikidata.csv test/resources/maplinks.csv cord-foi-test.nq

Run the full pipeline (WIP):

   $ lein run resources/cord-geographies-wikidata.csv resources/wiki-map.csv cord-foi.nq

## License

Copyright © 2018 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

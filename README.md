# wikidata-foi

## Inputs

`curl -H 'Accept: text/csv' https://query.wikidata.org/sparql\?query\=SELECT%20%3Fgeo%20%3Fmap%20WHERE%20%7B%0A%20%20SERVICE%20wikibase%3Alabel%20%7B%20bd%3AserviceParam%20wikibase%3Alanguage%20%22en%2Cen%22.%20%7D%0A%20%20%0A%20%20%3Fgeo%20wdt%3AP3896%20%3Fmap%20.%0A%7D -o resources/wiki-map.csv`

## License

Copyright © 2018 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
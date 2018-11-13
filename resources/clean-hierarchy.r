library(dplyr)
library(readr)
cc <- read_csv("cord-geographies-wikidata.csv")

# overwrite original, removing duplicate rows
write_csv(cc %>% distinct(), "cord-geographies-wikidata.csv")

# overwrite original, removing duplicate rows
write.csv(cc %>% distinct(Notation, .keep_all=T), "cord-geographies-single-parent.csv", row.names=F)

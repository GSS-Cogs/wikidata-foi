library(dplyr)
cc <- read.csv("cord-geographies-wikidata.csv")

# overwrite original, removing duplicate rows
write.csv(cc %>% distinct(), "cord-geographies-wikidata.csv", row.names=F)

# overwrite original, removing duplicate rows
write.csv(cc %>% distinct(Notation, .keep_all=T), "cord-geographies-single-parent.csv", row.names=F)
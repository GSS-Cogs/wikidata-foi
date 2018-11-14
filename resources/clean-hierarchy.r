library(dplyr)
library(readr)
cc <- read_csv("cord-features-wikidata.csv")

# overwrite original, removing duplicate rows
write_csv(cc %>% distinct(), "cord-features-wikidata.csv",na="")

# reduce to one parent per code
write_csv(cc %>% distinct(Notation, .keep_all=T), "cord-features-single-parent.csv", na="")

# reduce to one parent per code, flatten hierarchy to two levels
write_csv(cc %>% distinct(Notation, .keep_all=T) %>% mutate(`Parent Notation`=ifelse(Notation=="A1",NA,"A1")), "cord-features-two-level.csv", na="")

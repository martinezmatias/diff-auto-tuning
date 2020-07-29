### others
limitd = 0000
limitup = 14000#14000
v1 = as.numeric(readLines("/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data/paired_values_size_1.csv"))[limitd:limitup]
v2 = as.numeric(readLines("/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data/paired_values_size_2.csv"))[limitd:limitup]

install.packages('magrittr')
install.packages('tidyverse')
install.packages('rstatix')
install.packages('ggpubr')
install.packages('datarium')
library(tidyverse)
library(rstatix)
library(ggpubr)


library(rcompanion)
library(lattice)
library(FSA)
# data.frame(dat17, dat16, check.rows= FALSE)
wilcox.test(v1,v2, paired = TRUE)
m = data.frame(v1, v2)
wilcoxonR(x=m$v1, g=m$v2)


#vda(v1 ~ v2, data=m, ci=TRUE)

library(effsize)
cliff.delta(d = v1, f =  v2)


v1 = as.numeric(readLines("/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data/paired_values_heigth_1.csv"))#[limitd:limitup]
v2 = as.numeric(readLines("/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data/paired_values_heigth_2.csv"))#[limitd:limitup]
wilcox.test(v1,v2, paired = TRUE)
m = data.frame(v1, v2)
wilcoxonR(x=m$v1, g=m$v2)

library(magrittr)
library(tidyverse)
library(rstatix)
library(ggpubr)
stat.test <- m %>% 
  wilcox_test(weight ~ group) %>%
  add_significance()
stat.test

## based on https://www.datanovia.com/en/lessons/wilcoxon-test-in-r/#:~:text=Calculate%20and%20report%20Wilcoxon%20test%20effect%20size%20(r%20value).&text=The%20r%20value%20varies%20from,%3D%200.5%20(large%20effect).
# Load the data

data("genderweight", package = "datarium")
# Show a sample of the data by group
set.seed(123)
genderweight %>% sample_n_by(group, size = 2)

bxp <- ggboxplot(
  genderweight, x = "group", y = "weight", 
  ylab = "Weight", xlab = "Groups", add = "jitter"
)
bxp

stat.test <- genderweight %>% 
  wilcox_test(weight ~ group) %>%
  add_significance()
stat.test
genderweight %>% wilcox_effsize(weight ~ group)

#### Now addapting 

bxp <- ggboxplot(
  m, x = "v1", y = "v2", 
  ylab = "Weight", xlab = "Groups", add = "jitter"
)
bxp

stat.test <- m %>% 
  wilcox_test(v1 ~ v2) %>%
  add_significance()
stat.test
m %>% wilcox_effsize(v1 ~ v2)
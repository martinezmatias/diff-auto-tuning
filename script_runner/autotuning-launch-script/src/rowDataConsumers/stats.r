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
library(effsize)
library(rcompanion)
library(lattice)
library(FSA)
# data.frame(dat17, dat16, check.rows= FALSE)
wilcox.test(v1,v2, paired = TRUE)
m = data.frame(v1, v2)
wilcoxonR(x=m$v1, g=m$v2)


#vda(v1 ~ v2, data=m, ci=TRUE)


cliff.delta(d = v1, f =  v2)


v1 = as.numeric(readLines("/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data/paired_values_heigth_1.csv"))#[limitd:limitup]
v2 = as.numeric(readLines("/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data/paired_values_heigth_2.csv"))#[limitd:limitup]

v2 = as.numeric(readLines("/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/results/paired_values_best_ClassicGumtree_0.1_2000_1_1.csv"))#[limitd:limitup]
v1 = as.numeric(readLines("/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/results/paired_values_default_ClassicGumtree_0.5_1000_1_2.csv"))#[limitd:limitup]


wilcox.test(v1,v2, paired = TRUE , alternative =  "greater")
m = data.frame(v1, v2)
wilcoxonR(x=m$v1, g=m$v2)


ggplot(m, aes(x=v1, y=v2)) + 
  geom_boxplot(fill="slateblue", alpha=0.2) + 
  xlab("cyl")


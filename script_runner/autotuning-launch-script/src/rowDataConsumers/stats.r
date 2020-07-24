limitd = 0000
limitup = 14000#14000
v1 = as.numeric(readLines("/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data/paired_values_size_1.csv"))[limitd:limitup]
v2 = as.numeric(readLines("/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data/paired_values_size_2.csv"))[limitd:limitup]

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

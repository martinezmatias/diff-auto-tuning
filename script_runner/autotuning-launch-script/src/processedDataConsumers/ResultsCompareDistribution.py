import pandas
import scipy.stats
import matplotlib.pyplot as plt
import numpy as np
from src.commons.CohenEffectSize import *
from scipy.stats import wilcoxon, kruskal

def compareDistribution(df, keyBestConfiguration, keyDefaultConfiguration):

	print("Comparing Best {} and Default {} ".format(keyBestConfiguration, keyDefaultConfiguration))

	valuesBestConfiguration = list(df[keyBestConfiguration].values)
	valuesDefaultConfiguration = list(df[keyDefaultConfiguration].values)

	xBest = []
	xDefault = []

	for i in range(0, len(valuesDefaultConfiguration)):
		if not np.isnan(valuesBestConfiguration[i]) and not np.isnan(valuesDefaultConfiguration[i]):
			xBest.append(valuesBestConfiguration[i])
			xDefault.append(valuesDefaultConfiguration[i])

	print("Size best {} size default {}".format(len(xBest), len(xDefault)))

	stat, p =	scipy.stats.mannwhitneyu(xBest, xDefault)

	print(' mannwhitneyu stat=%.3f, p=%.3f' % (stat, p))

	stat, p = wilcoxon(xBest, xDefault)

	print(' wilcoxon stat=%.3f, p=%.3f' % (stat, p))

	stat, p = kruskal(xBest, xDefault)

	print(' kruskal stat=%.3f, p=%.3f' % (stat, p))

	x = [x for x in range(0, len(valuesBestConfiguration)) ]
	#print("best {}".format(valuesBestConfiguration))
	#print("default  {}".format(valuesDefaultConfiguration))

	d = cohend(xDefault, xBest)
	print('Cohens d: %.3f' % d)


	print("Pearson's r {} ".format(scipy.stats.pearsonr(xDefault, xBest)))

	print("Spearman's rho {} ".format(scipy.stats.spearmanr(xDefault, xBest)))

	print("Kendall's tau {} ".format(scipy.stats.kendalltau(xDefault, xBest)))  # Pearson's r

	# https://pingouin-stats.org/index.html
	import pingouin as pg
	print(pg.normality(xBest))
	#print(pg.multivariate_normality(xBest))
	#https://pingouin-stats.org/api.html#effect-sizes
	print("eff size % f" % pg.compute_effsize(xBest, xDefault))
	#https://pingouin-stats.org/generated/pingouin.mwu.html#pingouin.mwu
	stats = pg.mwu(xBest, xDefault, tail='two-sided')
	print("pingouin MWU:\n {}".format(stats))
	#https://pingouin-stats.org/generated/pingouin.wilcoxon.html?highlight=wilcoxon
	stats = pg.wilcoxon(xDefault, xBest, tail='two-sided')
	print("pingouin wilcoxon:\n {}".format(stats))

	#https://docs.scipy.org/doc/scipy/reference/generated/scipy.stats.kruskal.html

def run(pathResults ="../../plots/data/distance_per_diff_GTSPOON.csv"):
	df = pandas.read_csv(pathResults, sep=",")

	columns = list(df.columns)

	# We get the name of the configurations
	allConfig = columns[1:]
	print("columns {}".format(columns))

	compareDistribution(df=df,keyBestConfiguration="ClassicGumtree_0.1_2000_1", keyDefaultConfiguration="ClassicGumtree_0.5_1000_2" )



run()
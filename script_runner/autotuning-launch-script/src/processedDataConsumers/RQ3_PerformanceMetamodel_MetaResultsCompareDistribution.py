import pandas
import scipy.stats
import matplotlib.pyplot as plt
import numpy as np
import math
from src.commons.CohenEffectSize import *
from scipy.stats import wilcoxon, kruskal
from src.commons.Datalocation import *
from src.commons.DiffAlgorithmMetadata import *
def compareDistributions(pathResults ="{}/distance_per_diff_GTSPOON.csv".format(RESULTS_PROCESSED_LOCATION), keyBestConfiguration= defaultConfigurations["ClassicGumtree"], keyDefaultConfiguration="ClassicGumtree_0.5_1000_2"):
	df = pandas.read_csv(pathResults, sep=",")

	columns = list(df.columns)

	# We get the name of the configurations
	print("columns {}".format(columns))

	compareDistribution(df=df,keyBestConfiguration=keyBestConfiguration, keyDefaultConfiguration=keyDefaultConfiguration)


def crossResults(pathDistances, pathSize, keyBestConfiguration, keyDefaultConfiguration):
	print("reading data for {}".format(pathDistances))
	dfDistances = pandas.read_csv(pathDistances, sep=",")
	dfSize = pandas.read_csv(pathSize, sep=",")
	return crossResultsDatasets(dfDistances, dfSize,  keyBestConfiguration, keyDefaultConfiguration)

def crossResultsDatasets(dfDistances, dfSize, keyBestConfiguration, keyDefaultConfiguration):

	columnsDistances = list(dfDistances.columns)

	# We get the name of the configurations
	print("Nr columns In Distances {}".format(len(columnsDistances)))
	diffsDistances = dfDistances['diff']
	allDiffDistances = list(diffsDistances.values)
	print("nr of diffs Distances {}".format(len(allDiffDistances)))

	##now ed size
	columnsSize = list(dfSize.columns)
	print("Nr columns In Sizes {}".format(len(columnsSize)))

	diffsSize = dfSize['diff']
	allDiffSizes = list(diffsSize.values)
	print("nr of diffs Sizes {}".format(len(allDiffSizes)))

	print("Comparing Best {} and Default {} ".format(keyBestConfiguration, keyDefaultConfiguration))

	valuesDistancesBestConfiguration = list(dfDistances[keyBestConfiguration].values)
	valuesDistancesDefaultConfiguration = list(dfDistances[keyDefaultConfiguration].values)

	valuesSizeBestConfiguration = list(dfSize[keyBestConfiguration].values)
	valuesSizeDefaultConfiguration = list(dfSize[keyDefaultConfiguration].values)

	print("nr of valuesBestConfiguration {}".format(len(valuesDistancesBestConfiguration)))
	print("nr of valuesDefaultConfiguration {}".format(len(valuesDistancesDefaultConfiguration)))
	xBest = []
	xDefault = []
	nrNAN = 0
	nrNotNAN = 0
	bothBest = 0
	onlyBestIsBest = 0
	onlyDefaultIsBest = 0
	noneIsBest = 0

	for i in range(0, len(allDiffDistances)):

		iDiffD = allDiffDistances[i]
		iDiffS = allDiffSizes[i]

		#print("{} {} {} ".format(i, iDiffD, iDiffS))

		if not (iDiffD ==  iDiffS):
			print("Error Different diff")
			return

		iBest =  valuesDistancesBestConfiguration[i]
		iDefault = valuesDistancesDefaultConfiguration[i]
		if math.isnan(iBest) and  math.isnan(iDefault):
			nrNAN+=1
		else:
			nrNotNAN+=1

			if iBest == 0 and iDefault == 0:
				bothBest+=1
				if valuesSizeBestConfiguration[i] != valuesSizeBestConfiguration[i]:
					print("Error: values must match")
					return

				#if valuesSizeBestConfiguration[i] < 10:
				#	print("{} Same Size: {} {} ".format(i,iDiffD,valuesSizeBestConfiguration[i] ))

			elif iBest == 0:
				onlyBestIsBest+=1
				#print("{} Best better Size: {} {} {} ".format(i,iDiffD, valuesSizeBestConfiguration[i], valuesSizeDefaultConfiguration[i]))
			elif iDefault == 0:
				onlyDefaultIsBest+=1
				#print("{} Best default Size: {} {} {} ".format(i, iDiffD, valuesSizeDefaultConfiguration[i], valuesSizeBestConfiguration[i]))
			else:
				noneIsBest+=1

	percentageBest = (onlyBestIsBest + bothBest) / nrNotNAN
	percentageDefault = (onlyDefaultIsBest + bothBest) / nrNotNAN
	print("nan {}, non nan {}, both best {} ({:.5f}), only best {} ({:.5f}), only default {} ({:.5f}), noOneIsTheBest {}".format(nrNAN, nrNotNAN, bothBest,
																																 bothBest / nrNotNAN,
																																 onlyBestIsBest,
																																 percentageBest,
																																 onlyDefaultIsBest,
																																 percentageDefault,
																																 noneIsBest))

	#for i in range(0, len(valuesDefaultConfiguration)):
	#	if not np.isnan(valuesBestConfiguration[i]) and not np.isnan(valuesDefaultConfiguration[i]):
	#		xBest.append(valuesBestConfiguration[i])
	#		xDefault.append(valuesDefaultConfiguration[i])

	print("Size best {} size default {}".format(len(xBest), len(xDefault)))


	#saveFile("{}/paired_values_best_{}_1.csv".format(RESULTS_ROW_LOCATION,keyBestConfiguration), xBest)
	#saveFile("{}/paired_values_default_{}_2.csv".format(RESULTS_ROW_LOCATION,keyDefaultConfiguration), xDefault)
	print("END-ok")
	return percentageBest, percentageDefault

def compareDistribution(df, keyBestConfiguration, keyDefaultConfiguration):

	print("Comparing Best {} and Default {} ".format(keyBestConfiguration, keyDefaultConfiguration))

	valuesBestConfiguration = list(df[keyBestConfiguration].values)
	valuesDefaultConfiguration = list(df[keyDefaultConfiguration].values)

	xBest = []
	xDefault = []
	import seaborn as sns
	import matplotlib.pyplot as plt
	for i in range(0, len(valuesDefaultConfiguration)):
		if not np.isnan(valuesBestConfiguration[i]) and not np.isnan(valuesDefaultConfiguration[i]):
			xBest.append(valuesBestConfiguration[i])
			xDefault.append(valuesDefaultConfiguration[i])

	if False:

		print("Size best {} size default {}".format(len(xBest), len(xDefault)))

		# Method 1: on the same Axis
		sns.distplot(xBest, color="skyblue", label="First")
		sns.distplot(xDefault, color="red", label="Second")
		#sns.plt.legend()

		plt.show()

	#ax = sns.violinplot(data=[xBest,xDefault], split=True, orient="v", inner="quartile", cut=0, showfliers = False )
	#ax.set_xticklabels(['first', 'second'])
	plt.boxplot([xBest,xDefault], showfliers=False)
	plt.show()


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
	print("normality best: {}".format(pg.normality(xBest)))
	print("normality default: {}".format(pg.normality(xDefault)))
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

	saveFile("{}/paired_values_best_{}_1.csv".format(RESULTS_ROW_LOCATION,keyBestConfiguration), xBest)
	saveFile("{}/paired_values_default_{}_2.csv".format(RESULTS_ROW_LOCATION,keyDefaultConfiguration), xDefault)

def saveFile(filename, pvalues1):
	fout1 = open(filename, 'w')
	for i in pvalues1:
		fout1.write("{}\n".format(i))
	fout1.flush()
	fout1.close()




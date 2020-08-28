import pandas
import scipy.stats
import matplotlib.pyplot as plt
import numpy as np
import math
from src.commons.CohenEffectSize import *
from scipy.stats import wilcoxon, kruskal
from src.commons.Datalocation import *
from src.commons.DiffAlgorithmMetadata import *
import pingouin as pg
def compareDistributions(pathResults ="{}/dd.csv".format(RESULTS_PROCESSED_LOCATION), keyBestConfiguration = "", keyDefaultConfiguration = ""):
	df = pandas.read_csv(pathResults, sep=",")

	columns = list(df.columns)

	# We get the name of the configurations
	print("columns {}".format(columns))

	compareDistribution(df=df,keyBestConfiguration=keyBestConfiguration, keyDefaultConfiguration=keyDefaultConfiguration)


def crossResults(pathDistances, pathSize, keyBestConfiguration, keyDefaultConfiguration):
	print("reading data for {}".format(pathDistances))
	dfDistances = pandas.read_csv(pathDistances, sep=",")
	print("reading data for {}".format(pathSize))
	dfSize = pandas.read_csv(pathSize, sep=",")
	return crossResultsDatasets(dfDistances, dfSize,  keyBestConfiguration, keyDefaultConfiguration)

def crossResultsDatasets(dfDistances, dfSize, keyBestConfiguration, keyDefaultConfiguration):

	columnsDistances = list(dfDistances.columns)

	# We get the name of the configurations
	diffsDistances = dfDistances['diff']
	allDiffDistances = list(diffsDistances.values)
	#print("nr of diffs Distances {}".format(len(allDiffDistances)))

	##now ed size
	columnsSize = list(dfSize.columns)
	#print("Nr columns In Sizes {}".format(len(columnsSize)))

	diffsSize = dfSize['diff']
	allDiffSizes = list(diffsSize.values)
	#print("nr of diffs Sizes {}".format(len(allDiffSizes)))

	#print("Comparing Best {} and Default {} ".format(keyBestConfiguration, keyDefaultConfiguration))

	valuesDistancesBestConfiguration = list(dfDistances[keyBestConfiguration].values)
	valuesDistancesDefaultConfiguration = list(dfDistances[keyDefaultConfiguration].values)

	valuesSizeBestConfiguration = list(dfSize[keyBestConfiguration].values)
	valuesSizeDefaultConfiguration = list(dfSize[keyDefaultConfiguration].values)


	nrNAN = 0
	nrNotNAN = 0
	bothBest = 0
	onlyBestIsBest = 0
	onlyDefaultIsBest = 0
	noneIsBest = 0

	sizesBest = []
	sizeDefault = []
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

			if not np.isnan(valuesSizeDefaultConfiguration[i]) : #only commented to R
			#if not np.isnan(valuesSizeDefaultConfiguration[i]) and not np.isnan(valuesSizeBestConfiguration[i]):
				sizeDefault.append(int(valuesSizeDefaultConfiguration[i]))

			if not np.isnan(valuesSizeBestConfiguration[i]) :
			#if not np.isnan(valuesSizeDefaultConfiguration[i]) and not np.isnan(valuesSizeBestConfiguration[i]):
				sizesBest.append(int(valuesSizeBestConfiguration[i]))

			if iBest == 0 and iDefault == 0:
				bothBest+=1
				if valuesSizeBestConfiguration[i] != valuesSizeDefaultConfiguration[i]:
					print("Error: values must match")
					return

				#if valuesSizeBestConfiguration[i] < 10:
				#	print("{} Same Size: {} {} ".format(i,iDiffD,valuesSizeBestConfiguration[i] ))

			elif iBest == 0:
				onlyBestIsBest+=1

			elif iDefault == 0:
				onlyDefaultIsBest+=1

			else:
				noneIsBest+=1

	percentageOnlyBest = (onlyBestIsBest) / nrNotNAN
	percentageOnlyDefault = (onlyDefaultIsBest ) / nrNotNAN

	percentageBest = (onlyBestIsBest + bothBest) / nrNotNAN
	percentageDefault = (onlyDefaultIsBest +  bothBest) / nrNotNAN
	print("nan {}, non nan {}, both best {} ({:.5f}), best is best {}  ({:.5f}), only best {} ({:.5f}), default is best {} ({:.5f}),  only default {} ({:.5f}), noOneIsTheBest {} ({:.5f})".format(nrNAN, nrNotNAN, bothBest,
																																 bothBest / nrNotNAN,(onlyBestIsBest + bothBest), percentageBest,
																																 onlyBestIsBest,
																																 percentageOnlyBest, (onlyDefaultIsBest + bothBest), percentageDefault,
																																 onlyDefaultIsBest,
																																 percentageOnlyDefault,
																																 noneIsBest, noneIsBest/nrNotNAN))




	print("END-ok")
	return percentageBest, percentageDefault

def experimentAutoTuning(dfDistances, dfSize, keyBestConfiguration, keyDefaultConfiguration):

	print("Comparing best {} and default {}".format(keyBestConfiguration, keyDefaultConfiguration ))
	iRow = 0

	columns = list(dfSize.columns)
	# We get the name of the configurations
	allConfig = columns[1:]

	print("All config size {}".format(len(allConfig)))

	indexOfConfig = {}


	totalNan = 0

	# we start in 1 because the first is the diff
	for i in range(1, len(columns)):
		indexOfConfig[columns[i]] = i

	totalGlobalSmallerThanDefault = 0
	totalGlobalEqualsThanDefault = 0
	totalGlobalWorseThanDefault = 0

	totalLocalSmallerThanDefault = 0
	totalLocalEqualsThanDefault = 0
	totalLocalWorseThanDefault = 0
	configsLocalSmallerDefaults = {}

	totalLocalSmallerThanGlobal = 0
	totalLocalEqualsThanGlobal = 0
	totalLocalWorstThanGlobal = 0
	configsLocalSmallerGlobal = {}

	for index, row in dfSize.iterrows():

		sizeBestGlobalConfig = row[keyBestConfiguration]
		sizeDefaultConfig = row[keyDefaultConfiguration]

		if math.isnan(sizeBestGlobalConfig) or math.isnan(sizeDefaultConfig):
			#print("{} nan".format(row[0]))
			totalNan+=1
			continue

		iRow += 1

		## by default the size of the default
		localSize = sizeDefaultConfig
		minConfig = keyDefaultConfiguration

		for aConfig in range(1, len(allConfig)+1):
			sizeAConfig = row[aConfig]
			if sizeAConfig < localSize:
				localSize = sizeAConfig
				minConfig = allConfig[aConfig-1]

		if sizeBestGlobalConfig < sizeDefaultConfig:
			totalGlobalSmallerThanDefault += 1
		elif sizeBestGlobalConfig > sizeDefaultConfig:
			totalGlobalWorseThanDefault += 1
		else:
			totalGlobalEqualsThanDefault += 1

		if localSize < sizeDefaultConfig:
			totalLocalSmallerThanDefault += 1
			if minConfig not in configsLocalSmallerDefaults :
				configsLocalSmallerDefaults[minConfig] = 0
			configsLocalSmallerDefaults[minConfig] += 1

		elif localSize > sizeDefaultConfig:
			totalLocalWorseThanDefault += 1 ## it cannot happed
		else:
			totalLocalEqualsThanDefault += 1

		if localSize < sizeBestGlobalConfig:
			totalLocalSmallerThanGlobal += 1

			if minConfig not in configsLocalSmallerGlobal :
				configsLocalSmallerGlobal[minConfig] = 0
			configsLocalSmallerGlobal[minConfig] += 1

		elif localSize > sizeBestGlobalConfig:
			totalLocalWorstThanGlobal += 1 ## it cannot exust
		else:
			totalLocalEqualsThanGlobal += 1


		if iRow % 500 == 0:
			print(iRow)

		#print("{} {} optimized?  {} min size found {} default size {} bestSearch size {}" .format(iRow, row["diff"],  foundSmallerThanDefault, minSize, sizeDefaultConfig, sizeBestSearchConfig ))

		if False and iRow == 100:
			print("!!!!!Stop results!!!!!!")
			break

	print("{} Global vs default:  & {}  ({:.2f}\%) & {} ({:.2f}\%) & {} ({:.2f}\%) ".format(iRow, totalGlobalSmallerThanDefault, (totalGlobalSmallerThanDefault/iRow)* 100,  totalGlobalEqualsThanDefault, (totalGlobalEqualsThanDefault/iRow)* 100 ,  totalGlobalWorseThanDefault, (totalGlobalWorseThanDefault/iRow)* 100))

	print("{} Local vs default:  & {}  ({:.2f}\%) & {} ({:.2f}\%) & {} ({:.2f}\%) ".format(iRow,
																							totalLocalSmallerThanDefault,
																							(
																										totalLocalSmallerThanDefault / iRow) * 100,
																							totalLocalEqualsThanDefault,
																							(
																										totalLocalEqualsThanDefault / iRow) * 100,
																							totalLocalWorseThanDefault,
																							(
																										totalLocalWorseThanDefault / iRow) * 100))

	print("{} Local vs global:  & {}  ({:.2f}\%) & {} ({:.2f}\%) & {} ({:.2f}\%) ".format(iRow,
																							totalLocalSmallerThanGlobal,
																							(
																										totalLocalSmallerThanGlobal / iRow) * 100,
																							totalLocalEqualsThanGlobal,
																							(
																										totalLocalEqualsThanGlobal / iRow) * 100,
																							totalLocalWorstThanGlobal,
																							(
																										totalLocalWorstThanGlobal / iRow) * 100))

	print("configs local  best default".format(configsLocalSmallerDefaults))

	print("configs local  best global {} ".format(configsLocalSmallerGlobal))

	localBestDefault = configsLocalSmallerDefaults.keys()
	bestLocalDefault = list(sorted(localBestDefault, key=lambda x: configsLocalSmallerDefaults[x], reverse=False))
	print("order best local vs default".format(bestLocalDefault))

	localBestGlobal = configsLocalSmallerGlobal.keys()
	bestLocalGlobal = list(sorted(localBestGlobal, key=lambda x: configsLocalSmallerGlobal[x], reverse=False))
	print("order best local vs default".format(bestLocalGlobal))

	print("total diff nan {}".format(totalNan))
	print("END-ok")

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

	print("First avg {} median {} std {} ".format(np.mean(xBest), np.median(xBest), np.std(xBest)))
	print("Second avg {} median {} std {} ".format(np.mean(xDefault), np.median(xDefault), np.std(xDefault)))

	fig, ax = plt.subplots()
	#plt.kde(ax=[xBest,xDefault], legend=False, title='Histogram: A vs. B')
	plt.hist(xBest, alpha=0.5)
	plt.hist(xDefault, alpha=0.5)
	plt.xlim(0, 60)
	#plt.show()

	#ax = sns.violinplot(data=[xBest,xDefault], split=True, orient="v", inner="quartile", cut=0, showfliers = False )
	#ax.set_xticklabels(['first', 'second'])
	plt.boxplot([xBest,xDefault], showfliers=False)
	#plt.show()

	#if True:
	#	return
	print("Number of pairs {} ".format(len(xDefault)))
	stat, p =	scipy.stats.mannwhitneyu( xDefault, xBest, alternative='greater')

	print(' mannwhitneyu stat=%.3f, p=%.3f' % (stat, p))

	stat, p = wilcoxon( xDefault, xBest,alternative='greater')

	print(' wilcoxon stat=%.3f, p=%.3f' % (stat, p))

	stat, p = kruskal( xDefault, xBest,alternative='greater')

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

	print("normality best: {}".format(pg.normality(xBest)))
	print("normality default: {}".format(pg.normality(xDefault)))
	#print(pg.multivariate_normality(xBest))
	#https://pingouin-stats.org/api.html#effect-sizes
	#print("eff size % f" % pg.compute_effsize(xBest, xDefault))
	#https://pingouin-stats.org/generated/pingouin.mwu.html#pingouin.mwu
	#stats = pg.mwu(xBest, xDefault, tail='two-sided')
	#print("pingouin MWU:\n {}".format(stats))
	#https://pingouin-stats.org/generated/pingouin.wilcoxon.html?highlight=wilcoxon
	#stats = pg.wilcoxon(xDefault, xBest, tail='two-sided')
	#print("pingouin wilcoxon:\n {}".format(stats))

	#https://docs.scipy.org/doc/scipy/reference/generated/scipy.stats.kruskal.html

	saveFile("{}/paired_values_best_{}_1.csv".format(RESULTS_ROW_LOCATION,keyBestConfiguration), xBest)
	saveFile("{}/paired_values_default_{}_2.csv".format(RESULTS_ROW_LOCATION,keyDefaultConfiguration), xDefault)

def saveFile(filename, pvalues1):
	print("save at {}".format(filename))
	fout1 = open(filename, 'w')
	for i in pvalues1:
		fout1.write("{}\n".format(i))
	fout1.flush()
	fout1.close()

def crossResultsDatasetsDistanceAnalysis(dfDistances, dfSize, keyBestConfiguration, keyDefaultConfiguration):

	columnsDistances = list(dfDistances.columns)

	# We get the name of the configurations
	diffsDistances = dfDistances['diff']
	allDiffDistances = list(diffsDistances.values)
	#print("nr of diffs Distances {}".format(len(allDiffDistances)))

	##now ed size
	columnsSize = list(dfSize.columns)
	#print("Nr columns In Sizes {}".format(len(columnsSize)))

	diffsSize = dfSize['diff']
	allDiffSizes = list(diffsSize.values)
	#print("nr of diffs Sizes {}".format(len(allDiffSizes)))

	#print("Comparing Best {} and Default {} ".format(keyBestConfiguration, keyDefaultConfiguration))

	valuesDistancesBestConfiguration = list(dfDistances[keyBestConfiguration].values)
	valuesDistancesDefaultConfiguration = list(dfDistances[keyDefaultConfiguration].values)

	valuesSizeBestConfiguration = list(dfSize[keyBestConfiguration].values)
	valuesSizeDefaultConfiguration = list(dfSize[keyDefaultConfiguration].values)

	#print("nr of valuesBestConfiguration {}".format(len(valuesDistancesBestConfiguration)))
	#print("nr of valuesDefaultConfiguration {}".format(len(valuesDistancesDefaultConfiguration)))

	nrNAN = 0
	nrNotNAN = 0
	bothBest = 0
	onlyBestIsBest = 0
	onlyDefaultIsBest = 0
	noneIsBest = 0
	differencesFavorBest = []
	differencesFavorDefault = []

	sizesBest = []
	sizeDefault = []
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

			if not np.isnan(valuesSizeDefaultConfiguration[i]) : #only commented to R
			#if not np.isnan(valuesSizeDefaultConfiguration[i]) and not np.isnan(valuesSizeBestConfiguration[i]):
				sizeDefault.append(int(valuesSizeDefaultConfiguration[i]))

			if not np.isnan(valuesSizeBestConfiguration[i]) :
			#if not np.isnan(valuesSizeDefaultConfiguration[i]) and not np.isnan(valuesSizeBestConfiguration[i]):
				sizesBest.append(int(valuesSizeBestConfiguration[i]))

			if iBest == 0 and iDefault == 0:
				bothBest+=1
				if valuesSizeBestConfiguration[i] != valuesSizeDefaultConfiguration[i]:
					print("Error: values must match")
					return

				#if valuesSizeBestConfiguration[i] < 10:
				#	print("{} Same Size: {} {} ".format(i,iDiffD,valuesSizeBestConfiguration[i] ))

			elif iBest == 0:
				onlyBestIsBest+=1
				#print("{} Best better Size: {} {} {} ".format(i,iDiffD, valuesSizeBestConfiguration[i], valuesSizeDefaultConfiguration[i]))
				if not np.isnan(valuesSizeDefaultConfiguration[i]) and not np.isnan(valuesSizeBestConfiguration[i]) :
					differencesFavorBest.append(float(valuesSizeDefaultConfiguration[i]) - float(valuesSizeBestConfiguration[i]))

			elif iDefault == 0:
				onlyDefaultIsBest+=1
				if not np.isnan(valuesSizeDefaultConfiguration[i]) and not np.isnan(valuesSizeBestConfiguration[i]) :
					differencesFavorDefault.append(float(valuesSizeBestConfiguration[i]) - float(valuesSizeDefaultConfiguration[i]))
			else:
				noneIsBest+=1

	percentageBest = (onlyBestIsBest) / nrNotNAN
	percentageDefault = (onlyDefaultIsBest ) / nrNotNAN
	print("nan {}, non nan {}, both best {} ({:.5f}), only best {} ({:.5f}), only default {} ({:.5f}), noOneIsTheBest {} ({:.5f})".format(nrNAN, nrNotNAN, bothBest,
																																 bothBest / nrNotNAN,
																																 onlyBestIsBest,
																																 percentageBest,
																																 onlyDefaultIsBest,
																																 percentageDefault,
																																 noneIsBest, noneIsBest/nrNotNAN))


	#print("best distance favor {}  ".format(len(differencesFavorBest)))
	#print("default distance favor  {}  ".format(len(differencesFavorDefault)))
	#saveFile("{}/paired_values_best_{}_1.csv".format(RESULTS_ROW_LOCATION,keyBestConfiguration), xBest)
	#saveFile("{}/paired_values_default_{}_2.csv".format(RESULTS_ROW_LOCATION,keyDefaultConfiguration), xDefault)

	print("avg favor best {:.5f} avg favor default {:.5f}".format(np.mean(differencesFavorBest), np.mean(differencesFavorDefault)) )

	if False:
		print("best sizes {}  ".format(len(sizesBest)))
		print("best default {}  ".format(len(sizeDefault)))

		#https://matplotlib.org/3.1.1/gallery/statistics/histogram_multihist.html
		#plt.hist([sizesBest, sizeDefault], alpha=0.5, bins=10000, label=["Best", "Default"]) #histtype = 'step', stacked = True, fill = True,
		plt.hist([sizesBest, sizeDefault], alpha=0.5,bins=5000,histtype = 'step', label=["Best", "Default"])
		plt.legend(["Best", "Default"])

		plt.xlim(0, 50)
		plt.show()

		stat, p = scipy.stats.mannwhitneyu(sizesBest, sizeDefault,alternative='two-sided')

		print('sizes  mannwhitneyu stat=%.3f, p=%.3f' % (stat, p))

		stat, p = scipy.stats.mannwhitneyu(differencesFavorBest, differencesFavorDefault, alternative='two-sided')

		print('differences  mannwhitneyu stat=%.3f, p=%.3f' % (stat, p))

		saveFile("{}/paired_values_best_{}_1.csv".format(RESULTS_ROW_LOCATION,keyBestConfiguration), sizesBest)
		saveFile("{}/paired_values_default_{}_2.csv".format(RESULTS_ROW_LOCATION,keyDefaultConfiguration), sizeDefault)

		plt.boxplot([differencesFavorBest, differencesFavorDefault], showfliers=True)
		plt.legend(["Best", "Default"])
		plt.show()

		plt.hist([differencesFavorBest, differencesFavorDefault], alpha=0.5, bins=5000, histtype='step', label=["Best", "Default"])
		plt.legend(["Best", "Default"])
		plt.xlim(0, 100)
		plt.show()

	print("END-ok")
	return percentageBest, percentageDefault


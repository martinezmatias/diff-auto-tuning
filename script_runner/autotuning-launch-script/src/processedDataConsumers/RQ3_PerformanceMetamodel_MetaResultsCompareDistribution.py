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
def compareDistributions(pathResults ="{}/distance_per_diff_GTSPOON.csv".format(RESULTS_PROCESSED_LOCATION), keyBestConfiguration= defaultConfigurations["ClassicGumtree"], keyDefaultConfiguration="ClassicGumtree_0.5_1000_2"):
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
	iRow = 0

	columns = list(dfSize.columns)
	# We get the name of the configurations
	allConfig = columns[1:]

	print("All config size {}".format(len(allConfig)))

	rowsDiffOptimized = 0

	rowsDiffAutotuneEqualsBest = 0
	rowsDiffAutotuneShortestBest = 0

	indexOfConfig = {}

	casesAutoTuneBeatsBest = []

	autoDiffOptimizedBest = []

	totalNan = 0

	# we start in 1 because the first is the diff
	for i in range(1, len(columns)):
		indexOfConfig[columns[i]] = i

	# count the cases than auto-tune beats the default, which was not beaten by GridSearch
	nrAutoTuneBestDefaultSmallerThanBestOP = 0

	#Autoconfig best than the BestSearch when it's the best (it can be better than the best, but the bestSearch is not the best (default is better))
	nrAutoTuneBestSmallerThanBestWhenItsBest = 0

	totalBestSearchSmallerThanDefault = 0

	for index, row in dfSize.iterrows():

		sizeBestSearchConfig = row[keyBestConfiguration]
		sizeDefaultConfig = row[keyDefaultConfiguration]

		if math.isnan(sizeBestSearchConfig) or math.isnan(sizeDefaultConfig):
			#print("{} nan".format(row[0]))
			totalNan+=1
			continue

		iRow += 1

		minSize = sizeDefaultConfig
		minConfig = keyDefaultConfiguration
		minConfigBeatBest = keyDefaultConfiguration
		foundSmallerThanDefault  = False
		#for aConfig in allConfig:
		autotuneSmallerThanDefaultAndBest = False
		autotuneSmallerThanDefaultEqualsBest = False
		autotuneSmallerThanBestSearchWhenItBest = False

		bestSearchSmallerThanDefault = sizeBestSearchConfig < sizeDefaultConfig
		if bestSearchSmallerThanDefault:
			totalBestSearchSmallerThanDefault+=1


		for aConfig in range(1, len(allConfig)+1):
			sizeAConfig = row[aConfig]
			if sizeAConfig < minSize:
				minSize = sizeAConfig
				minConfig = aConfig
				foundSmallerThanDefault = True

				if sizeAConfig ==  sizeBestSearchConfig and  not autotuneSmallerThanDefaultAndBest:
					autotuneSmallerThanDefaultEqualsBest = True
				elif sizeAConfig < sizeBestSearchConfig:
					# now is smaller
					autotuneSmallerThanDefaultEqualsBest = False
					autotuneSmallerThanDefaultAndBest = True
					##we rest one because the index starts in 1 (to iterate over the row)
					minConfigBeatBest = allConfig[aConfig-1]

					if bestSearchSmallerThanDefault:
						autotuneSmallerThanBestSearchWhenItBest = True


		# finds a small?
		if foundSmallerThanDefault:
			rowsDiffOptimized+=1

			# Only auto is best (and not BestSearch) i.e. Default is better
			if not bestSearchSmallerThanDefault:
				nrAutoTuneBestDefaultSmallerThanBestOP+=1

		if autotuneSmallerThanDefaultEqualsBest:
			rowsDiffAutotuneEqualsBest+=1

		if autotuneSmallerThanBestSearchWhenItBest:
			nrAutoTuneBestSmallerThanBestWhenItsBest+=1

		if autotuneSmallerThanDefaultAndBest:
			rowsDiffAutotuneShortestBest+=1
			casesAutoTuneBeatsBest.append({"diff":row[0],"minConfigFoundAutotune":minConfigBeatBest, "isBestSearchSmallerThanDefault":bestSearchSmallerThanDefault, "minSizeFound":minSize, "sizeDefault":sizeDefaultConfig, "sizeBest":sizeBestSearchConfig, "autotuneSmallerThanBestSearchWhenItBest":autotuneSmallerThanBestSearchWhenItBest })

		if iRow % 500 == 0:
			print(iRow)

		#print("{} {} optimized?  {} min size found {} default size {} bestSearch size {}" .format(iRow, row["diff"],  foundSmallerThanDefault, minSize, sizeDefaultConfig, sizeBestSearchConfig ))

		if False and iRow == 1000:
			print("!!!!!Stop results!!!!!!")
			break

	print("Total diffs {}, autotune - optimized w.r.t Default {} , "
		  "equals best search {}, "
		  "shortest best search {} , "
		  "autotune beats default when it is best {},  "
		  "autoTune beats best search when it the best {},  "
		  "(SearchRelated) total Best search smaller Default {} ".format(
		iRow,
		rowsDiffOptimized,
		rowsDiffAutotuneEqualsBest,
		rowsDiffAutotuneShortestBest ,
		nrAutoTuneBestDefaultSmallerThanBestOP,
		nrAutoTuneBestSmallerThanBestWhenItsBest,
		totalBestSearchSmallerThanDefault))
	print("beating cases ({}): {}".format(len(casesAutoTuneBeatsBest),casesAutoTuneBeatsBest))
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
	plt.show()

	#ax = sns.violinplot(data=[xBest,xDefault], split=True, orient="v", inner="quartile", cut=0, showfliers = False )
	#ax.set_xticklabels(['first', 'second'])
	plt.boxplot([xBest,xDefault], showfliers=False)
	plt.show()

	if True:
		return

	stat, p =	scipy.stats.mannwhitneyu(xBest, xDefault, alternative='two-sided')

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

	print("normality best: {}".format(pg.normality(xBest)))
	print("normality default: {}".format(pg.normality(xDefault)))
	#print(pg.multivariate_normality(xBest))
	#https://pingouin-stats.org/api.html#effect-sizes
	#print("eff size % f" % pg.compute_effsize(xBest, xDefault))
	#https://pingouin-stats.org/generated/pingouin.mwu.html#pingouin.mwu
	stats = pg.mwu(xBest, xDefault, tail='two-sided')
	print("pingouin MWU:\n {}".format(stats))
	#https://pingouin-stats.org/generated/pingouin.wilcoxon.html?highlight=wilcoxon
	stats = pg.wilcoxon(xDefault, xBest, tail='two-sided')
	print("pingouin wilcoxon:\n {}".format(stats))

	#https://docs.scipy.org/doc/scipy/reference/generated/scipy.stats.kruskal.html

	#saveFile("{}/paired_values_best_{}_1.csv".format(RESULTS_ROW_LOCATION,keyBestConfiguration), xBest)
	#saveFile("{}/paired_values_default_{}_2.csv".format(RESULTS_ROW_LOCATION,keyDefaultConfiguration), xDefault)

def saveFile(filename, pvalues1):
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


import numpy as np
import matplotlib.pyplot as plt

from sklearn import datasets
from src.commons.DiffAlgorithmMetadata import *
from sklearn.model_selection import KFold, train_test_split
import pandas
import scipy
import numpy
from scipy.stats import wilcoxon, kruskal


def computeGridSearchKFold(pathResults ="../../plots/data/distance_per_diff.csv", kFold = 5, algorithm = None, defaultId = None):

	print("----\nRunning {} algoritm {}".format(pathResults, algorithm))
	k_fold = KFold(kFold)

	df = pandas.read_csv(pathResults, sep=",")

	columns = list(df.columns)

	# We get the name of the configurations
	allConfig = []
	if algorithm is None:
		allConfig = columns[1:]
	else:
		for aConfig in columns[1:]:
			if algorithm in aConfig:
				allConfig.append(aConfig)

	# we get the first column, which has the diff names
	diffs = df['diff']

	allDiff = list(diffs.values)

	resultsByKTraining = []
	resultsByKTestingSorted = []
	resultsByKTestingByConfig = []

	bestOnTestingByFold = {}
	avgIndexOnTesting = {}

	pearsonTraining = []
	spearmanTraining = []

	# For each Fold
	for k, (train, test) in enumerate(k_fold.split(allDiff)):
		X_train = []
		X_test = []
		print("\n---------Running fold {}".format(k))

		# Create the training dataset
		for i in train:
			X_train.append(allDiff[i])

		# Create the testing dataset
		for i in test:
			X_test.append(allDiff[i])

		print("\nTraining {} ".format(k))

		configsTraining,rankedConfigsTraining = findBestRanking(X_train, allConfig, df)

		print("Configs {}".format(configsTraining))
		print("Ranked {}".format(rankedConfigsTraining))


		resultsByKTraining.append(rankedConfigsTraining)

		print("\nTesting {} ".format(k))

		configsTesting,rankedConfigTesting = findBestRanking(X_test, allConfig, df)
		resultsByKTestingSorted.append(rankedConfigTesting)
		resultsByKTestingByConfig.append(configsTesting)

		print("For information, Compute correlation between  training and testing")
		computeCorrelation(configsTesting, configsTraining)


		for config in rankedConfigTesting:
			if config['c'] not in bestOnTestingByFold:
				bestOnTestingByFold[config['c']] = []
				avgIndexOnTesting[config['c']] = []

			bestOnTestingByFold[config['c']].append(config['bs'])
			avgIndexOnTesting[config['c']].append(config['i'])


		###maybe only compare top X
		print("\nCheck with defaults: ")
		compareDefaultWithBest(rankedConfigsTraining)

	print("\n--End Kfold:")

	## Once we finish, we compute the correlation between the rankings
	print("\nCheck k-fold rankings: ")
	for i in range(0, len(resultsByKTestingSorted)):
		for j in range(0, len(resultsByKTestingSorted)):
			if i > j :
				print("Correlation between testing i:{} j:{} ".format(i,j))
				rp, srho =  computeCorrelation(resultsByKTestingByConfig[i], resultsByKTestingByConfig[j])

				pearsonTraining.append(rp[0])
				spearmanTraining.append(srho[0])

	print("\n Getting the best:")
	## As we have compute K folds, we summarize the performance
	iK = 0
	performanceTestingBestOnTraining = []
	indexTestingBestOnTraining = []

	for iResultsFold in resultsByKTraining:
		print("\nAnalyzing kfold {}".format(iK))
		## We retrieve the perfomrance on testing of  the best config from training
		bestConfigInTraining = iResultsFold[0]
		print("K: {} Best configuration given by the training: {}".format(iK, bestConfigInTraining))

		bestConfigInTesting = None
		#Now, we find it in the corresponding training (not necesary is the best i.e. the first one)
		resultTestingOfK = resultsByKTestingSorted[iK]
		for aConfigFromTesting in resultTestingOfK:
			if aConfigFromTesting['c'] == bestConfigInTraining['c']:
				bestConfigInTesting = aConfigFromTesting



		## find the default in Testing:
		performanceDefaultOnTesting = bestOnTestingByFold[defaultId] ## each position has the data of one fold
		indexDefaultOnTesting = avgIndexOnTesting[defaultId]

		if bestConfigInTesting is not None:
			print("K: {} Default configuration performance {} index {}".format(iK, performanceDefaultOnTesting[iK], indexDefaultOnTesting[iK]))
			print("K: {} Best configuration given by the training on the testing: {}".format(iK, bestConfigInTesting))

			performanceTestingBestOnTraining.append(bestConfigInTesting['bs'])
			indexTestingBestOnTraining.append(bestConfigInTesting['i'])

		iK += 1

	print("avg performance on testing of Default {}: {}".format(np.mean(performanceDefaultOnTesting),
																 performanceDefaultOnTesting))
	print("avg index on testing of best in Default {}: {}".format(np.mean(indexDefaultOnTesting),
																   indexDefaultOnTesting))

	print("avg performance on testing of best in training {}: {}".format(np.mean(performanceTestingBestOnTraining), performanceTestingBestOnTraining))
	print("avg index on testing of best in training {}: {}".format(np.mean(indexTestingBestOnTraining),
																		 indexTestingBestOnTraining))


	return performanceTestingBestOnTraining,  bestOnTestingByFold[defaultId]
	#avgPerformanceByFold = {}
	#for i in bestOnTestingByFold:
	#	avgPerformanceByFold[i] = np.mean(bestOnTestingByFold[i])

	#bestsorted = sorted(avgPerformanceByFold.keys(), key= lambda x: avgPerformanceByFold[x], reverse=True)

	## Retrieve best from training on the testing, that's the value we need to report


	#print("\nperformance of best config on  TRAINING avg {} {}".format( avgPerformanceByFold[bestsorted[0]], bestOnTestingByFold[bestsorted[0]]))

	#if defaultId is not None:
	#	print("\nperformance of default config avg {}, index {} ".format(avgPerformanceByFold[defaultId], np.mean(avgIndexOnTesting[defaultId])))
	#	print("\nperformance of default config {}".format(bestOnTestingByFold[defaultId]))


	#print("Pearson {}: {}".format(np.mean(pearsonTraining),pearsonTraining))
	#print("spearman {}: {}".format(np.mean(spearmanTraining),spearmanTraining))


	#print("\nend {} algoritm {}\n".format(pathResults, algorithm))
	#return bestOnTestingByFold[bestsorted[0]], bestOnTestingByFold[defaultId]


def saveBest(out, data, typeset,k, algo = "",name = "" ):


	filename = "{}/best_{}_{}_{}_{}.csv".format(name, typeset, k, algo)
	fout1 = open(filename, 'w')
	for conf in data:
			fout1.write("{},{},{},{}\n".format(conf['c'], conf['av'], conf['bs'], conf['i']))
	fout1.flush()
	fout1.close()


'''
df is the  dataframe with all data
X: the list of the diffs to consider (because we may not be interested in analyzing all diffs, specially on the k-fold)
allconfig: the key of all configurations 
'''
def analyzeConfigurationsFromDiffs(df, X, allconfig):

	# This array stores, per configuration, a list with all the distance values
	valuesPerConfig = [ [] for i in allconfig]

	# This array stores, per configuration, the number of diffs analyzed
	presentPerConfig = [0 for i in allconfig]

	countRow = 0
	for rowDiff in df.itertuples():

		countRow+=1

		if rowDiff[1]  in X:
			# the first two positions are the ID and diff name. So, we start in the Shift = 2 position
			shift = 2

			for i in range(0, len(allconfig)):
				positionOfConfig = shift + i

				distance = rowDiff[positionOfConfig]
				if not np.isnan(distance):
					valuesPerConfig[i].append(distance)
					presentPerConfig[i]+=1


	return valuesPerConfig, presentPerConfig


def compareDefaultWithBest(rankedBestConfigs):

	configs = list(defaultConfigurations.values())

	rankingDefaultConfig = []

	allDefaults = []

	## collect the information about each defauls
	for i in range(0, len(rankedBestConfigs)):
		currentConfig = rankedBestConfigs[i]
		nameConfig = currentConfig['c']
		if nameConfig in configs:
			rankingDefaultConfig.append((i, currentConfig))
			allDefaults.append(currentConfig)

	## Print each default
	print("\nDefaults configs: ")
	print(rankingDefaultConfig)
	for defaultC in rankingDefaultConfig:
		print(defaultC)

	return allDefaults


def computeCorrelation(configsTesting, configsTraining, field = 'i'):
	xbestTraining = [x[field] for x in configsTraining]
	ybestTest = [x[field] for x in configsTesting]
	print("index training {}".format(xbestTraining))
	print("index testing {}".format(ybestTest))
	rp = scipy.stats.pearsonr(xbestTraining, ybestTest)
	print("Pearson's r {} ".format(rp))
	srho = scipy.stats.spearmanr(xbestTraining, ybestTest)
	print("Spearman's rho {} ".format(srho))
	print("Kendall's tau {} ".format(scipy.stats.kendalltau(xbestTraining, ybestTest)))
	import pingouin as pg


	stats = pg.wilcoxon(xbestTraining, ybestTest, tail='two-sided')
	print("pingouin wilcoxon:\n {}".format(stats))

	stat, p = wilcoxon(xbestTraining, ybestTest)

	print('scipy wilcoxon:\ stat=%.3f, p=%.3f' % (stat, p))

	stat, p = kruskal(xbestTraining, ybestTest)

	print('scipy kruskal:\ stat=%.3f, p=%.3f' % (stat, p))

	print("eff size % f" % pg.compute_effsize(xbestTraining, ybestTest))
	# https://pingouin-stats.org/generated/pingouin.mwu.html#pingouin.mwu
	stats = pg.mwu(xbestTraining, ybestTest, tail='two-sided')
	print("pingouin MWU:\n {}".format(stats))

	stat, p = scipy.stats.mannwhitneyu(xbestTraining, ybestTest)

	print('scipy mannwhitneyu:\ stat=%.3f, p=%.3f' % (stat, p))

	return rp, srho

def findBestRanking(X_train, allConfig, df):
	valuesPerConfig, presentPerConfig = analyzeConfigurationsFromDiffs(df, X_train, allConfig)
	configs, rankedBest = computeBestConfiguration(allConfig, presentPerConfig, valuesPerConfig)
	return configs, rankedBest


def computeBestConfiguration(allConfig, presentPerConfig, valuesPerConfig):

	averages = [0 for x in (allConfig)]
	bestIn = [0 for x in (allConfig)]
	configs= []
	for i in range(0, len(allConfig)):
		averages[i] = np.mean(valuesPerConfig[i])
		zeros = list(filter(lambda x: x == 0, valuesPerConfig[i]))
		if presentPerConfig[i] is not  0:
			bestIn[i] = len(zeros) / presentPerConfig[i]
		else:
			bestIn[i] = 0
		configs.append({'c':allConfig[i], 'av':averages[i], 'bs':bestIn[i]})

	##Sorting according number of best
	bestOrder = sorted(configs, key=lambda x: x['bs'], reverse=True)
	print("Bests ({}) {}".format(len(bestOrder),bestOrder))

	for i in range(0, len(bestOrder)):
		bestOrder[i]["i"] = i

	return configs, bestOrder

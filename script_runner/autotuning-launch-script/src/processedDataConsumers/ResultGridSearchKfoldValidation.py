import numpy as np
import matplotlib.pyplot as plt

from sklearn import datasets
from src.commons.DiffAlgorithmMetadata import *
from sklearn.model_selection import KFold, train_test_split
import pandas
import scipy
import numpy
from scipy.stats import wilcoxon, kruskal
import pingouin as pg
from sklearn.utils import shuffle

def computeGridSearchKFold(pathResults ="../../plots/data/distance_per_diff.csv", kFold = 5, algorithm = None, defaultId = None, random_seed = 0):

	print("----\nRunning {} algoritm {}".format(pathResults, algorithm))
	k_fold = KFold(kFold, random_state=0)

	df = pandas.read_csv(pathResults, sep=",")

	print("DS size before {} ".format(df.size))
	## let's shuffle the results, otherwise they are grouped by megadiff group id
	df = df.sample(frac=1, random_state=random_seed).reset_index(drop=True)
	print("DS size after {} ".format(df.size))

	columns = list(df.columns)

	# We get the name of the configurations
	allConfig = []
	if algorithm is None:
		allConfig = columns[1:]
	else:
		for aConfig in columns[1:]:
			if algorithm in aConfig:
				allConfig.append(aConfig)

	indexOfConfig = {}
	# we start in 1 because the first is the diff
	for i in range(1, len(columns)):
		indexOfConfig[columns[i]] = i

	print("All configs considered with algo {}:{}".format(algorithm, len(allConfig)))
	# we get the first column, which has the diff names
	diffs = df['diff']

	allDiff = list(diffs.values)

	resultsByKTraining = []
	resultsByKTestingSorted = []
	resultsByKTestingByConfig = []

	bestOnTestingByFold = {}
	avgIndexOnTesting = {}

	rp_index = []
	srho_index = []
	pmann_index = []
	pwilcoxon_index = []

	rp_performance = []
	srho_performance= []
	pmann_performance = []
	pwilcoxon_performance = []

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

		configsTraining,rankedConfigsTraining = findBestRanking(X_train, allConfig, df, indexOfConfig)

		print("Configs ({}) {}".format(len(configsTraining),configsTraining))
		print("Ranked ({}) {}".format(len(rankedConfigsTraining), rankedConfigsTraining))


		resultsByKTraining.append(rankedConfigsTraining)

		print("\nTesting {} ".format(k))

		configsTesting,rankedConfigTesting = findBestRanking(X_test, allConfig, df, indexOfConfig)
		resultsByKTestingSorted.append(rankedConfigTesting)
		resultsByKTestingByConfig.append(configsTesting)

		#print("For information, Compute correlation between  training and testing")
		#computeCorrelation(configsTesting, configsTraining)

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
				print("\nCorrelation between testing i:{} j:{} ".format(i,j))
				rp, srho, pmann, pwilcoxon =  computeCorrelation(resultsByKTestingByConfig[i], resultsByKTestingByConfig[j], field = 'i')
				rp_index.append(rp[0])
				srho_index.append(srho[0])
				pmann_index.append(pmann)
				pwilcoxon_index .append(pwilcoxon)

				rp, srho, pmann, pwilcoxon = computeCorrelation(resultsByKTestingByConfig[i], resultsByKTestingByConfig[j],  field = 'bs')
				rp_performance.append(rp[0])
				srho_performance.append(srho[0])
				pmann_performance.append(pmann)
				pwilcoxon_performance.append(pwilcoxon)


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

	print("avg  rp_index: {} {}".format(np.mean(rp_index),rp_index))
	print("avg  srho_index: {} {}".format(np.mean(srho_index), srho_index))
	print("avg  pmann_index: {} {}".format(np.mean(pmann_index), pmann_index))
	print("avg  pwilcoxon_index: {} {}".format(np.mean(pwilcoxon_index), pwilcoxon_index))
	print("avg  rp_performance: {} {}".format(np.mean(rp_performance), rp_performance))
	print("avg  srho_performance: {} {}".format(np.mean(srho_performance), srho_performance))
	print("avg  pmann_performance: {} {}".format(np.mean(pmann_performance), pmann_performance))
	print("avg  pwilcoxon_performance: {} {}".format(np.mean(pwilcoxon_performance), pwilcoxon_performance))

	return performanceTestingBestOnTraining,  bestOnTestingByFold[defaultId] , rp_index,srho_index,pmann_index, pwilcoxon_index, rp_performance,srho_performance,pmann_performance,pwilcoxon_performance
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
def analyzeConfigurationsFromDiffs(df, setofDiffToConsider, allconfig, indexOfColumns):

	# This array stores, per configuration, a list with all the distance values
	valuesPerConfig = [[] for i in allconfig]

	# This array stores, per configuration, the number of diffs analyzed
	presentPerConfig = [0 for i in allconfig]

	countRow = 0
	for rowDiff in df.itertuples():

		countRow+=1
		# in the DataFrame row the first two positions are the tuple id and diff name.
		## example #<class 'tuple'>: (22671, 'nr_98_id_1_010de14013c38b7f82e4755270e88a8249f3a825_SimpleConveyer_GTSPOON.csv', 2.0, 16.0, 80.0, 80.0,192.0, ...
		diff_ID = rowDiff[1]
		if diff_ID in setofDiffToConsider:
			# in the DataFrame row the first two positions are the tuple id and diff name. So, we start in the Shift = 1 position
			shift = 1

			for i in range(0, len(allconfig)):
				currentConfig = allconfig[i]
				indexOfcurrent =  indexOfColumns[currentConfig]
				positionOfConfig = shift + indexOfcurrent
				if i < 10:
					print("{} {} ".format(currentConfig, positionOfConfig))
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
	print("\nField: {}".format(field))
	xbestTraining = [x[field] for x in configsTraining]
	ybestTest = [x[field] for x in configsTesting]
	print("index ({}) left {}".format(len(xbestTraining), ",".join(["%.3f"%x for x in xbestTraining])))
	print("index ({}) right {}".format(len(ybestTest),",".join(["%.3f"%x for x in ybestTest])))
	rp = scipy.stats.pearsonr(xbestTraining, ybestTest)
	print("Pearson's r {} ".format(rp))
	srho = scipy.stats.spearmanr(xbestTraining, ybestTest)
	print("Spearman's rho {} ".format(srho))
	print("Kendall's tau {} ".format(scipy.stats.kendalltau(xbestTraining, ybestTest)))


	stat, pwil = wilcoxon(xbestTraining, ybestTest, alternative='two-sided')

	print('scipy wilcoxon: stat=%.3f, p=%.3f' % (stat, pwil))

#	stats = pg.wilcoxon(xbestTraining, ybestTest, tail='two-sided')
#	print("pingouin wilcoxon:\n {}".format(stats))


	#stat, p = kruskal(xbestTraining, ybestTest)
	#print('scipy kruskal:\ stat=%.3f, p=%.3f' % (stat, p))

	#print("eff size % f" % pg.compute_effsize(xbestTraining, ybestTest))
	# https://pingouin-stats.org/generated/pingouin.mwu.html#pingouin.mwu
#	stats = pg.mwu(xbestTraining, ybestTest, tail='two-sided')
#	print("pingouin MWU:\n {}".format(stats))

	stat, pmann= scipy.stats.mannwhitneyu(xbestTraining, ybestTest, alternative='two-sided')

	print('scipy mannwhitneyu: stat=%.3f, p=%.3f' % (stat, pmann))

	return rp, srho, pmann, pwil

def findBestRanking(X_train, allConfig, df, indexOfColumns):
	valuesPerConfig, presentPerConfig = analyzeConfigurationsFromDiffs(df, X_train, allConfig, indexOfColumns)
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

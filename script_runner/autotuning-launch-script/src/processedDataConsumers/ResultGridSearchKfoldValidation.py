import numpy as np
import matplotlib.pyplot as plt

from sklearn import datasets
from src.commons.DiffAlgorithmMetadata import *
from sklearn.model_selection import KFold, train_test_split
import pandas
import scipy
import numpy


def computeGridSearchKFold(pathResults ="../../plots/data/distance_per_diff.csv", kFold = 5, algorithm = None, defaultId = None):

	print("Running {} algoritm {}".format(pathResults, algorithm))
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
	resultsByKTesting = []

	avgBestOnTesting = {}
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

		configsTraining,rankedBestTraining = findBestRanking(X_train, allConfig, df)

		print("Configs {}".format(configsTraining))
		print("Ranked {}".format(rankedBestTraining))

		resultsByKTraining.append(configsTraining)

		print("\nTesting {} ".format(k))

		configsTesting,rankedBestTesting = findBestRanking(X_test, allConfig, df)
		resultsByKTesting.append(configsTesting)

		print("For information, Compute correlation between  training and testing")
		computeCorrelation(configsTesting, configsTraining)


		for config in rankedBestTesting:
			if config['c'] not in avgBestOnTesting:
				avgBestOnTesting[config['c']] = []
				avgIndexOnTesting[config['c']] = []

			avgBestOnTesting[config['c']].append(config['bs'])
			avgIndexOnTesting[config['c']].append(config['i'])


		###maybe only compare top X
		print("\nCheck with defaults: ")
		compareDefaultWithBest(rankedBestTraining)



	## Once we finish, we compute the correlation between the rankings
	print("\nCheck k-fold rankings: ")
	for i in range(0, len(resultsByKTesting)):
		for j in range(0, len(resultsByKTesting)):
			if i > j :
				print("Correlation between testing i:{} j:{} ".format(i,j))
				rp, srho =  computeCorrelation(resultsByKTesting[i], resultsByKTesting[j])

				pearsonTraining.append(rp[0])
				spearmanTraining.append(srho[0])

	print("\n getting the best:")
	avgSum = {}
	for i in avgBestOnTesting:
		avgSum[i] = np.mean(avgBestOnTesting[i])

		## TODO
		##Comparison distribution best and default
	bestsorted = sorted(avgSum.keys(), key= lambda x: avgSum[x], reverse=True)

	#for b in bestsorted:
	#	print("{} {} {} ".format(b, avgSum[b], np.mean(avgIndexOnTesting[b])))

	print("\nperformance of best config avg {} {}".format( avgSum[bestsorted[0]], avgBestOnTesting[bestsorted[0]]))

	if defaultId is not None:
		print("\nperformance of default config avg {}, index {} ".format(avgSum[defaultId], np.mean(avgIndexOnTesting[defaultId])))
		print("\nperformance of default config {}".format(avgBestOnTesting[defaultId]))


	print("Pearson {}: {}".format(np.mean(pearsonTraining),pearsonTraining))
	print("spearman {}: {}".format(np.mean(spearmanTraining),spearmanTraining))


	print("\nend {} algoritm {}\n".format(pathResults, algorithm))
	return avgBestOnTesting[bestsorted[0]], avgBestOnTesting[defaultId]

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


def computeCorrelation(configsTesting, configsTraining):
	xbestTraining = [x['i'] for x in configsTraining]
	ybestTest = [x['i'] for x in configsTesting]
	print("index training {}".format(xbestTraining))
	print("index testing {}".format(ybestTest))
	rp = scipy.stats.pearsonr(xbestTraining, ybestTest)
	print("Pearson's r {} ".format(rp))
	srho = scipy.stats.spearmanr(xbestTraining, ybestTest)
	print("Spearman's rho {} ".format(srho))
	print("Kendall's tau {} ".format(scipy.stats.kendalltau(xbestTraining, ybestTest)))

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

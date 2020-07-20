import numpy as np
import matplotlib.pyplot as plt

from sklearn import datasets
from src.commons.DiffAlgorithmMetadata import *
from sklearn.model_selection import KFold, train_test_split
import pandas
import scipy


def computeGridSearchKFold(pathResults ="../../plots/data/distance_per_diff.csv", kFold = 5):

	k_fold = KFold(kFold)

	df = pandas.read_csv(pathResults, sep=",")

	columns = list(df.columns)

	# We get the name of the configurations
	allConfig = columns[1:]

	# we get the first column, which has the diff names
	diffs = df['diff']

	allDiff = list(diffs.values)

	resultsByKTraining = []
	resultsByKTesting = []

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
		# we  compute the list of best from the training
		configsTraining,rankedBestTraining = findBestRanking(X_train, allConfig, df)

		print("Configs {}".format(configsTraining))
		print("Ranked {}".format(rankedBestTraining))

		resultsByKTraining.append(configsTraining)

		print("\nTesting {} ".format(k))

		configsTesting,rankedBestTesting = findBestRanking(X_test, allConfig, df)
		resultsByKTesting.append(configsTesting)

		computeCorrelation(configsTesting, configsTraining)

		###maybe only compare top X
		print("\nCheck with defaults: ")
		compareDefaultWithBest(rankedBestTraining)

		print("\nCheck k-fold rankings: ")

		for i in range(0, len(resultsByKTesting)):
			for j in range(0, len(resultsByKTesting)):
				if i > j :
					print("Correlation i:{} j:{} ".format(i,j))
					computeCorrelation(resultsByKTesting[i], resultsByKTesting[j])

		## TODO
		##Comparison distribution best and default

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

	for i in range(0, len(rankedBestConfigs)):
		currentConfig = rankedBestConfigs[i]
		nameConfig = currentConfig['c']
		if nameConfig in configs:
			rankingDefaultConfig.append((i, currentConfig))

	##
	print("\nDefaults configs: ")
	print(rankingDefaultConfig)
	for defaultC in rankingDefaultConfig:
		print(defaultC)


def computeCorrelation(configsTesting, configsTraining):
	xbestTraining = [x['i'] for x in configsTraining]
	ybestTest = [x['i'] for x in configsTesting]
	print("index training {}".format(xbestTraining))
	print("index testing {}".format(ybestTest))
	print("Pearson's r {} ".format(scipy.stats.pearsonr(xbestTraining, ybestTest)))
	print("Spearman's rho {} ".format(scipy.stats.spearmanr(xbestTraining, ybestTest)))
	print("Kendall's tau {} ".format(scipy.stats.kendalltau(xbestTraining, ybestTest)))


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

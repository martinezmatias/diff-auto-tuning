import numpy as np
import matplotlib.pyplot as plt

from sklearn import datasets
from src.commons.DiffAlgorithmMetadata import *
from sklearn.model_selection import KFold, train_test_split
import pandas
import scipy

def analyzeDataset(df, X, allconfig):

	valuesPerConfig = [ [] for i in allconfig]

	presentPerConfig = [0 for i in allconfig]

	for rowDiff in df.itertuples():
		#print(rowDiff)

		if rowDiff[1]  in X:
			print("{} in X".format(rowDiff[1]))
			# the first two positions are the ID and diff name
			shift = 2

			for i in range(0, len(allconfig)):
				positionOfConfig = shift + i

				v = rowDiff[positionOfConfig]
				if not np.isnan(v):
					valuesPerConfig[i].append(v)
					presentPerConfig[i]+=1


	return valuesPerConfig, presentPerConfig


def compareWithBest(rankedBestConfigs):


	configs = list(defaultConfigurations.values())

	rankingDefaultConfig = []

	for i in range(0, len(rankedBestConfigs)):
		currentConfig = rankedBestConfigs[i]
		nameConfig = currentConfig[0]
		if nameConfig in configs:
			rankingDefaultConfig.append((i, currentConfig))

	##
	print("\nDefaults configs: ")
	print(rankingDefaultConfig)
	for defaultC in rankingDefaultConfig:
		print(defaultC)


def computeBestConfigurationKFold(pathResults = "../../plots/data/distance_per_diff.csv", nrFold = 5):

	k_fold = KFold(nrFold)

	df = pandas.read_csv(pathResults, sep=",")

	columns = list(df.columns)

	# We get the name of the configurations
	allConfig = columns[1:]

	# ww get the first column, which has the diff names
	diffs = df['diff']

	allDiff = list(diffs.values)

	resultsByKTraining = []

	# For each Fold
	for k, (train, test) in enumerate(k_fold.split(allDiff)):
		X_train = []
		X_test = []
		print("Running fold {}".format(k))

		# Create the training dataset
		for i in train:
			X_train.append(allDiff[i])

		# Create the testing dataset
		for i in test:
			X_test.append(allDiff[i])

		# we  compute the list of best from the training
		rankedBestTraining = findBestRanking(X_train, allConfig, df)

		resultsByKTraining.append(rankedBestTraining)

		rankedBestTesting = findBestRanking(X_test, allConfig, df)
		resultsByKTraining.append(rankedBestTesting)

		xbestTraining = [ x[2] for x in rankedBestTraining ]
		ybestTest = [x[2] for x in rankedBestTesting]


		print("Pearson's r {} ".format(scipy.stats.pearsonr(xbestTraining, ybestTest)))

		print("Spearman's rho {} ".format(scipy.stats.spearmanr(xbestTraining, ybestTest)))

		print("Kendall's tau {} ".format(scipy.stats.kendalltau(xbestTraining, ybestTest)))

		###maybe only compare top X
		compareWithBest(rankedBestTraining)

def findBestRanking(X_train, allConfig, df):
	valuesPerConfig, presentPerConfig = analyzeDataset(df, X_train, allConfig)
	rankedBest = computeBest(allConfig, presentPerConfig, valuesPerConfig)
	return rankedBest


def computeBest(allConfig, presentPerConfig, valuesPerConfig):
	print(valuesPerConfig)
	print(presentPerConfig)
	averages = [0 for x in (allConfig)]
	bestIn = [0 for x in (allConfig)]
	bestOrder = []
	for i in range(0, len(allConfig)):
		averages[i] = np.mean(valuesPerConfig[i])
		zeros = list(filter(lambda x: x == 0, valuesPerConfig[i]))
		if presentPerConfig[i] is not  0:
			bestIn[i] = len(zeros) / presentPerConfig[i]
		else:
			bestIn[i] = 0
		bestOrder.append(([allConfig[i], averages[i], bestIn[i]]))
	print("av {}".format(averages))
	print("best {}".format(bestIn))
	bestOrder = sorted(bestOrder, key=lambda x: x[1])
	print("zipped {}".format(bestOrder))
	return bestOrder

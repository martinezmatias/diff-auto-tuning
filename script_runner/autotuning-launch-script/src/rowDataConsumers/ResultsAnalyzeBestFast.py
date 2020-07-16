import os
from statistics import mean, stdev
import matplotlib.pyplot as plt
from src.commons.MetaDataReader import *
import numpy as np
import pandas
from src.commons.DiffAlgorithmMetadata import *
import pandas as pd
import scipy.stats
from src.commons.Utils import *

indexesOfColumns = {}
indexOfConfig = {}
orderOfConfiguration = []

'''Compute the fitness of all the data given as parameter'''

def computeBestConfigurationsFast(rootResults, out = "../../plots/data/"):

		files = (os.listdir(rootResults))
		files = list(filter(lambda x: os.path.isdir(os.path.join(rootResults, x)), files))
		totalDiffAnalyzed = 0
		totalConfigAnalyzed = 0

		# map where the key is the distance, value is the nr of ocurrences
		results = {}
		problems = []



		listProportions = []

		matrixOverlapConfigurations = {}

		#
		nrMaxConfig = 3000
		matrixOfDistancesPerDiff = {}

		## Navigate group ids
		for groupId in sorted(files, key=lambda x: int(x)):

			if groupId == ".DS_Store":
				continue

			filesGroup = os.path.join(rootResults, groupId)

			if not os.path.isdir(filesGroup):
				continue

			## let's read the diff from csv
			diffFromGroup = 0

			##Navigates diff
			listdir = os.listdir(filesGroup)
			for diff in listdir:
				if not diff.endswith(".csv") or diff.startswith("metaInfo"):
					continue
				try:

					matrixOfDistancesPerDiff[diff] = [None] * nrMaxConfig

					print("groupid {} file {} /{}  total analyzed: {}".format(groupId,diffFromGroup, len(listdir)/2,totalDiffAnalyzed ))
					csvFile = os.path.join(filesGroup, diff)
					df = pandas.read_csv(csvFile)
					diffFromGroup += 1
					totalDiffAnalyzed += 1
					totalConfigAnalyzedFromDiff = computeFitnessOfFilePair(filesGroup, results,diff, df,
											matrixOfDistancesPerDiff = matrixOfDistancesPerDiff
											 )
					totalConfigAnalyzed+= totalConfigAnalyzedFromDiff
					#Testing
					if diffFromGroup == 10:
						break

				except Exception as e:
					print("Problems with {}".format(diff))
					print(e.with_traceback())
					problems.append(diff)

			 ##test
			break


		saveResultsPerDiffAndConfiguration(matrixOfDistancesPerDiff, outDirectory=out)
		print("Total diff {} total config {}".format(totalDiffAnalyzed, totalConfigAnalyzed))
		print("END")



''' Navigates the CSV of one diff, computes the best configurations and store some metrics '''
def computeFitnessOfFilePair(location, results, diffId, datasetofPair, key ="all", overlap = [],

							 matrixOfDistancesPerDiff = {}
							 ):

	# Get all the nr Actions
	allNrActions = datasetofPair["NRACTIONS"]


	#Take the min value of edit script size
	minES = allNrActions.min(skipna=True)

	# List with the best configurations (that with nr of actions equals to minES)
	allBestConfigurationOfFile = []
	totalRow = 0

	## for the first call to this method, let's store the columns
	columnsToMap(datasetofPair, indexesOfColumns=indexesOfColumns)

	# Navigates each configuration (one per row)
	for rowConfiguration in datasetofPair.itertuples():

		totalRow +=1

		currentNrActions = rowConfiguration.NRACTIONS
		currentTime = rowConfiguration.TIME

		# Skip if the configuration does not produce results (timeout, failure, etc)
		if(np.isnan(currentNrActions) or int(currentNrActions) == 0 ):
			continue

		# compute the fitness of the current configuration
		distance = int(currentNrActions) - minES

		# get a key of the configuration (concatenation of its parameters)
		rowConfigurationKey = getConfigurationKeyFromCSV(rowConfiguration, indexesOfColumns=indexesOfColumns)

		index = None
		# Initialize the structures
		if rowConfigurationKey not in results:
			results[rowConfigurationKey] = {}

			index = len(indexOfConfig.keys())
			indexOfConfig[rowConfigurationKey] = index
			orderOfConfiguration.append(rowConfigurationKey)

		else:
			# the configuration was already seen, so it has an idex
			index = indexOfConfig[rowConfigurationKey]

		## save the distance of the config by diff
		matrixOfDistancesPerDiff[diffId][index] = distance


		# the configuration is the best
		if distance == 0:
			# We save the configuration as best
			allBestConfigurationOfFile.append(rowConfigurationKey)

	return totalRow

def saveResultsPerDiffAndConfiguration(matrixOfDistancesPerDiff, outDirectory ="../../plots/data/"):

	if not os.path.exists(outDirectory):
		os.makedirs(outDirectory)

	csv__file = "{}/distance_per_diff.csv".format(outDirectory)
	fbestFile = open(csv__file, "w")

	fbestFile.write("diff,{}\n".format(",".join(orderOfConfiguration)))

	for diff in matrixOfDistancesPerDiff:
		distances = matrixOfDistancesPerDiff[diff]
		##truncate the list:
		distances = distances[0: len(orderOfConfiguration)]
		filtered = ["" if v is None else str(v) for v in distances]
		data = ",".join(filtered)
		fbestFile.write("{},{}\n".format(diff, data))
		fbestFile.flush()

	fbestFile.close()


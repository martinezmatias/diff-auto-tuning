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
from src.commons.Datalocation import *

'''Compute the fitness of all the data given as parameter'''

def computeEditScriptSize(rootResults, out = RESULTS_PROCESSED_LOCATION, suffix ="", key = None):
		indexesOfPropertiesInTable = {}
		indexOfConfig = {}
		orderOfConfiguration = []

		files = (os.listdir(rootResults))
		files = list(filter(lambda x: os.path.isdir(os.path.join(rootResults, x)), files))
		totalDiffAnalyzed = 0
		totalConfigAnalyzed = 0

		# map where the key is the distance, value is the nr of ocurrences
		results = {}
		problems = []
		#
		nrMaxConfig = 4000
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
					if diffFromGroup % 100 == 0:
						print("groupid {} file {} /{}  total analyzed: {}".format(groupId,diffFromGroup, len(listdir)/2,totalDiffAnalyzed ))
					csvFile = os.path.join(filesGroup, diff)
					df = pandas.read_csv(csvFile)
					diffFromGroup += 1
					totalDiffAnalyzed += 1
					totalConfigAnalyzedFromDiff = computeSizeOfFilePair(filesGroup, results, diff, df,
																		matrixOfDistancesPerDiff = matrixOfDistancesPerDiff,
																		key=key, indexesOfPropertiesInTable=indexesOfPropertiesInTable, indexOfConfig=indexOfConfig, orderOfConfiguration=orderOfConfiguration
																		)
					totalConfigAnalyzed+= totalConfigAnalyzedFromDiff
					#Testing
					#if diffFromGroup == 10:
					#	break

				except Exception as e:
					print("Problems with {}".format(diff))
					print(e.with_traceback())
					problems.append(diff)

				#test
			#break


		saveResultsPerDiffAndConfiguration(matrixOfDistancesPerDiff, outDirectory=out, filesuffix = suffix, orderOfConfiguration=orderOfConfiguration)
		print("Total diff {} total config {}".format(totalDiffAnalyzed, totalConfigAnalyzed))
		print("END")



''' Navigates the CSV of one diff, computes the best configurations and store some metrics '''
def computeSizeOfFilePair(location, results, diffId, dataFrame, key = None,
						  matrixOfDistancesPerDiff = {}, indexesOfPropertiesInTable = {}, indexOfConfig = {}, orderOfConfiguration = []
						  ):

	totalRow = 0

	## for the first call to this method, let's store the columns
	columnsToMap(dataFrame, indexesOfPropertiesInTable=indexesOfPropertiesInTable)

	## we store the configuration to be analyzed together with it NrActions
	configurationsFiltered = {}

	for rowConfiguration in dataFrame.itertuples():
		matcherName = rowConfiguration.MATCHER
		if key is not None and isinstance(matcherName, str) and key not in matcherName:
			continue

		currentNrActions = rowConfiguration.NRACTIONS

		# Skip if the configuration does not produce results (timeout, failure, etc)
		if (np.isnan(currentNrActions) or int(currentNrActions) == 0):
			continue

		# compute the fitness of the current configuration
		edsizerow = int(currentNrActions)

		rowConfigurationKey = getConfigurationKeyFromCSV(rowConfiguration,
														 indexesOfPropertiesOnTable=indexesOfPropertiesInTable)

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
		matrixOfDistancesPerDiff[diffId][index] = edsizerow

	return totalRow

def saveResultsPerDiffAndConfiguration(matrixOfDistancesPerDiff, outDirectory ="../../plots/data/", filesuffix = "", orderOfConfiguration = []):

	if not os.path.exists(outDirectory):
		os.makedirs(outDirectory)

	csv__file = "{}/editscript_size_per_diff_{}.csv".format(outDirectory, filesuffix)
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

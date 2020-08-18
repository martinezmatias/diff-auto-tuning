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
from src.rowDataConsumers.RQ0_Setup_ComputeFitnessDistanceOfConfiguationsFromRowData import *

cacheOfSizes = {}
'''Compute the fitness of all the data given as parameter'''

def searchExampleForPaper(rootResults, out = RESULTS_PROCESSED_LOCATION, suffix ="", key = None, algorithm="ClassicGumtree", thresholdEDsize = 1, thresholdEDsizeDefault = 10):
		indexesOfPropertiesInTable = {}
		indexOfConfig = {}
		orderOfConfiguration = []
		cacheOfSizes.clear()

		f = open("revisionstoanalyze_{}.csv".format(suffix), "w")
		f.write("{},{},{},{},{},{},{},{}\n".format("DIFF_ID", "BEST_CONFIG", "BEST_SIZE", "DEFAULT_CONFIG", "DEFAULT_SIZE",
											 "DISTANCE", "KEY_BEST", "KEY_DEFAULT"))

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

					minEDsize = computeLessOfFilePair(filesGroup, results, diff, df,
																matrixOfDistancesPerDiff = matrixOfDistancesPerDiff,
																key=key, indexesOfPropertiesInTable=indexesOfPropertiesInTable, indexOfConfig=indexOfConfig, orderOfConfiguration=orderOfConfiguration
																)
					bestOfDiff = []
					if minEDsize > thresholdEDsize:
						continue
					#get distance of default config
					defaultConfig = defaultConfigurations[algorithm]
					indexDefault = indexOfConfig[defaultConfig]
					distanceDefault = matrixOfDistancesPerDiff[diff][indexDefault]
					# we only want defaults with not best performance
					if distanceDefault is not None and distanceDefault > 0 and distanceDefault <= thresholdEDsizeDefault:
						foundBest = False
						for index in range(0, len(matrixOfDistancesPerDiff[diff])):

								distance = matrixOfDistancesPerDiff[diff][index]

								if distance == 0:

									for config in indexOfConfig.keys():
										if indexOfConfig[config] == index:
											bestOfDiff.append(config)

											#print("\nconfig {} minED {} distance default {} distance config {} \n path {}".format(config,minEDsize, distanceDefault, distance, csvFile))
											rowstring = "{},{},{},{},{},{},{},{}\n".format(diff, config, minEDsize,
																					 defaultConfig,
																					 cacheOfSizes[diff][defaultConfig],
																					 distanceDefault, createCompleteKey(configId=config, algo=algorithm), createCompleteKey(configId=defaultConfig, algo=algorithm))
											print(rowstring)
											f.write(rowstring)
											f.flush()
											foundBest = True

											break

								if foundBest:
									break
									#return
					#Testing
					#if diffFromGroup == 10:
					#	break

				except Exception as e:
					print("Problems with {}".format(diff))
					print(e.with_traceback())
					problems.append(diff)

				#test
			#break

		f.close()
		print("Total diff {} total config {}".format(totalDiffAnalyzed, totalConfigAnalyzed))
		print("END")



''' Navigates the CSV of one diff, computes the best configurations and store some metrics '''
def computeLessOfFilePair(location, results, diffId, dataFrame, key = None,
							 matrixOfDistancesPerDiff = {}, indexesOfPropertiesInTable = {}, indexOfConfig = {}, orderOfConfiguration = []
							 ):



	# List with the best configurations (that with nr of actions equals to minES)
	allBestConfigurationOfFile = []
	totalRow = 0

	## for the first call to this method, let's store the columns
	columnsToMap(dataFrame, indexesOfPropertiesInTable=indexesOfPropertiesInTable)

	## we store the configuration to be analyzed together with it NrActions
	configurationsFiltered = {}
	minES = 1000000

	cacheOfSizes[diffId] = {}

	# Navigates each configuration (one per row).
	#Filters those condif we target and store the distance
	for rowConfiguration in dataFrame.itertuples():

		matcherName = rowConfiguration.MATCHER
		if key is not None and isinstance(matcherName, str) and key not in matcherName:
			continue

		currentNrActions = rowConfiguration.NRACTIONS

		# Skip if the configuration does not produce results (timeout, failure, etc)
		if(np.isnan(currentNrActions) or int(currentNrActions) == 0 ):
			continue

		# get a key of the configuration (concatenation of its parameters)
		rowConfigurationKey = getConfigurationKeyFromCSV(rowConfiguration, indexesOfPropertiesOnTable=indexesOfPropertiesInTable)
		# We store the number of actions
		configurationsFiltered[rowConfigurationKey] = currentNrActions
		cacheOfSizes[diffId][rowConfigurationKey] = currentNrActions
		# check if that number is the min
		if currentNrActions < minES:
			minES = currentNrActions

		totalRow +=1


	# Now, computes the distance of each configuration (between those filtered!)
	for rowConfigurationKey in configurationsFiltered.keys():

		currentNrActions = configurationsFiltered[rowConfigurationKey]

		# compute the fitness of the current configuration
		distance = int(currentNrActions) - minES

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

	return minES

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


def computeBestConfigurations(rootResults, out = "../../plots/data/"):

		files = (os.listdir(rootResults))
		files = list(filter(lambda x: os.path.isdir(os.path.join(rootResults, x)), files))
		totalDiffAnalyzed = 0

		# map where the key is the distance, value is the nr of ocurrences
		results = {}
		problems = []

		timesPerConfiguration = {}
		sizePerConfiguration = {}
		heightPerConfiguration = {}

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
					fileSummaryInfo = computeFitnessOfFilePair(filesGroup, results,diff, df,
											 timesPerConfiguration = timesPerConfiguration,
											 sizePerConfiguration = sizePerConfiguration,
											 heigthPerConfiguration= heightPerConfiguration,
											 matrixOverlapConfiguration = matrixOverlapConfigurations,
											matrixOfDistancesPerDiff = matrixOfDistancesPerDiff
											 )


					## Now, save info of file
					localProportion = saveInfoOfFiles(diff, fileSummaryInfo)
					## we store the proportion
					listProportions.extend(localProportion)

					#Testing
					if diffFromGroup == 10:
						break

				except Exception as e:
					print("Problems with {}".format(diff))
					print(e.with_traceback())
					problems.append(diff)

			## test
			break


		plotPropertiesOfBestPerFilePair(listProportions)

		saveBestConfigurations(results, timesPerConfig= timesPerConfiguration,
						   sizePerConfig= sizePerConfiguration, heightPerConfig = heightPerConfiguration, outDirectory=out)


		##Move to the main result file
		saveNumberBestNotBest(timesPerConfiguration, name="timesPerConfiguration", outDirectory=out)
		saveNumberBestNotBest(sizePerConfiguration, name="sizePerConfiguration", outDirectory=out)
		saveNumberBestNotBest(heightPerConfiguration, name="heightPerConfiguration", outDirectory=out)

		# Save matrix overlap
		saveMatrixOverlapConfig(matrixOverlapConfigurations,  outDirectory=out)

		## Analyze parameter
		analyzeParameter(timesPerConfiguration, name="timesPerConfiguration",  outDirectory=out)
		analyzeParameter(sizePerConfiguration, name="sizePerConfiguration", outDirectory=out)
		analyzeParameter(heightPerConfiguration, name="heightPerConfiguration", outDirectory=out)

		saveResultsPerDiffAndConfiguration(matrixOfDistancesPerDiff, outDirectory=out)

'''returns a list with all the parameters with the algorithm name as prefix'''
def getAllHyperparametersHeader():
	header = []
	for algo in propertiesPerMatcher.keys():
		for hyperparam in propertiesPerMatcher[algo]:
			header.append(algo+"_"+hyperparam)

	return header

'''given configuration get a row with values for each hyperparameter existing: 
those that correspond to the configuration algorithm will have the corresponding value (specified in the config), otherwise 
we put an empty string'''
def getValueOfHypeParameter(config, header):
	configs = config.split("_")
	algo = configs[0]
	valuesOfCofiguration = {}
	paramsOfAlgo = propertiesPerMatcher[algo]
	for param in range(0, len(paramsOfAlgo)):
		paramName = paramsOfAlgo[param]
		valuesOfCofiguration[algo+"_"+paramName] = configs[param + 1]

	row = []
	for h in header:
		if h in valuesOfCofiguration:
			row.append(valuesOfCofiguration[h])
		else:
			row.append("")
	return row


'''save the metrics of a given measure into a file'''
def saveNumberBestNotBest(measurePerConfiguration, outDirectory ="./plots/data/", name ="times"):
	if not os.path.exists(outDirectory):
		os.makedirs(outDirectory)

	fbestFile = open("{}/single_configs_{}.csv".format(outDirectory, name), "w")
	fbestFile.write("config,best_time_avg,best_time_median,notbest_time_avg,notbest_time_median,Mann-Whitney_U_stat,Mann-Whitney_U_p\n")

	for config in measurePerConfiguration.keys():
		rc, best, notbest = getRowNumberBestNotBest(measurePerConfiguration, config)
		content = "{},{}\n".format(config,rc )
		fbestFile.write((content))
	fbestFile.close()

'''Store for each parameter and value the total number of diff with those values  '''
def analyzeParameter(metricPerConfiguration, outDirectory ="./plots/data/", name ="times"):

	directorypar = "{}/parameters/".format(outDirectory)
	if not os.path.exists(directorypar):
		os.makedirs(directorypar)

	fbestFile = open("{}/single_parameter_{}.csv".format(outDirectory, name), "w")
	fbestFile.write("config,"
					+ "algo,property,value,"
					+"percentage_best,"
					+"best_{}_len,best_{}_mean,best_{}_median,best_{}_stdev,".format(name, name, name,name)
					+"notbest_{}_len,notbest_{}_mean,notbest_{}_median,notbest_{}_stdev,{}_Mann-Whitney_U_stat,{}_Mann-Whitney_U_p\n".format(name, name, name,name,name,name))

	dataBest = {}
	dataNotBest = {}
	joinkey = "-"
	## let's summarize the number of diff according to the hyperparameter and value
	# navigate each configuration
	for config in metricPerConfiguration.keys():

			params = (config).split("_")
			# get the Diff algorithm name
			algorithName = params[0]
			# get all properties of that name
			algoProperties = propertiesPerMatcher[algorithName]
			# iterates on each  property
			for i in range(1, len(algoProperties) + 1):
				# create a key using the value of that property
				keySingleParameter = algorithName+joinkey+ algoProperties[i-1] +joinkey+params[i]
				if keySingleParameter not in dataBest:
					dataBest[keySingleParameter] = []
					dataNotBest[keySingleParameter] = []

				dataBest[keySingleParameter].extend(plainDict(metricPerConfiguration[config]["best"]))
				dataNotBest[keySingleParameter].extend(plainDict(metricPerConfiguration[config]["notbest"]))
	## for each pair hyperparameter-value
	for keySingleParameter in dataBest:
		data = []
		all = []
		all.extend(dataBest[keySingleParameter])
		all.extend(dataNotBest[keySingleParameter])
		data.append(all)
		legends = ["all"]
		if len(dataBest[keySingleParameter])>0:
			data.append(dataBest[keySingleParameter])
			legends.append("")#workarround
			legends.append("best")
		if len(dataNotBest[keySingleParameter]) > 0:
			data.append(dataNotBest[keySingleParameter])
			legends.append("")#workarround
			legends.append("notbest")

		#put in columns
		keySingleSplited = keySingleParameter.split(joinkey)

		##https://machinelearningmastery.com/nonparametric-statistical-significance-tests-in-python/
		stat, p = scipy.stats.mannwhitneyu(dataBest[keySingleParameter], dataNotBest[keySingleParameter])

		fbestFile.write("{},"
						"{},{},{}," ## algo, property,value
						"{}," # percentage
						"{}," #best
						"{},"#not best
						"{},{:.5f}"#mannwhitneyu
						"\n".format(keySingleParameter, keySingleSplited[0], keySingleSplited[1], keySingleSplited[2],
											   len(dataBest[keySingleParameter]) / (len(dataBest[keySingleParameter]) + len(dataNotBest[keySingleParameter])),
											   getStatsList(dataBest[keySingleParameter]),
											   getStatsList(dataNotBest[keySingleParameter]),
											   stat, p
									  )
						)

		if len(all) > 0:
			plotDistribution(data, "", "", "{}-{}".format(name,keySingleParameter), legends, "{}/plot_{}_param_{}.pdf".format(directorypar, name, keySingleParameter))

	fbestFile.close()

'''return stats of a given list'''
def getStatsList(aList):
	if(len(aList)):
		return "{},{},{},{}".format(len(aList), mean(aList), np.median(aList), stdev(aList))
	else:
		return "{},,,".format(len(aList))

def plotDistribution(data, xlabel, ylabel, key, legends, filename):
	fig, ax = plt.subplots()
	# ax.boxplot(datakeyTimeSingleConfigurationByGroup, showfliers=False)
	# ax.set_xticklabels(keysGroups)
	ax.violinplot(data, showmedians=True, showmeans=True)
	legend = [""]
	legend.extend(legends)
	ax.set_xticklabels(legend)
	plt.title(key)
	plt.ylabel(ylabel)
	plt.xlabel(xlabel)
	# plt.show()
	plt.savefig(filename)
	plt.close()

'''given a configuration returns some metrics (e.g.avg) about a measure (e.g., execution time)'''
def getRowNumberBestNotBest(timesPerConfiguration, config):
	best_ = plainDict(timesPerConfiguration[config]["best"])
	meantbest = "" if len(best_) == 0 else mean(best_)
	medianbest = "" if len(best_) == 0 else np.median(best_)
	notbest_ = plainDict(timesPerConfiguration[config]["notbest"])
	meantnotbest = "" if len(notbest_) == 0 else mean(notbest_)
	mediannotbest = "" if len(notbest_) == 0 else np.median(notbest_)
	stat, p = scipy.stats.mannwhitneyu(best_, notbest_)
	content = "{},{},{},{},{},{}".format( meantbest, medianbest, meantnotbest, mediannotbest,stat, p)

	return content, len(best_), len(notbest_)


def saveMatrixOverlapConfig(matrixOverlapConfigurations, outDirectory ="./plots/data"):

	if not os.path.exists(outDirectory):
		os.makedirs(outDirectory)

	sortedConfig = sorted(matrixOverlapConfigurations.keys())

	fbestFile = open("{}/matrix_overlap_configs.csv".format(outDirectory), "w")
	fbestFile.write(",".join(sortedConfig))
	fbestFile.write("\n")
	for config in sortedConfig:
		row = []
		for anotherConfig in sortedConfig:
			if anotherConfig not in matrixOverlapConfigurations[config]:
				row.append("")
			else:
				row.append(str(matrixOverlapConfigurations[config][anotherConfig]))

		fbestFile.write(",".join(row))
		fbestFile.write("\n")
		fbestFile.flush()

	fbestFile.close()

''' Navigates the CSV of one diff, computes the best configurations and store some metrics '''
def computeFitnessOfFilePair(location, results, diffId, datasetofPair, key ="all", overlap = [],
							 overlapPerAlgo = {}, debugBestbyConfiguration = {}, debug = True,
							 timesPerConfiguration = {},
							 sizePerConfiguration = {},
							 heigthPerConfiguration = {},
							 matrixOverlapConfiguration = {},
							 matrixOfDistancesPerDiff = {}
							 ):

	# Stores metrics of the file under comparison
	infoByFile = {}
	# Get all the nr Actions
	allNrActions = datasetofPair["NRACTIONS"]
	pd_series = pd.Series(allNrActions)
	counts = pd_series.value_counts()
	entropy = scipy.stats.entropy(counts)

	#Take the min value of edit script size
	minES = allNrActions.min(skipna=True)

	# List with the best configurations (that with nr of actions equals to minES)
	allBestConfigurationOfFile = []
	totalRow = 0

	# Retrieve metrics of the AST under comparison
	size, height = getTreeMetricsFromFile(location, diffId)

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
			debugBestbyConfiguration[rowConfigurationKey] = []

			## time and sizes
			timesPerConfiguration[rowConfigurationKey]  = {}
			timesPerConfiguration[rowConfigurationKey]["best"] = {}
			timesPerConfiguration[rowConfigurationKey]["notbest"] = {}

			sizePerConfiguration[rowConfigurationKey] = {}
			sizePerConfiguration[rowConfigurationKey]["best"] = {}
			sizePerConfiguration[rowConfigurationKey]["notbest"] = {}

			heigthPerConfiguration[rowConfigurationKey] = {}
			heigthPerConfiguration[rowConfigurationKey]["best"] = {}
			heigthPerConfiguration[rowConfigurationKey]["notbest"] = {}

			overlapPerAlgo[rowConfigurationKey] = {}
			for algorithmName in propertiesPerMatcher.keys():
				overlapPerAlgo[rowConfigurationKey][algorithmName] = {}

			index = len(indexOfConfig.keys())
			indexOfConfig[rowConfigurationKey] = index
			orderOfConfiguration.append(rowConfigurationKey)

		else:
			# the configuration was already seen, so it has an idex
			index = indexOfConfig[rowConfigurationKey]

		## save the distance of the config by diff
		matrixOfDistancesPerDiff[diffId][index] = distance

		## Save the distance of the configuration
		incrementOne(results[rowConfigurationKey], distance)

		# the configuration is the best
		if distance == 0:
			# We save the configuration as best
			allBestConfigurationOfFile.append(rowConfigurationKey)

			# we store the data (time, size, height) of that best configuration
			incrementOne(timesPerConfiguration[rowConfigurationKey]["best"], currentTime)
			if size is not None :
				incrementOne(sizePerConfiguration[rowConfigurationKey]["best"], size)
				incrementOne(heigthPerConfiguration[rowConfigurationKey]["best"], height)
			if debug:
				debugBestbyConfiguration[rowConfigurationKey].append(diffId)

		else:
			# The configuration is not the best, we store the data  (time, size, height)
			incrementOne(timesPerConfiguration[rowConfigurationKey]["notbest"], currentTime)
			if size is not None:
				incrementOne(sizePerConfiguration[rowConfigurationKey]["notbest"], size)
				incrementOne(heigthPerConfiguration[rowConfigurationKey]["notbest"], height)

	## Stats per file
	proportionBest = len(allBestConfigurationOfFile) / totalRow

	## We store the information for the diff
	infoByFile[diffId] = {}
	infoByFile[diffId]["nrBest"] = len(allBestConfigurationOfFile)
	infoByFile[diffId]["proportionBest"] = proportionBest
	infoByFile[diffId]["entropyNrActions"] = entropy
	infoByFile[diffId]["allBest"] = allBestConfigurationOfFile


	## Computes which are the algoritms for the best
	for oneBestConfiguration in allBestConfigurationOfFile:
		if oneBestConfiguration not in matrixOverlapConfiguration:
			matrixOverlapConfiguration[oneBestConfiguration] = {}


	# Store the proportion w.r.t other algorithms
	# we create a matrix (matrixOverlapConfiguration) that compares each configuration
	for oneBestConfiguration in allBestConfigurationOfFile:

			# computes when two configurations are both the BEST for a file.
			for anotherBestConfig in allBestConfigurationOfFile:
				if anotherBestConfig is not oneBestConfiguration:
					if anotherBestConfig not in	matrixOverlapConfiguration[oneBestConfiguration]:
						matrixOverlapConfiguration[oneBestConfiguration][anotherBestConfig]=1
					else:
						matrixOverlapConfiguration[oneBestConfiguration][anotherBestConfig]+= 1


	return infoByFile



def saveBestConfigurations(results, limitTop = 300000, outDirectory ="./plots/data/", timesPerConfig = {}, sizePerConfig = {}, heightPerConfig = {}):
	if not os.path.exists(outDirectory):
		os.makedirs(outDirectory)

	dir_over_gen = "{}/overlap_general/".format(outDirectory)
	if not os.path.exists(dir_over_gen):
		os.makedirs(dir_over_gen)

	dir_over_crossed = "{}/overlap_crossed/".format(outDirectory)
	if not os.path.exists(dir_over_crossed):
		os.makedirs(dir_over_crossed)
	# as results is a map where keys are distance, we sort according with the nr of occurrence of distance zero
	keySorted = sorted(results.keys(), key=lambda x: (results[x][0] if 0 in results[x] else 0), reverse=True)
	print("Finishing processing")
	iConfiguration = 0
	fbestConfig = open("{}/best_configurations_summary.csv".format(outDirectory), "w")

	header = getAllHyperparametersHeader()

	fbestConfig.write("top,configuration,nrBest,total,"
					  "best_time_avg,best_time_median,notbest_time_avg,notbest_time_median,time_Mann-Whitney_U_stat,time_Mann-Whitney_U_p,"
					   "best_size_avg,best_size_median,notbest_size_avg,notbest_size_median,size_Mann-Whitney_U_stat,size_Mann-Whitney_U_p,"
					   "best_height_avg,best_height_median,notbest_height_avg,notbest_height_median,height_Mann-Whitney_U_stat,height_Mann-Whitney_U_p"
					    ",{}"
					    "\n".format(",".join(header)))

	for configuration in keySorted:
		nrBest = (results[configuration][0] if 0 in results[configuration] else 0)
		if (nrBest > 0):
			rowTimes, allbest, allnotbest = getRowNumberBestNotBest(timesPerConfig, configuration)
			rowSizes, a1, b1 = getRowNumberBestNotBest(sizePerConfig, configuration)
			rowHeight, a1, b1 = getRowNumberBestNotBest(heightPerConfig, configuration)

			fbestConfig.write("{},{},{},{},"
							  "{},{},{}"
							  ",{}\n".format(iConfiguration, configuration, nrBest, allbest + allnotbest,
												  rowTimes, rowSizes,rowHeight
											 		, ",".join(getValueOfHypeParameter(configuration,header))
											 ))

		iConfiguration += 1
		if (iConfiguration == limitTop):
			break

	fbestConfig.flush()
	fbestConfig.close()


def plotPropertiesOfBestPerFilePair(listProportions, directory = "./plots/data"):

	if not os.path.exists(directory):
		os.makedirs(directory)

	fig, ax = plt.subplots()
	# ax.boxplot(listProportions, showfliers=False)
	ax.violinplot(listProportions, showmedians=True, showmeans=True)
	# legend = [""]
	# legend.extend(legends)
	# ax.set_xticklabels(legend)
	plt.title("Distribution proportion best configuration")
	plt.ylabel("Proportion")
	# plt.xlabel("Distribution proportion best configuration")
	# plt.show()
	plt.savefig("{}/distribution_bestProportion.pdf".format(directory))
	plt.close()


def saveInfoOfFiles(filename, debugInfoByFile, directory = "./plots/data/"):

	if not os.path.exists(directory):
		os.makedirs(directory)

	dir_best = "{}/overlap_best_configs_file/".format(directory)
	if not os.path.exists(dir_best):
		os.makedirs(dir_best)

	dir_best_files = "{}/overlap_best_per_file/".format(directory)
	if not os.path.exists(dir_best_files):
		os.makedirs(dir_best_files)

	listProportions = []
	fbestFile = open("{}/best_file_summary_{}.csv".format(dir_best,filename), "w")
	fbestFile.write("file,nrBest,proportionBest,entropyNrActions\n")

	for filename in debugInfoByFile.keys():

		if debugInfoByFile[filename]["proportionBest"] > 0:
			listProportions.append(debugInfoByFile[filename]["proportionBest"])
			fbestFile.write("{},{},{},{}\n".format(filename,
												   debugInfoByFile[filename]["nrBest"],
												   debugInfoByFile[filename]["proportionBest"],
												   debugInfoByFile[filename]["entropyNrActions"]))

			detailBestFile = open("{}/best_{}".format(dir_best_files, filename), "w")
			for best in debugInfoByFile[filename]["allBest"]:
				detailBestFile.write("{}\n".format(best))

			detailBestFile.flush()
			detailBestFile.close()
	fbestFile.flush()
	fbestFile.close()
	return listProportions



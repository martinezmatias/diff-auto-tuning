import os
from distutils.command.config import config
from statistics import mean, stdev
import matplotlib.pyplot as plt
from sklearn.metrics import cohen_kappa_score
import numpy as np
import pandas
from DiffAlgorithmMetadata import *
import pandas as pd
import scipy.stats

'''Compute the fitness of all the data given as parameter'''
def computeFitnesss(rootResults):

		files = (os.listdir(rootResults))
		files = list(filter(lambda x: os.path.isdir(os.path.join(rootResults, x)), files))
		totalDiffAnalyzed = 0

		results = {}
		overlap = {}
		overlapPerAlgo = {}

		problems = []

		timesPerConfiguration = {}
		sizePerConfiguration = {}
		heightPerConfiguration = {}

		listProportions = []

		matrixOverlapConfigurations = {}

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
					print("groupid {} file {} /{}  total analyzed: {}".format(groupId,diffFromGroup, len(listdir)/2,totalDiffAnalyzed ))
					csvFile = os.path.join(filesGroup, diff)
					df = pandas.read_csv(csvFile)
					diffFromGroup += 1
					fileSummaryInfo = computeFitnessOfFilePair(filesGroup, results,diff, df, overlap=overlap,
											 overlapPerAlgo=  overlapPerAlgo,
											 timesPerConfiguration = timesPerConfiguration,
											 sizePerConfiguration = sizePerConfiguration,
											 heigthPerConfiguration= heightPerConfiguration,
											 matrixOverlapConfiguration = matrixOverlapConfigurations
											 )
					totalDiffAnalyzed += 1

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

		if False:
			plotPropertiesOfBestPerFilePair(listProportions)

			saveBestConfigurations(results, overlap= overlap, overlapPerAlgo = overlapPerAlgo, timesPerConfig= timesPerConfiguration,
							   sizePerConfig= sizePerConfiguration, heightPerConfig = heightPerConfiguration)


			##Move to the main result file
			saveNumberBestNotBest(timesPerConfiguration, name="timesPerConfiguration")
			saveNumberBestNotBest(sizePerConfiguration, name="sizePerConfiguration")
			saveNumberBestNotBest(heightPerConfiguration, name="heightPerConfiguration")

			# Save matrix overlap
			saveMatrixOverlapConfig(matrixOverlapConfigurations)


			## Analyze parameter
			analyzeParameter(timesPerConfiguration, name="timesPerConfiguration")
			analyzeParameter(sizePerConfiguration, name="sizePerConfiguration")
			analyzeParameter(heightPerConfiguration, name="heightPerConfiguration")


def saveNumberBestNotBest(metricPerConfiguration, directory ="./plots/data/", name ="times"):
	if not os.path.exists(directory):
		os.makedirs(directory)

	fbestFile = open("{}/single_configs_{}.csv".format(directory, name), "w")
	fbestFile.write("config,best_time_avg,best_time_median,notbest_time_avg,notbest_time_median,Mann-Whitney_U_stat,Mann-Whitney_U_p\n")

	for config in metricPerConfiguration.keys():
		content = "{},{}\n".format(config, getRowNumberBestNotBest(metricPerConfiguration, config))
		fbestFile.write((content))
	fbestFile.close()


def analyzeParameter(metricPerConfiguration, directory ="./plots/data/", name ="times"):

	directorypar = "{}/parameters/".format(directory)
	if not os.path.exists(directorypar):
		os.makedirs(directorypar)

	fbestFile = open("{}/single_parameter_{}.csv".format(directory, name), "w")
	fbestFile.write("config,"
					+ "algo,property,value,"
					+"percentage_best,"
					+"best_{}_len,best_{}_mean,best_{}_median,best_{}_stdev,".format(name, name, name,name)
					+"notbest_{}_len,notbest_{}_mean,notbest_{}_median,notbest_{}_stdev,{}_Mann-Whitney_U_stat,{}_Mann-Whitney_U_p\n".format(name, name, name,name,name,name))

	dataBest = {}
	dataNotBest = {}
	joinkey = "-"
	# navigate each configuration
	for config in metricPerConfiguration.keys():

			params = (config).split("_")
			# get the Diff algorithm name
			algoname = params[0]
			# get all properties of that name
			algoProperties = propertiesPerMatcher[algoname]
			# iterates on each  property
			for i in range(1, len(algoProperties) + 1):
				# create a key using the value of that property
				key = algoname+joinkey+ algoProperties[i-1] +joinkey+params[i]
				if key not in dataBest:
					dataBest[key] = []
					dataNotBest[key] = []

				dataBest[key].extend(plainDict(metricPerConfiguration[config]["best"]))
				dataNotBest[key].extend(plainDict(metricPerConfiguration[config]["notbest"]))

	for key in dataBest:
		data = []
		all = []
		all.extend(dataBest[key])
		all.extend(dataNotBest[key])
		data.append(all)
		legends = ["all"]
		if len(dataBest[key])>0:
			data.append(dataBest[key])
			legends.append("")#workarround
			legends.append("best")
		if len(dataNotBest[key]) > 0:
			data.append(dataNotBest[key])
			legends.append("")#workarround
			legends.append("notbest")

		#put in columns
		keySplited = key.split(joinkey)

		##https://machinelearningmastery.com/nonparametric-statistical-significance-tests-in-python/
		stat, p = scipy.stats.mannwhitneyu(dataBest[key], dataNotBest[key])

		fbestFile.write("{},"
						"{},{},{}," ## algo, property,value
						"{}," # percentage
						"{}," #best
						"{},"#not best
						"{},{:.5f}"#mannwhitneyu
						"\n".format(key, keySplited[0], keySplited[1], keySplited[2],
											   len(dataBest[key]) / (len(dataBest[key]) + len(dataNotBest[key])),
											   getStatsList(dataBest[key]),
											   getStatsList(dataNotBest[key]),
											   stat, p
									  )
						)

		if len(all) > 0:
			plotDistribution(data, "", "", "{}-{}".format(name,key), legends, "{}/plot_{}_param_{}.pdf".format(directorypar, name, key))

	fbestFile.close()
def getStatsList(list):
	if(len(list)):
		return "{},{},{},{}".format(len(list) , mean(list),np.median(list), stdev(list))
	else:
		return "{},,,".format(len(list))

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

def getRowNumberBestNotBest(timesPerConfiguration, config):
	best_ = plainDict(timesPerConfiguration[config]["best"])
	meantbest = "" if len(best_) == 0 else mean(best_)
	medianbest = "" if len(best_) == 0 else np.median(best_)
	notbest_ = plainDict(timesPerConfiguration[config]["notbest"])
	meantnotbest = "" if len(notbest_) == 0 else mean(notbest_)
	mediannotbest = "" if len(notbest_) == 0 else np.median(notbest_)
	stat, p = scipy.stats.mannwhitneyu(best_, notbest_)
	content = "{},{},{},{},{},{}".format( meantbest, medianbest, meantnotbest, mediannotbest,stat, p)

	##

	return content


def saveMatrixOverlapConfig(matrixOverlapConfigurations, directory = "./plots/data"):

	if not os.path.exists(directory):
		os.makedirs(directory)

	sortedConfig = sorted(matrixOverlapConfigurations.keys())

	fbestFile = open("{}/matrix_overlap_configs.csv".format(directory), "w")
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
def computeFitnessOfFilePair(location, results, filename,datasetofPair, key = "all", overlap = [],
							 overlapPerAlgo = {}, debugBestbyConfiguration = {}, debug = True,
							timesPerConfiguration = {},
							sizePerConfiguration = {},
							heigthPerConfiguration = {},
							 matrixOverlapConfiguration = {}
							 ):

	# Stores metrics of the file under comparison
	debugInfoByFile = {}

	nractions = datasetofPair["NRACTIONS"]
	pd_series = pd.Series(nractions)
	counts = pd_series.value_counts()
	entropy = scipy.stats.entropy(counts)

	#Take the min value of edit script size
	minES = nractions.min(skipna=True)

	# List with the best configurations (that with nr of actions equals to minES)
	allBestConfigurationOfFile = []
	totalRow = 0

	# Retrieve metrics of the AST under comparison
	size, height = getTreeSize(location, filename)


	#if True:
	#	return {}

	# Navigates each configuration (one per row)
	for rowConfiguration in datasetofPair.iterrows():

		totalRow +=1

		#if True:
		#	continue

		currentNrActions = rowConfiguration[1]['NRACTIONS']
		currentTime = rowConfiguration[1]['TIME']

		# Skip if the configuration does not produce results (timeout, failure, etc)
		if(np.isnan(currentNrActions) or int(currentNrActions) == 0 ):
			continue

		# compute the fitness of the current configuration
		distance = int(currentNrActions) - minES

		# get a key of the configuration (concatenation of its parameters)
		rowConfigurationKey = getConfigurationkey(rowConfiguration[1])

		if rowConfigurationKey not in results:
			results[rowConfigurationKey] = {}
			overlap[rowConfigurationKey] = {}
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
			for algo in propertiesPerMatcher.keys():
				overlapPerAlgo[rowConfigurationKey][algo] = {}

		incrementOne(results[rowConfigurationKey], distance)

		if distance == 0:
			# We save the configuration as best
			allBestConfigurationOfFile.append(rowConfigurationKey)

			# we store the data (time, size, height) of that best configuration
			incrementOne(timesPerConfiguration[rowConfigurationKey]["best"], currentTime)
			if size is not None :
				incrementOne(sizePerConfiguration[rowConfigurationKey]["best"], size)
				incrementOne(heigthPerConfiguration[rowConfigurationKey]["best"], height)
			if debug:
				debugBestbyConfiguration[rowConfigurationKey].append(filename)

		else:
			# The configuration is not the best, we store the data  (time, size, height)
			incrementOne(timesPerConfiguration[rowConfigurationKey]["notbest"], currentTime)
			if size is not None:
				incrementOne(sizePerConfiguration[rowConfigurationKey]["notbest"], size)
				incrementOne(heigthPerConfiguration[rowConfigurationKey]["notbest"], height)

	## Stats per file
	proportionBest = len(allBestConfigurationOfFile) / totalRow
	if True:
		debugInfoByFile[filename] = {}
		debugInfoByFile[filename]["nrBest"] = len(allBestConfigurationOfFile)
		debugInfoByFile[filename]["proportionBest"] = proportionBest
		debugInfoByFile[filename]["entropyNrActions"] = entropy
		debugInfoByFile[filename]["allBest"] = allBestConfigurationOfFile


	##Initialization of structure that counts the best per matcher
	countOverlapAlgo = {}
	for algo in propertiesPerMatcher.keys():
		countOverlapAlgo[algo] = 0

	## Computes which are the algoritms for the best
	for oneBestConfiguration in allBestConfigurationOfFile:
		# Store the proportion
		incrementOne(overlap[oneBestConfiguration], proportionBest)

		algorithm_name = oneBestConfiguration.split("_")[0]
		countOverlapAlgo[algorithm_name] += 1

		if oneBestConfiguration not in matrixOverlapConfiguration:
			matrixOverlapConfiguration[oneBestConfiguration] = {}


	# Store the proportion w.r.t other algorithms
	# we create a matrix (matrixOverlapConfiguration) that compares each configuration
	for oneBestConfiguration in allBestConfigurationOfFile:
		if False:
			algorithm_name = oneBestConfiguration.split("_")[0]
			for anotherAlgo in propertiesPerMatcher.keys():
				if anotherAlgo is not algorithm_name:
					percentageOverlap = countOverlapAlgo[anotherAlgo]
					incrementOne(overlapPerAlgo[oneBestConfiguration][anotherAlgo], percentageOverlap)

			# computes when two configurations are both the BEST for a file.
			for anotherBestConfig in allBestConfigurationOfFile:
				if anotherBestConfig is not oneBestConfiguration:
					if anotherBestConfig not in	matrixOverlapConfiguration[oneBestConfiguration]:
						matrixOverlapConfiguration[oneBestConfiguration][anotherBestConfig]=1
					else:
						matrixOverlapConfiguration[oneBestConfiguration][anotherBestConfig]+= 1


	return debugInfoByFile


def getTreeSize(location, filename):
	import csv
	with open('{}/metaInfo_{}'.format(location, filename), mode='r') as csv_file:
		csv_reader = csv.DictReader(csv_file)
		line_count = 0
		for row in csv_reader:
			size = row["L_SIZE"]
			height = row["L_HEIGHT"]
			return int(size), int(height)


	return None, None

def saveBestConfigurations(results, overlap, overlapPerAlgo, limitTop = 300000, debug = False, directory ="./plots/data/", timesPerConfig = {}, sizePerConfig = {}, heightPerConfig = {}):
	if not os.path.exists(directory):
		os.makedirs(directory)

	dir_over_gen = "{}/overlap_general/".format(directory)
	if not os.path.exists(dir_over_gen):
		os.makedirs(dir_over_gen)

	dir_over_crossed = "{}/overlap_crossed/".format(directory)
	if not os.path.exists(dir_over_crossed):
		os.makedirs(dir_over_crossed)

	keySorted = sorted(results.keys(), key=lambda x: (results[x][0] if 0 in results[x] else 0), reverse=True)
	print("Finishing processing")
	top = 0
	fbestConfig = open("{}/best_configurations_summary.csv".format(directory), "w")
	fbestConfig.write("top,configuration,nrBest,"
					  "best_time_avg,best_time_median,notbest_time_avg,notbest_time_median,time_Mann-Whitney_U_stat,time_Mann-Whitney_U_p,"
					   "best_size_avg,best_size_median,notbest_size_avg,notbest_size_median,size_Mann-Whitney_U_stat,size_Mann-Whitney_U_p,"
					   "best_height_avg,best_height_median,notbest_height_avg,notbest_height_median,height_Mann-Whitney_U_stat,height_Mann-Whitney_U_p"
					  "\n")

	for configuration in keySorted:
		nrBest = (results[configuration][0] if 0 in results[configuration] else 0)
		if (nrBest > 0):
			lbest = sorted(plainDict(overlap[configuration]))
			rowTimes = getRowNumberBestNotBest(timesPerConfig, configuration)
			rowSizes = getRowNumberBestNotBest(sizePerConfig, configuration)
			rowHeight = getRowNumberBestNotBest(heightPerConfig, configuration)

			fbestConfig.write("{},{},{},{},{},{}\n".format(top, configuration, nrBest, rowTimes, rowSizes,rowHeight))

			## We store the overlap
			fOverlapConfig = open("{}/overlap_general_config_{}.csv".format(dir_over_gen, configuration), "w")
			for b in lbest:
				fOverlapConfig.write("{}\n".format(b))
			fOverlapConfig.close()

			## now overlap by algo
			for algo in propertiesPerMatcher.keys():
				fOverlapAlgoConfig = open("{}/overlap_config_{}_with_{}.csv".format(dir_over_crossed,configuration, algo), "w")
				lbestalgo = sorted(plainDict(overlapPerAlgo[configuration][algo]))
				for b in lbestalgo:
					fOverlapAlgoConfig.write("{}\n".format(b))
				fOverlapAlgoConfig.close()


		top += 1
		if (top == limitTop):
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


def plainDict(dic = {}):
	r = []
	for k in dic.keys():
		nr = dic[k]
		for i in range(1, nr + 1):
			r.append(k)

	return r

def incrementOne(dict, key, value = 1):
	if key not in dict:
		dict[key] = value
	else:
		dict[key] += value

'''Returns a key for the configuration'''
def getConfigurationkey(row):
		matcherName = row['MATCHER']
		key = matcherName;
		for property in propertiesPerMatcher[matcherName]:
			key+="_"+"{:.1f}".format((row[property])).rstrip('0').rstrip('.')

		return key

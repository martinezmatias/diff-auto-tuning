import os
from statistics import mean, stdev
import matplotlib.pyplot as plt
from sklearn.metrics import cohen_kappa_score
import numpy as np
import pandas
from DiffAlgorithmMetadata import *

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
											 matrixOverlapConfiguration = matrixOverlapConfigurations
											 )
					totalDiffAnalyzed += 1

					## Now, save info of file
					localProportion = saveInfoOfFiles(diff, fileSummaryInfo)
					## we store the proportion
					listProportions.extend(localProportion)


				except Exception as e:
					print("Problems with {}".format(diff))
					print(e.with_traceback())
					problems.append(diff)

			## test
			break

		plotPropertiesOfBestPerFilePair(listProportions)

		printBest(results, overlap= overlap, overlapPerAlgo = overlapPerAlgo, limitTop=1000)
		##Move to the main result file
		saveTimes(timesPerConfiguration)

		saveMatrixOverlapConfig(matrixOverlapConfigurations)


def saveTimes(timesPerConfiguration,directory = "./plots/data/" ):
	if not os.path.exists(directory):
		os.makedirs(directory)

	fbestFile = open("{}/times.csv".format(directory), "w")
	fbestFile.write("config,best_time_avg, notbest_time_avg\n")

	for config in timesPerConfiguration.keys():
		best_ = plainDict(timesPerConfiguration[config]["best"])
		meantbest = "" if len(best_) == 0 else mean(best_)
		medianbest = "" if len(best_) == 0 else np.median(best_)
		notbest_ = plainDict(timesPerConfiguration[config]["notbest"])
		meantnotbest= "" if len(notbest_) == 0 else mean(notbest_)
		mediannotbest = "" if len(notbest_) == 0 else np.median(notbest_)
		fbestFile.write("{},{},{},{},{}\n".format(config,meantbest,medianbest,meantnotbest, mediannotbest))
	fbestFile.close()


def saveMatrixOverlapConfig(matrixOverlapConfigurations, directory = "./plots/data"):

	if not os.path.exists(directory):
		os.makedirs(directory)

	sortedConfig = sorted(matrixOverlapConfigurations.keys())

	fbestFile = open("{}/matrix_overlap_configs.csv".format(directory), "w")
	fbestFile.write(",".join(sortedConfig))

	for config in sortedConfig:
		row = []
		for anotherConfig in sortedConfig:
			if anotherConfig not in matrixOverlapConfigurations[config]:
				row.append("")
			else:
				row.append(matrixOverlapConfigurations[config][anotherConfig])

		fbestFile.write(",".join(row))
		fbestFile.flush()

	fbestFile.close()


def computeFitnessOfFilePair(location, results, filename,datasetofPair, key = "all", overlap = [],
							 overlapPerAlgo = {}, debugBestbyConfiguration = {}, debug = True,
							timesPerConfiguration = {},
							sizePerConfiguration = {},
							 matrixOverlapConfiguration = {}
							 ):

	import pandas as pd
	import scipy.stats

	debugInfoByFile = {}

	nractions = datasetofPair["NRACTIONS"]
	pd_series = pd.Series(nractions)
	counts = pd_series.value_counts()
	entropy = scipy.stats.entropy(counts)
	#Take the min value of edit script size
	minES = nractions.min(skipna=True)
	allBestConfigurationOfFile = []
	totalRow = 0

	size = getTreeSize(location, filename)

	for rowConfiguration in datasetofPair.iterrows():
		totalRow +=1
		currentNrActions = rowConfiguration[1]['NRACTIONS']
		currentTime = rowConfiguration[1]['TIME']
		if(np.isnan(currentNrActions) or int(currentNrActions) == 0 ):
			continue

		distance = int(currentNrActions) - minES

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


			overlapPerAlgo[rowConfigurationKey] = {}
			for algo in propertiesPerMatcher.keys():
				overlapPerAlgo[rowConfigurationKey][algo] = {}

		incrementOne(results[rowConfigurationKey], distance)

		if distance == 0:
			allBestConfigurationOfFile.append(rowConfigurationKey)
			incrementOne(timesPerConfiguration[rowConfigurationKey]["best"], currentTime)
			incrementOne(sizePerConfiguration[rowConfigurationKey]["best"], size)
			if debug:
				debugBestbyConfiguration[rowConfigurationKey].append(filename)

		else:
			incrementOne(timesPerConfiguration[rowConfigurationKey]["notbest"], currentTime)
			incrementOne(sizePerConfiguration[rowConfigurationKey]["notbest"], size)

	## Stats per file
	proportionBest = len(allBestConfigurationOfFile) / totalRow
	if debug:
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
	for oneBestConfiguration in allBestConfigurationOfFile:
		algorithm_name = oneBestConfiguration.split("_")[0]
		for anotherAlgo in propertiesPerMatcher.keys():
			if anotherAlgo is not algorithm_name:
				percentageOverlap = countOverlapAlgo[anotherAlgo]
				incrementOne(overlapPerAlgo[oneBestConfiguration][anotherAlgo], percentageOverlap)

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
			if line_count == 1:
				size = row["L_SIZE"]
				return size
			line_count += 1


def printBest(results,  overlap, overlapPerAlgo,  limitTop = 1000, debug = False, directory = "./plots/data/"):
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
	fbestConfig.write("top, configuration, nrBest\n")

	for configuration in keySorted:
		nrBest = (results[configuration][0] if 0 in results[configuration] else 0)
		if (nrBest > 0):
			lbest = sorted(plainDict(overlap[configuration]))
			print("{} {} #{} overlap {} ".format(top, configuration, nrBest, lbest))
			fbestConfig.write("{},{},{}\n".format(top, configuration, nrBest))

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


def saveInfoOfFiles(filename, debugInfoByFile, directory = "./plots/data"):

	if not os.path.exists(directory):
		os.makedirs(directory)

	listProportions = []
	fbestFile = open("{}/best_file_summary_{}.csv".format(directory,filename), "w")
	fbestFile.write("file,nrBest,proportionBest,entropyNrActions\n")

	for filename in debugInfoByFile.keys():

		if debugInfoByFile[filename]["proportionBest"] > 0:
			listProportions.append(debugInfoByFile[filename]["proportionBest"])
			fbestFile.write("{},{},{},{}\n".format(filename,
												   debugInfoByFile[filename]["nrBest"],
												   debugInfoByFile[filename]["proportionBest"],
												   debugInfoByFile[filename]["entropyNrActions"]))

			detailBestFile = open("./plots/data/best_{}".format(filename), "w")
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


def getConfigurationkey(row):
		matcherName = row['MATCHER']
		key = matcherName;
		for property in propertiesPerMatcher[matcherName]:
			key+="_"+"{:.1f}".format((row[property])).rstrip('0').rstrip('.')

		return key

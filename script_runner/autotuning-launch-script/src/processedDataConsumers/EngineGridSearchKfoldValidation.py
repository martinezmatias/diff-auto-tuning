import zipfile

import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
from sklearn import datasets
from src.commons.DiffAlgorithmMetadata import *
from sklearn.model_selection import KFold, train_test_split
import pandas
import scipy
import numpy
from scipy.stats import wilcoxon, kruskal
import pingouin as pg
from sklearn.utils import shuffle
from src.commons.Utils import  *
from src.commons.Datalocation import  *
from src.processedDataConsumers.CostParameters import *
import math
import sys
PERFORMANCE_TESTING = "performanceOnTesting"


def computeGridSearchKFold(pathResults ="{}/distance_per_diff.csv".format(RESULTS_PROCESSED_LOCATION), overwrite = OVERWRITE_RESULTS, dfcomplete = None,kFold = 5, algorithm = None, defaultId = None, random_seed = 0, datasetname = None, out = RESULTS_PROCESSED_LOCATION, fration =1 ):

	print("****\nStart execution K {} algo {} random {} dataset {} fraction {}\n".format(kFold, algorithm, random_seed, datasetname, fration))
	out = "{}/GridSearch/".format(out)
	print("----\nRunning {} algoritm {}".format(pathResults, algorithm))
	k_fold = KFold(kFold, random_state=0)

	print("Default configuration used {}".format(defaultId))

	if not overwrite and alreadyAnalyzed(out = out, datasetname = datasetname, algorithm=algorithm, franctiondataset =  fration, randomseed=random_seed):
		print("EARLY END 1: Config already analyzed {} {} {} {} {}".format(out, datasetname, algorithm, fration, random_seed))
		return None


	if fration == 1 and random_seed > 0  and  alreadyAnalyzed(out = out, datasetname = datasetname,  algorithm=algorithm,  franctiondataset =  fration, randomseed=0):
		print("EARLY END 2: Already computed Grid for fraction 1. {} {} {} {} {}".format(out, datasetname, algorithm, fration,																 random_seed))
		return None

	print("Not executed previously")

	if dfcomplete is None:
		print("Computing dataset for a first time")
		import time
		start_time = time.time()
		dfcomplete  = pandas.read_csv(pathResults, sep=",")
		elapsed_time = time.time() - start_time
		print("Time loading data from disk: {}".format(time.strftime("%H:%M:%S", time.gmtime(elapsed_time))))
	else:
		print("dataset already loaded")

	print("Dataset fraction used {}".format(fration))
	print("DS size before {} ".format(dfcomplete.size))


	diffs = dfcomplete['diff']
	allDiff = list(diffs.values)
	print("All diffs in dataset {}".format(len(allDiff)))
	## let's shuffle the results, otherwise they are grouped by megadiff group id
	df = dfcomplete.sample(frac=fration, random_state=random_seed).reset_index(drop=True)
	print("DS size after {} ".format(df.size))

	if df.shape[0] <= kFold:
		return

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

	print("All configs considered with algo {}: {}".format(algorithm, len(allConfig)))
	# we get the first column, which has the diff names
	diffs = df['diff']

	allDiff = list(diffs.values)

	print("All diffs considered after reduction with proportion {} ({}%): {}".format(fration, fration * 100, len(allDiff)))

	performanceTestingOfBestAll = {}

	testingSets = []
	allBestFromTraining = []

	# For each Fold
	defaultInTestingK = []
	defaultInTrainingK = []
	bestInTrainingK = []
	bestInTestingK = []

	for k, (train, test) in enumerate(k_fold.split(allDiff)):
		X_train = []#[]
		X_test = []

		print("\n---------Running fold {}".format(k))

		# Create the training dataset
		for i in train:
			X_train.append(allDiff[i])

		# Create the testing dataset
		for i in test:
			X_test.append(allDiff[i])

		saveDiffFromFold(out=out, data=X_train, typeset=datasetname, k=k, algo=algorithm, name="diffOnTraining",
						 fraction=fration, randomseed=random_seed)
		saveDiffFromFold(out=out, data=X_test, typeset=datasetname, k=k, algo=algorithm, name="diffOnTesting",
						 fraction=fration, randomseed=random_seed)

		print("\nTraining {} size #diff: {}".format(k, len(X_train)))
		print("itraining min {}".format(min(train)))
		print("itest min {}".format(min(test)))



		##Training
		performanceTrainingPerDiff = computeAvgPerdiff(X_train, train, allConfig, allDiff, df)

		saveBestPerformances(out=out, data=performanceTrainingPerDiff, typeset=datasetname, k=k, algo=algorithm,
								 name="performanceTraining",
								 fraction=fration, randomseed=random_seed)

		## Select best and compare with default

		#print("Performance training {} in kfold {}".format(performanceTrainingPerDiff, k))
		bestOrder = list(sorted(allConfig, key=lambda x: performanceTrainingPerDiff[x]['av'], reverse=False))
		#print("Sorted training {} ".format(bestOrder))


		##
		bestFromTraining = bestOrder[0]
		bestInTrainingK.append(performanceTrainingPerDiff[bestFromTraining]['av'])
		print("Best training {} {}".format(k, performanceTrainingPerDiff[bestFromTraining]))
		defaultInTrainingK.append(performanceTrainingPerDiff[defaultId]['av'])

		if bestFromTraining not in allBestFromTraining:
			allBestFromTraining.append(bestFromTraining)

		configsForTesting = [bestFromTraining, defaultId]

		##Testing

		testingSets.append((X_test, test))
		performanceTestingOfBest = computeAvgPerdiff(X_test, test, configsForTesting, allDiff, df)


		print("Performance testing {} in kfold {}".format(performanceTestingOfBest, k))

		bestInTestingK.append(performanceTestingOfBest[bestFromTraining]['av'])
		defaultInTestingK.append(performanceTestingOfBest[defaultId]['av'])

		saveBestPerformances(out=out, data=performanceTestingOfBest, typeset=datasetname, k=k, algo=algorithm,
							 name="performanceTesting",
							 fraction=fration, randomseed=random_seed)



	# end K

	saveList(out, datasetname=datasetname, algorithm=algorithm, data=bestInTrainingK,
			 name="performanceBestOnTraining", fraction=fration, randomseed=random_seed)
	saveList(out, datasetname=datasetname, algorithm=algorithm, data=bestInTestingK,
			 name="performanceBestOnTesting", fraction=fration, randomseed=random_seed)
	saveList(out, datasetname=datasetname, algorithm=algorithm, data=defaultInTrainingK,
			 name="performanceDefaultOnTraining", fraction=fration, randomseed=random_seed)
	saveList(out, datasetname=datasetname, algorithm=algorithm, data=defaultInTestingK,
			 name="performanceDefaultOnTesting", fraction=fration, randomseed=random_seed)



		#now all validate
	bestOnTesting = {}
	for c in allBestFromTraining:
		bestOnTesting[c] = []

	print("Now, k fold")
	for k in range(0,kFold):
			X_test_i, test_i = testingSets[k]
			performanceTestingOfBest = computeAvgPerdiff(X_test_i, test_i, allBestFromTraining, allDiff, df)
			print("Performance testing {} in kfold {}".format(performanceTestingOfBest, k))


			for configInTraining in performanceTestingOfBest.keys():
				bestOnTesting[configInTraining].append(performanceTestingOfBest[configInTraining]['av'])

	saveAvgPerformancePerConfig(out=out, data=bestOnTesting, typeset=datasetname, algo=algorithm,
								name=PERFORMANCE_TESTING, fraction=fration, randomseed=random_seed)

	return dfcomplete


def computeAvgPerdiff(X_diff, selectedIndexDiff, allConfig, allDiff, df):
	performancePerDiff = {}
	ij = 0
	for iConfig in range(0, len(allConfig)):
		currentConfig = allConfig[iConfig]
		#print("Analyzing  {} config {}/{}".format(iConfig, currentConfig, len(allConfig)))

		valuesOfConfig = df[currentConfig]
		valuesSelected = []

		diffEvaluated = []
		for iT in range(0, len(selectedIndexDiff)):
			iInTrain = selectedIndexDiff[iT]
			# print("iDiff {}/{} : {} iTraining {}".format(iT, len(train), allDiff[iInTrain],  X_train[iT]))
			if allDiff[iInTrain] is not X_diff[iT]:
				print("Error, different diff")
				return None
			diffEvaluated.append(allDiff[iInTrain])
			if not math.isnan(valuesOfConfig[iInTrain]):

				if int(valuesOfConfig[iInTrain] ) is 0:
					print("found a zero in the matrix!!!")
					import sys
					sys.exit(1)

				valuesSelected.append(valuesOfConfig[iInTrain])

		ij+=1
		##

		avgConfig = np.mean(valuesSelected)
		#if ij <5:
		#	print("Total values collected for {}: ({}) {} {}".format(currentConfig, len(valuesSelected), np.mean(valuesSelected), valuesSelected))
		#	print(diffEvaluated)
		#print("Avg of config {}: {} ".format(currentConfig, avgConfig))
		performancePerDiff[currentConfig] = {"c": currentConfig, "av": avgConfig, "t": len(valuesSelected)}
	return performancePerDiff


def saveBestPerformances(out, data, typeset, k, algo ="", name ="", fraction=1, randomseed=0):

	algoName = "allAlgorithms" if algo is None else algo
	randomparentfolder = "{}/dataset_{}/algorithm_{}/seed_{}/fractionds_{}/".format(out, typeset, algoName, randomseed,
																					fraction, name)
	if not os.path.exists(randomparentfolder):
		os.makedirs(randomparentfolder)

	filename = "{}/summary_{}_{}_K_{}_{}_f_{}.csv".format(randomparentfolder, typeset, name, k, algoName, fraction)
	fout1 = open(filename, 'w')
	for conf in data.values():
			#fout1.write("{},{},{},{}\n".format(conf['c'], conf['av'], conf['bs'], conf['i']))
			fout1.write("{},{},{}\n".format(conf['c'], conf['av'], conf['t']))

	fout1.flush()
	fout1.close()
	print("Save results at {}".format(filename))

def saveDiffFromFold(out, data, typeset, k, algo ="", name ="", fraction=1, randomseed=0):

	algoName = "allAlgorithms" if algo is None else algo
	randomparentfolder = "{}/dataset_{}/algorithm_{}/seed_{}/fractionds_{}/".format(out, typeset, algoName, randomseed,
																					fraction, name)
	if not os.path.exists(randomparentfolder):
		os.makedirs(randomparentfolder)

	filename = "data_{}_{}_K_{}_{}_f_{}.txt".format(typeset, name, k, algoName, fraction)
	zipname = "{}/data_{}_{}_K_{}_{}_f_{}.zip".format(randomparentfolder, typeset, name, k, algoName, fraction)

	with zipfile.ZipFile(zipname, "w", zipfile.ZIP_DEFLATED) as myzip:
		with myzip.open(filename, "w") as fout1:
			for conf in data:
				fout1.write(str.encode("{}\n".format(conf)))
	print("Save results at {}".format(zipname))


def saveAvgPerformancePerConfig(out,  typeset, data = {}, algo = "",name = "" , fraction = 1, randomseed=0):

	algoName =  "allAlgorithms" if algo is None else  algo
	randomparentfolder = "{}/dataset_{}/algorithm_{}/seed_{}/fractionds_{}/".format(out, typeset, algoName,randomseed,fraction, name)
	if not os.path.exists(randomparentfolder):
		os.makedirs(randomparentfolder)

	filename = "{}/avg_performance_{}_{}_{}_f_{}.csv".format(randomparentfolder, name, typeset, algoName,fraction)
	fout1 = open(filename, 'w')
	means = {}
	for conf in data.keys():
			#fout1.write("{},{}\n".format(conf, np.mean(data[conf])))
			means[conf] = np.mean(data[conf])

	allconfigs = list(means.keys())
	allconfigs = sorted(allconfigs, key=lambda x: means[x], reverse=True)

	for xconf in allconfigs:
		fout1.write("{},{}\n".format(xconf, means[xconf]))
		fout1.flush()
	fout1.close()
	print("Save results at {}".format(filename))

def saveList(out,datasetname, data, algorithm, name, fraction, randomseed):

	algoName = "allAlgorithms" if algorithm is None else algorithm
	randomparentfolder = "{}/dataset_{}/algorithm_{}/seed_{}/fractionds_{}/".format(out, datasetname, algoName, randomseed,
																					fraction)
	if not os.path.exists(randomparentfolder):
		os.makedirs(randomparentfolder)

	filename = "{}/summary_{}_{}_{}_f_{}.csv".format(randomparentfolder,datasetname,algoName, name, fraction)
	fout1 = open(filename, 'w')
	for conf in data:
			fout1.write("{}\n".format(conf))
	fout1.flush()
	fout1.close()
	print("Save results at {}".format(filename))

def saveDefaultName(out,datasetname,  algorithm, name, fraction, randomseed, default):

	algoName = "allAlgorithms" if algorithm is None else algorithm
	randomparentfolder = "{}/dataset_{}/algorithm_{}/seed_{}/fractionds_{}/".format(out, datasetname, algoName, randomseed,
																					fraction)
	if not os.path.exists(randomparentfolder):
		os.makedirs(randomparentfolder)

	filename = "{}/default_configuration_{}_{}_{}_f_{}.csv".format(randomparentfolder,datasetname,algoName, name, fraction)
	fout1 = open(filename, 'w')

	fout1.write("{}\n".format(default))
	fout1.flush()
	fout1.close()
	print("Save results at {}".format(filename))

import os
def alreadyAnalyzed(out, datasetname,  algorithm, franctiondataset, randomseed):
	algoName = "allAlgorithms" if algorithm is None else algorithm
	randomparentfolder = "{}/dataset_{}/algorithm_{}/seed_{}/fractionds_{}/".format(out, datasetname, algoName,
																					randomseed,
																					franctiondataset)

	filename = "{}/avg_performance_{}_{}_{}_f_{}.csv".format(randomparentfolder, PERFORMANCE_TESTING, datasetname,
																	 "allAlgorithms" if algorithm is None else algorithm,
																	 franctiondataset)
	print("Checking existence of {}".format(filename))
	return  os.path.exists(filename)

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
				distance = rowDiff[positionOfConfig]
				## for performance, we use  isnam from math instead of np
				if not math.isnan(distance): #np.isnan(distance):
					valuesPerConfig[i].append(int(distance))
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
	xbestTraining = [round(x[field],4) for x in configsTraining]
	ybestTest = [round(x[field],4) for x in configsTesting]
	#print("index ({}) left {}".format(len(xbestTraining), ",".join(["%.3f"%x for x in xbestTraining])))
	#print("index ({}) right {}".format(len(ybestTest),",".join(["%.3f"%x for x in ybestTest])))
	#print("index ({}) left {}".format(len(xbestTraining), xbestTraining))
	#print("index ({}) right {}".format(len(ybestTest), ybestTest))

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
import time
def findBestRanking(X_train, allConfig, df, indexOfColumns):
	start_time = time.time()
	valuesPerConfig, presentPerConfig = analyzeConfigurationsFromDiffs(df, X_train, allConfig, indexOfColumns)
	elapsed_time = time.time() - start_time
	print("----Time parsing values: {}".format(time.strftime("%H:%M:%S", time.gmtime(elapsed_time))))

	start_time = time.time()
	configs, rankedBest = computeBestConfiguration(allConfig, presentPerConfig, valuesPerConfig)
	elapsed_time = time.time() - start_time
	print("----Time computing best values: {}".format(time.strftime("%H:%M:%S", time.gmtime(elapsed_time))))
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
	#print("Bests ({}) {}".format(len(bestOrder),bestOrder))

	for i in range(0, len(bestOrder)):
		bestOrder[i]["i"] = i

	return configs, bestOrder



def computeBestAndDefaultByFoldFiles(model, algoName, fileBest, fileDefault):
	computeBestAndDefaultByFold(model, algoName, readFileToFloatList(fileBest), readFileToFloatList(fileDefault))

'''Plots the numbers form rq1'''
def computeBestAndDefaultByFold(model, algoName, allBest = [], allDefault = []):
	print("\nModel: {} Comparing performance of best and default by fold. k={}".format(model, len(allBest)))
	print("{}: allBest mean {:.2f}\% (st {:.2f})".format(algoName, np.mean(allBest)* 100, np.std(allBest)* 100))
	print("{}: allDefault mean {:.2f}\% st {:.2f})".format(algoName, np.mean(allDefault)* 100, np.std(allDefault)* 100))
	improvements = []
	for i in range(0,len(allBest)):
		improvements.append(allBest[i] - allDefault[i])

	print("{}: improvement mean {:.2f}\% (st {:.2f})".format(algoName, np.mean(improvements) * 100, np.std(improvements)* 100))
	stat, pwil = wilcoxon(allBest, allDefault, alternative='two-sided')

	print('scipy wilcoxon: stat=%.3f, p=%.3f' % (stat, pwil))
'''plots the distribution for RQ1'''
def plotDistributionAvg( fileGT, fileCD, fileXY, defaultGT, defaultCD, defaultXy, model = "JDT", out = RESULTS_PROCESSED_LOCATION):

	allGT = readCSVToFloatList(fileGT)
	allCD = readCSVToFloatList(fileCD)
	allXY = readCSVToFloatList(fileXY)

	alldefaultGT = readFileToFloatList(defaultGT)
	alldefaultCD= readFileToFloatList(defaultCD)
	alldefaultXy= readFileToFloatList(defaultXy)

	defaultGt= np.mean(alldefaultGT)
	defaultCD= np.mean(alldefaultCD)
	defaultXY = np.mean(alldefaultXy)
	#plt.boxplot(gt)
	#plt.plot(1, 0.54, 'X', alpha=1)
	#plt.show()


	#plt.hist(gt)
	#plt.show()
	algos = [allGT, allCD, allXY]
	ax = sns.violinplot(data = algos,split=True,orient = "v" ,inner="quartile", cut=0, )
	ax.set_xticklabels(['GumTree', 'ChangeDistiller', 'Xy'])
	plt.plot(0, defaultGt, 'X', alpha=1, color='red', markersize=20 )
	plt.plot(1, defaultCD, 'X', alpha=1, color='red', markersize=20 )
	plt.plot(2, defaultXY, 'X', alpha=1, color='red', markersize=20 )
	plt.xticks(fontsize=20)
	plt.yticks(fontsize=20)
	#plt.xlabel("Diff Algorithm", fontproperties=20)
	plt.ylabel("Performance for {} ASTs".format(model), fontsize=14)
	outpath = "{}/distr_avg_performance_{}.pdf".format(out, model)
	print("Save plot at {}".format(outpath))
	plt.savefig(outpath)
	plt.show()

def countHigherValuesFile(path, thr = 0.05):
	print("\nAnalyzing {}".format(path))
	countHigherValues(readFileToFloatList(path), thr)

def countHigherValues(all, thr = 0.05):

	sup = list(filter(lambda x: x>thr, all))

	print("Total {} Sup {} ({:.2f}%)".format(len(all),len(sup), (len(sup)/len(all)*100)))


def runReadResultsCrossValidation(path = "/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/summary_performance_performance_{}_K_{}_{}.csv", dataset ="merge_gtJDT_5_CDJDT_4", onlytop = False ):

		corr_thr = 0.90
		dist_thr=0.05


		for algo in ["Gumtree", "ChangeDistiller", "Xy"]:
			person_sup = 0
			mann_sup = 0
			wilcoxon_sup = 0
			total = 0
			slopes =  []
			std_errs = []
			r_values = []
			for i in range(0, 10):
				for j in range(0, 10):
					if i> j:
						total +=1
						print("\nAlgo {} i {} j {}".format(algo, i, j))
						ki = path.format(dataset, i, algo)
						kj = path.format(dataset,j, algo)

						## let'salso check the names column zero to be sure
						li = readCSVToFloatList(ki, indexToKeep=2)
						lj = readCSVToFloatList(kj, indexToKeep=2)

						decimals = 2
						li = ([round(x,decimals) for x in   li])
						lj = ([round(x,decimals) for x in   lj])


						ci = readCSVToStringList(ki, indexToKeep=0)
						cj = readCSVToStringList(kj, indexToKeep=0)
						## Remove outliers
						if onlytop:
							soli = []
							solj = []
							mli = np.mean(li)
							mlj = np.mean(lj)

							sdli = np.std(li)
							sdlj = np.std(lj)

							for l in range(0, len(ci)):
								#if not isOutlier(value=li[l], mean=mli, std=sdli) and not isOutlier(value=lj[l], mean=mlj, std=sdlj):
								if li[l] >= 2 * sdli  and lj[l]>= 2 * sdlj:

									soli.append(li[l])
									solj.append(lj[l])

							print("Size after removing outliers {}".format(len(soli)))

							li= soli
							lj = solj

							print(li)
							print(lj)

						for l in range(0, len(ci)):
							if not ci[l] == cj[l]:

								print("error {}".format(l) )
								return
							#print("{} {} ".format(ci[l],cj[l]))

						print("Size data {}".format(len(li)))
						rp = scipy.stats.pearsonr(li, lj)
						print("Pearson's r {} ".format(rp))

						if rp[0] >corr_thr:
							person_sup+=1

						srho = scipy.stats.spearmanr(li, lj)
						print("Spearman's rho {} ".format(srho))

						#m, b  = np.polyfit(li, lj, 1)
						from scipy import stats
						slope, intercept, r_value, p_value, std_err = stats.linregress(li, lj)
						print("linear regression slope {}, intercept {}, r_value {}, p_value {}, std_err {}".format(slope, intercept, r_value, p_value, std_err ))
						slopes.append(slope)
						std_errs.append(std_err)
						r_values.append(r_value)
						stat, pwil = wilcoxon(li, lj, alternative='two-sided')

						print('scipy wilcoxon: stat=%.3f, p=%.3f' % (stat, pwil))

						if pwil >dist_thr:
							wilcoxon_sup+=1

						stat, pmann = scipy.stats.mannwhitneyu(li, lj, alternative='two-sided')

						print('scipy mannwhitneyu: stat=%.3f, p=%.3f' % (stat, pmann))
						if pmann > dist_thr:
							mann_sup += 1

			print("\nSummary {} total {} pperson {} ({}) , mann {} ({}) , wilcoxon {} ({}) ".format(algo, total, person_sup , 100*(person_sup/total), mann_sup, 100*(mann_sup/total), wilcoxon_sup, 100*(wilcoxon_sup/total)))

			print("avg r-values st slopes st   std err st ")
			print("& {:.5f} & {:.5f} & {:.5f} & {:.5f} & {:.5f} & {:.5f}\\".format(np.mean(r_values), np.std(r_values),np.mean(slopes), np.std(std_errs), np.mean(std_errs),np.std(std_errs)))
		print("\n----End {}".format(algo))

def isOutlier(value, mean,  std, m = 2):
	return abs(value - mean) > m * std



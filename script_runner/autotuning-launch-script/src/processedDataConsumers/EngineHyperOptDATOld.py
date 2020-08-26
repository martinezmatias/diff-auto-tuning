import hyperopt
from hyperopt import tpe, hp, fmin, Trials, rand
import pandas
import numpy as np
import os
from src.commons.DiffAlgorithmMetadata import *
from src.processedDataConsumers.EngineGridSearchKfoldValidation import *
from src.commons.DiffAlgorithmMetadata import *
from src.commons.Datalocation import *
from src.processedDataConsumers.CostParameters import *
import zipfile
import time

CONFIGS_PERFORMANCE = "bestConfigsPerformance"

AVG_CONSTANT = 'av'
import random
rangeSBUP = [ round(x,2) for x in np.arange(0.1,1.1,0.2)]
rangeMH = [ round(x,2) for x in np.arange(1,6,1)]
rangeGT_BUM_SMT = [ round(x,2) for x in np.arange(0.1,1.1,0.1)]
rangeGT_BUM_SZT = [ x for x in range(100,2001,100)]

rangeLSIM= [ round(x,2) for x in np.arange(0.1,1.1,0.2)]
rangeML = [ x for x in range(2,7,2)]
rangeSSIM1= [ round(x,2) for x in np.arange(0.2,1.1,0.2)]
rangeSSIM2= [ round(x,2) for x in np.arange(0.2,1.1,0.2)]

rangeXYSIM= [ round(x,2) for x in np.arange(0.1,1.1,0.1)]

notfound = []

def computeHyperOpt(pathResults, overwrite = OVERWRITE_RESULTS, useAverage = USE_AVG, dfcomplete = None, kFold=5, runTpe = True, max_evals=1000, random_seed = 0, fractiondata= 0.1, dataset ="alldata", algorithm = None, out = RESULTS_PROCESSED_LOCATION):
	out = "{}/{}/".format(out, "TPE" if runTpe else "random")
	print("\n***Start analysis:\nAnalyzing data from {}".format(pathResults))
	print("GT space size: {}".format(len(rangeGT_BUM_SMT) * len(rangeGT_BUM_SZT) * len(rangeMH)))
	print("SimpleGT space size: {}".format(len(rangeSBUP) * len(rangeMH)))
	print("CD space size: {}".format(len(rangeLSIM) * len(rangeML) * len(rangeSSIM1) * len((rangeSSIM2))))
	print("XY space size: {}".format(len(rangeXYSIM) * len(rangeMH)))
	print("Run TPE? {}".format(runTpe))
	print("Use Avg? {}".format(useAverage))
	print("Algorithm {}".format(algorithm))
	print("Overwrite results? {}".format(overwrite))
	print("Random_seed {}".format(random_seed))
	print("Total folds {}".format(kFold))
	print("Total evals {}".format(max_evals))
	print("Proportion dataset {}".format(fractiondata))

	inittime = time.time()
	random.seed(random_seed)

	if not overwrite and alreadyAnalyzedTPE(out = out, name =CONFIGS_PERFORMANCE, datasetname = dataset,  algorithm=algorithm, evals= max_evals,franctiondataset = fractiondata, isTPE=runTpe, randomseed=random_seed, useAvg=useAverage):
		print("EARLY END: Config already analyzed {} {} {} {} {} ".format(out, dataset, algorithm, fractiondata, random_seed))
		return None

	print("Executing new config")

	start_time_setup = time.time()

	if dfcomplete is None:
		print("Computing dataset for a first time {}".format(pathResults))
		dfcomplete = pandas.read_csv(pathResults, sep=",")
	else:
		print("dataset already loaded")

	print("dataset before random {}".format(dfcomplete.shape))

	diffs = dfcomplete['diff']
	allDiff = list(diffs.values)
	print("All diffs in dataset {}".format(len(allDiff)))

	df = dfcomplete.sample(frac=fractiondata, random_state=random_seed).reset_index(drop=True)

	print("dataset after random {}".format(df.shape))

	columns = list(df.columns)
	# We get the name of the configurations
	allConfig = columns[1:]
	#print(allConfig)

	indexOfConfig = {}

	# we start in 1 because the first is the diff
	for i in range(1, len(columns)):
		indexOfConfig[columns[i]] = i

	##We retrieve it again as the dataset could be probably reduced
	diffs = df['diff']

	allDiff = list(diffs.values)


	print("All diffs considered after reduction with proportion {} ({}%): {}".format(fractiondata, fractiondata * 100, len(allDiff)))

	k_fold = KFold(kFold, random_state=random_seed)

	###Those stores the mesures (avg, median, etc) of the best config
	mesureBestInTraining = []
	mesureBestInTesting = []

	mesureDefaultInTraining = []
	mesureDefaultInTesting = []

	###Those stores the proportionBest of the best config
	proportionBestInTraining = []
	proportionBestInTesting = []
	proportionBestAllDiffFromFraction = []

	proportionDefaultInTraining = []
	proportionDefaultInTesting = []
	proportionDefaultAllDiffFromFraction = []

	defaultConfigurationKey = None

	if algorithm is not None:
		keyDefault = None
		if algorithm is None or "Gumtree" in algorithm:
			keyDefault = "ClassicGumtree"
		else:
			keyDefault = algorithm

		defaultConfigurationKey = defaultConfigurations[keyDefault]

	bestConfigs = []

	if df.shape[0] <= kFold:
		return

	## Here we want to compute the distance from all diffs considered in this fraction (training + testing)

	rd, rd2, bestProportion_general = retrieveESsizeFromMatrix(df, allDiff, allConfig, indexOfConfig)
	if False:
		saveAvgPerformancePerConfigTPEAll(out=out, name="general", data= bestProportion_general, datasetname=dataset,
			 algorithm=algorithm,
			 evals=max_evals, franctiondataset=fractiondata, isTPE=runTpe, randomseed=random_seed, useAvg=useAverage,)


	elapsed_time_setup = time.time() - start_time_setup
	print("Time setup: {}".format(time.strftime("%H:%M:%S", time.gmtime(elapsed_time_setup))))

	start_time_kfold = time.time()

	# For each Fold
	for k, (train, test) in enumerate(k_fold.split(allDiff)):
		X_train = set([])
		X_test = set([])
		print("\n---------Running fold {}/{}".format(k,kFold))
		start_time = time.time()
		# Create the training dataset
		for i in train:
			X_train.add(allDiff[i])

		# Create the testing dataset
		for i in test:
			X_test.add(allDiff[i])

		saveDiffFromFoldTPE(out=out, data=X_train, typeset=dataset, k=k, algo=algorithm, name="diffOnTraining",
						 fraction=fractiondata, randomseed=random_seed, isTPE=runTpe)
		saveDiffFromFoldTPE(out=out, data=X_test, typeset=dataset, k=k, algo=algorithm, name="diffOnTesting",
						 fraction=fractiondata, randomseed=random_seed,  isTPE=runTpe)

		print("\nTraining {} #diff({})".format(k, len(X_train)))
		print("\nTesting {} #diff({})".format(k, len(X_test)))

		## let's compute first the metrics for each configuration
		configsTraining, rankedBestTraining, bestProportionTraining_k = findESAverageRanking(X_train, allConfig, df, indexOfColumns = indexOfConfig, useAvg=useAverage)

		configsTrainingMaps = {}
		for config in configsTraining:
			configsTrainingMaps[config['c']] = config

		print("Total Training configs {}".format(len(configsTrainingMaps.keys())))
		## Now the same for testing:

		configsTesting, rankedBestTesting, bestProportionTesting_k = findESAverageRanking(X_test, allConfig, df, indexOfColumns=indexOfConfig, useAvg=useAverage)

		configsTestingMaps = {}
		for config in configsTesting:
			configsTestingMaps[config['c']] = config

		print("Total Testing configs {}".format(len(configsTrainingMaps.keys())))
		keyBestConfigFound_k = None


		if runTpe:
			print("Running TPE")
			spaceAlgorithms = createSpace(algorithm=algorithm)
			search_space = { "space": hp.choice('algorithm_type', spaceAlgorithms),
			## A hack to pass the fitness of each configuration to the object function
			'data' :  configsTrainingMaps
			}
			trials = Trials()
			best = fmin(
				fn=objectiveFunctionDAT,
				space=search_space,
				algo=tpe.suggest if runTpe else rand.suggest,
				max_evals=max_evals,
				trials=trials,

			)

			eval = hyperopt.space_eval(search_space, best)
			print("finishing execution of Hyperopts for K {}".format(k))
			keyBestConfigFound_k = recreateConfigurationKey(eval)
		else:
			print("Running Random total configs: {}".format(len(allConfig)))
			minEdlength = 10000000;
			bestConfigFound = None
			for iEval in range(0, max_evals):
				iRandom = random.randint(0, len(allConfig))
				selectedConfig = allConfig[iRandom]

				dataOfConfig = configsTrainingMaps[selectedConfig]
				editScriptAvgSize = dataOfConfig[AVG_CONSTANT]
				print("#eval {} random Selected config {} length {} ".format(iEval, selectedConfig,editScriptAvgSize))
				if editScriptAvgSize < minEdlength:
					minEdlength = editScriptAvgSize
					bestConfigFound = selectedConfig

			if bestConfigFound is not None:
				keyBestConfigFound_k = bestConfigFound

		print("Best config found: {}".format(keyBestConfigFound_k))
		#print("eval {} {}".format(len(eval),eval))
		bestConfigs.append(keyBestConfigFound_k)

		print("Configs that could not be eval ({}) {}".format(len(notfound), notfound))

		if keyBestConfigFound_k not in configsTrainingMaps:
			print("Error: Key not in map, to continue {}".format(keyBestConfigFound_k))
			continue

		dataOfConfig = configsTrainingMaps[keyBestConfigFound_k]
		## this is a value between 0 (config not best in any diff) and 1 (config best in all diffs)
		bestMesure = dataOfConfig[AVG_CONSTANT]
		mesureBestInTraining.append(bestMesure)
		proportionBestInTraining.append(bestProportionTraining_k[keyBestConfigFound_k])
		print("Results k {}/{} config {} best testing metric {} proportion best {}  ".format(k, kFold, keyBestConfigFound_k, bestMesure,bestProportionTraining_k[keyBestConfigFound_k]))

		## the same but the testing:

		dataOfConfig = configsTestingMaps[keyBestConfigFound_k]
		## this is a value between 0 (config not best in any diff) and 1 (config best in all diffs)
		bestMesure = dataOfConfig[AVG_CONSTANT]
		mesureBestInTesting.append(bestMesure)
		proportionBestInTesting.append(bestProportionTesting_k[keyBestConfigFound_k])
		print("Results k {}/{} config {} best training metric {} proportion best {}  ".format(k, kFold, keyBestConfigFound_k, bestMesure, bestProportionTesting_k[keyBestConfigFound_k]))

		proportionBestAllDiffFromFraction.append(bestProportion_general[keyBestConfigFound_k])

		## now for the defaults
		if defaultConfigurationKey is not None and defaultConfigurationKey in configsTrainingMaps:
			dataOfConfig = configsTrainingMaps[defaultConfigurationKey]
			## this is a value between 0 (config not best in any diff) and 1 (config best in all diffs)
			bestMesure = dataOfConfig[AVG_CONSTANT]
			mesureDefaultInTraining.append(bestMesure)
			proportionDefaultInTraining.append(bestProportionTraining_k[defaultConfigurationKey])
			print("Performace default {} on training metric {} proportion best {}".format(defaultConfigurationKey, bestMesure, bestProportionTraining_k[defaultConfigurationKey]))


			dataOfConfig = configsTestingMaps[defaultConfigurationKey]
			## this is a value between 0 (config not best in any diff) and 1 (config best in all diffs)
			bestMesure = dataOfConfig[AVG_CONSTANT]
			mesureDefaultInTesting.append(bestMesure)
			proportionDefaultInTesting.append(bestProportionTesting_k[defaultConfigurationKey])
			print("Performace default {} on testing metric {} proportion best".format(defaultConfigurationKey, bestMesure, bestProportionTesting_k[defaultConfigurationKey]))

			proportionDefaultAllDiffFromFraction.append(bestProportion_general[defaultConfigurationKey])

		else:
			print("Could not determine default {}".format(defaultConfigurationKey))

		elapsed_time = time.time() - start_time
		print("Time for k {} {}".format(k, time.strftime("%H:%M:%S", time.gmtime(elapsed_time))))

	##Best configs per K fold
	saveList(out = out, name =CONFIGS_PERFORMANCE, bestTraining = mesureBestInTraining, bestTesting = mesureBestInTesting,
			 names = bestConfigs, datasetname = dataset, algorithm=algorithm, evals= max_evals,
			 franctiondataset = fractiondata, isTPE=runTpe, randomseed=random_seed, useAvg=useAverage)

	saveList(out=out, name="bestConfigsProportionBest", bestTraining=proportionBestInTraining, bestTesting=proportionBestInTesting,
			 names=bestConfigs, datasetname=dataset, algorithm=algorithm, evals=max_evals,
			 franctiondataset=fractiondata, isTPE=runTpe, randomseed=random_seed, useAvg=useAverage, bestGeneral=proportionBestAllDiffFromFraction)

	##Performance of the default per K fold
	defaultConfigNameList = averages = [defaultConfigurationKey for x in (mesureDefaultInTesting)]
	saveList(out=out, name="defaultConfigsPerformance", bestTraining=mesureDefaultInTraining,
			 bestTesting=mesureDefaultInTesting, names=defaultConfigNameList, datasetname=dataset, algorithm=algorithm,
			 evals=max_evals, franctiondataset=fractiondata, isTPE=runTpe, randomseed=random_seed, useAvg=useAverage)

	saveList(out=out, name="defaultConfigsProportionBest", bestTraining=proportionDefaultInTraining,
			 bestTesting=proportionDefaultInTesting, names=defaultConfigNameList, datasetname=dataset, algorithm=algorithm,
			 evals=max_evals, franctiondataset=fractiondata, isTPE=runTpe, randomseed=random_seed, useAvg=useAverage, bestGeneral=proportionDefaultAllDiffFromFraction)

	elapsed_time_kfold = time.time() - start_time_kfold
	print("Time kfolds: {}".format(time.strftime("%H:%M:%S", time.gmtime(elapsed_time_kfold))))

	elapsed_time = time.time() - inittime
	print("END total time after {} k {}".format(kFold, time.strftime("%H:%M:%S", time.gmtime(elapsed_time))))

	return dfcomplete

def findESAverageRanking(X_train, allConfig, df, indexOfColumns, useAvg):
	valuesPerConfig, presentPerConfig, bestProportion = retrieveESsizeFromMatrix(df, X_train, allConfig, indexOfColumns)

	configs, rankedBest = computeAvgSizeConfiguration(allConfig, presentPerConfig, valuesPerConfig, useAvg)
	return configs, rankedBest, bestProportion


def computeAvgSizeConfiguration(allConfig, presentPerConfig, valuesPerConfig, useAvg):

	averages = [0 for x in (allConfig)]
	configs= []
	for i in range(0, len(allConfig)):
		averages[i] =  np.mean(valuesPerConfig[i]) if useAvg or len((valuesPerConfig[i])) <= 2 else np.median(valuesPerConfig[i])
			#np.mean(valuesPerConfig[i]) #np.mean(valuesPerConfig[i]) if len((valuesPerConfig[i])) <= 2 else np.median(valuesPerConfig[i])
		#print("Config at i {}, {}, avg: {}, total values: {}, all values: {} ".format(i, allConfig[i],averages[i], len(valuesPerConfig[i]), valuesPerConfig[i]))
		configs.append({'c': allConfig[i], AVG_CONSTANT: averages[i]})

	##Sorting according number of best
	bestOrder = sorted(configs, key=lambda x: x[AVG_CONSTANT], reverse=False)
	print("Bests avg ({}) {}".format(len(bestOrder),bestOrder[0]))

	for i in range(0, len(bestOrder)):
		bestOrder[i]["i"] = i

	return configs, bestOrder

'''
df is the  dataframe with all data
X: the list of the diffs to consider (because we may not be interested in analyzing all diffs, specially on the k-fold)
allconfig: the key of all configurations 
'''
def retrieveESsizeFromMatrix(df, setofDiffToConsider, allconfig, indexOfColumns):

	# This array stores, per configuration, a list with all the distance values
	valuesPerConfig = [[] for i in allconfig]

	# This array stores, per configuration, the number of diffs analyzed
	presentPerConfig = [0 for i in allconfig]
	## Stores the diffs id having at least one configuration which produces zero changes.
	diffsWithEDlenghtZeros = []
	countDiffToConsider = 0

	#Store the distances only considering the diffs to consider given as parameter
	distancesPerConfig = {}

	for rowDiff in df.itertuples():

		# in the DataFrame row the first two positions are the tuple id and diff name.
		## example #<class 'tuple'>: (22671, 'nr_98_id_1_010de14013c38b7f82e4755270e88a8249f3a825_SimpleConveyer_GTSPOON.csv', 2.0, 16.0, 80.0, 80.0,192.0, ...
		diff_ID = rowDiff[1]
		if diff_ID in setofDiffToConsider:
			countDiffToConsider += 1
			# in the DataFrame row the first two positions are the tuple id and diff name. So, we start in the Shift = 1 position
			shift = 1
			##only to check we dont have zeros
			zerosOfDiff = 0

			## Lenght of the shortest ed found for this diff
			minESlengthOfDiff = 10000000

			cacheLengths = {}
			for i in range(0, len(allconfig)):

				currentConfig = allconfig[i]
				indexOfcurrent =  indexOfColumns[currentConfig]
				positionOfConfig = shift + indexOfcurrent
				aLenght = rowDiff[positionOfConfig]
				## for performance, we use  isnam from math instead of np
				if not math.isnan(aLenght): #np.isnan(distance):
					anIntLenght = int(aLenght)
					cacheLengths[currentConfig] = anIntLenght
					#We discard ES size with zeros, and we report
					if anIntLenght > 0:
						valuesPerConfig[i].append(anIntLenght)
						presentPerConfig[i]+=1

						# store if it's the min
						if anIntLenght < minESlengthOfDiff:
							minESlengthOfDiff = anIntLenght

					else:
						zerosOfDiff+=1
			#we iterate again to compute the distances
			for iConfig in cacheLengths.keys():
				iLength = cacheLengths[iConfig]
				if iConfig not in distancesPerConfig:
					distancesPerConfig[iConfig] = []
				distanceWithMin = iLength - minESlengthOfDiff
				distancesPerConfig[iConfig].append(distanceWithMin)

			if zerosOfDiff > 0:
				diffsWithEDlenghtZeros.append(diff_ID, zerosOfDiff)

	if len(diffsWithEDlenghtZeros) > 0:
		print("Warning: diff with ED of lenght zero {}".format(zerosOfDiff))

	##Now, let's compute the number each config is the best
	bestProportion = {}
	countI = 0
	print("Total diff considered: {}".format(countDiffToConsider))
	for iConfig in distancesPerConfig.keys():
		countI+=1
		iDistances = distancesPerConfig[iConfig]
		zeros = len(list(filter(lambda x: x == 0,iDistances)))
		#print("{} zeros of {}: {} ({})".format(countI, iConfig, zeros,  (zeros / countDiffToConsider)))
		bestProportion[iConfig] = zeros / countDiffToConsider

	return valuesPerConfig, presentPerConfig, bestProportion

def createSpace(algorithm = None):
	spaceAlgorithms = [
		{  # ["GT_BUM_SMT_SBUP", "GT_STM_MH"]
			'algorithm': 'SimpleGumtree',
			"SimpleGumtree_GT_BUM_SMT_SBUP": hp.choice("SimpleGumtree_GT_BUM_SMT_SBUP", rangeSBUP),
			"SimpleGumtree_GT_STM_MH": hp.choice("SimpleGumtree_GT_STM_MH", rangeMH)
		},
		{  # ["GT_BUM_SMT", "GT_BUM_SZT", "GT_STM_MH"]
			'algorithm': 'ClassicGumtree',
			"ClassicGumtree_GT_BUM_SMT": hp.choice("ClassicGumtree_GT_BUM_SMT", rangeGT_BUM_SMT),
			"ClassicGumtree_GT_BUM_SZT": hp.choice("ClassicGumtree_GT_BUM_SZT", rangeGT_BUM_SZT),
			"ClassicGumtree_GT_STM_MH": hp.choice("ClassicGumtree_GT_STM_MH", rangeMH),
		},
		{  # ["GT_BUM_SMT", "GT_BUM_SZT", "GT_STM_MH"]
			'algorithm': 'CompleteGumtreeMatcher',
			"CompleteGumtreeMatcher_GT_BUM_SMT": hp.choice("CompleteGumtreeMatcher_GT_BUM_SMT", rangeGT_BUM_SMT),
			"CompleteGumtreeMatcher_GT_BUM_SZT": hp.choice("CompleteGumtreeMatcher_GT_BUM_SZT", rangeGT_BUM_SZT),
			"CompleteGumtreeMatcher_GT_STM_MH": hp.choice("CompleteGumtreeMatcher_GT_STM_MH", rangeMH),
		},
		{  # ["GT_CD_LSIM", "GT_CD_ML","GT_CD_SSIM1",  "GT_CD_SSIM2"]
			'algorithm': 'ChangeDistiller',
			"ChangeDistiller_GT_CD_LSIM": hp.choice("ChangeDistiller_GT_CD_LSIM", rangeLSIM),
			"ChangeDistiller_GT_CD_ML": hp.choice("ChangeDistiller_GT_CD_ML", rangeML),
			"ChangeDistiller_GT_CD_SSIM1": hp.choice("ChangeDistiller_GT_CD_SSIM1", rangeSSIM1),
			"ChangeDistiller_GT_CD_SSIM2": hp.choice("ChangeDistiller_GT_CD_SSIM2", rangeSSIM2),
		},
		# ["GT_STM_MH", "GT_XYM_SIM"]
		{
			'algorithm': 'XyMatcher',
			"XyMatcher_GT_STM_MH": hp.choice("XyMatcher_GT_STM_MH", rangeMH),
			"XyMatcher_GT_XYM_SIM": hp.choice("XyMatcher_GT_XYM_SIM", rangeXYSIM),
		},
	]
	if algorithm is not None:
		spaceAlgorithms =  list(filter(lambda x: algorithm in x['algorithm'], spaceAlgorithms))

	return spaceAlgorithms

def alreadyAnalyzedTPE(out, name, datasetname,  algorithm, franctiondataset, evals, isTPE = True,  randomseed = 0, useAvg = True):
	executionmode = "hyper_op" if isTPE else "random_op"
	algoName = "allAlgorithms" if algorithm is None else algorithm
	randomparentfolder = "{}/{}/dataset_{}/algorithm_{}/seed_{}/fractionds_{}/".format(out, executionmode, datasetname,
																					   algoName,
																					   randomseed,
																					   franctiondataset)

	filename = "{}/{}_{}_{}_{}_evals_{}_f_{}_{}.csv".format(randomparentfolder, executionmode , datasetname, name, algoName, evals, franctiondataset,  "avg" if useAvg else "median")
	print("checking existance of {}".format(filename))
	return  os.path.exists(filename)

def saveList(out,bestTraining, name, bestTesting,names, datasetname,  algorithm, franctiondataset, evals, isTPE = True, randomseed = 0, useAvg = True, bestGeneral = None):
	executionmode = "hyper_op" if isTPE else "random_op"
	algoName = "allAlgorithms" if algorithm is None else algorithm
	randomparentfolder = "{}/{}/dataset_{}/algorithm_{}/seed_{}/fractionds_{}/".format(out,executionmode, datasetname, algoName,
																					randomseed,
																					franctiondataset)
	if not os.path.exists(randomparentfolder):
		os.makedirs(randomparentfolder)

	filename = "{}/{}_{}_{}_{}_evals_{}_f_{}_{}.csv".format(randomparentfolder,executionmode , datasetname, name, algoName, evals, franctiondataset, "avg" if useAvg else "median")
	fout1 = open(filename, 'w')
	for i in range(0, len(bestTraining)):
			if bestGeneral is None:
				fout1.write("{},{},{},{}\n".format(i,names[i], bestTraining[i],bestTesting[i]))
			else:
				fout1.write("{},{},{},{},{}\n".format(i, names[i], bestTraining[i], bestTesting[i],  bestGeneral[i]))
	fout1.flush()
	fout1.close()
	print("Save data on {}".format(filename))

def saveDiffFromFoldTPE_old(out, data, typeset, k, algo ="", name ="", fraction=1,  isTPE = True, randomseed=0):
	executionmode = "hyper_op" if isTPE else "random_op"
	algoName = "allAlgorithms" if algo is None else algo
	randomparentfolder = "{}/{}/dataset_{}/algorithm_{}/seed_{}/fractionds_{}/".format(out, executionmode, typeset, algoName, randomseed,
																					fraction, name)
	if not os.path.exists(randomparentfolder):
		os.makedirs(randomparentfolder)

	filename = "{}/data_{}_{}_K_{}_{}_f_{}.csv".format(randomparentfolder, typeset, name, k, algoName, fraction)
	fout1 = open(filename, 'w')
	for conf in data:
			fout1.write("{}\n".format(conf))
	fout1.flush()
	fout1.close()
	print("Save results at {}".format(filename))

def saveDiffFromFoldTPE(out, data, typeset, k, algo ="", name ="", fraction=1,  isTPE = True, randomseed=0):
	executionmode = "hyper_op" if isTPE else "random_op"
	algoName = "allAlgorithms" if algo is None else algo
	randomparentfolder = "{}/{}/dataset_{}/algorithm_{}/seed_{}/fractionds_{}/".format(out, executionmode, typeset, algoName, randomseed,
																					fraction, name)
	if not os.path.exists(randomparentfolder):
		os.makedirs(randomparentfolder)
	filenamedata = "data_{}_{}_K_{}_{}_f_{}.txt".format(typeset, name, k, algoName, fraction)

	filenamezip = "{}/data_{}_{}_K_{}_{}_f_{}.zip".format(randomparentfolder, typeset, name, k, algoName, fraction)
	with zipfile.ZipFile(filenamezip, "w", zipfile.ZIP_DEFLATED) as myzip:
		with myzip.open(filenamedata, "w") as fout1:
			for conf in data:
				fout1.write(str.encode("{}\n".format(conf)))
				#fout1.flush()
				#fout1.close()
	print("Save results at {}".format(filenamezip))



def objectiveFunctionDAT(params):

	if True:
		print("skip")
		return 0

	## we attach the data in the parameter space.
	dataBestConfigurations = params['data']

	keyConfig = recreateConfigurationKey(params)

	if keyConfig not in dataBestConfigurations.keys():
		# as fmin aims at minimizing, let's send 1
		print("{} not found ".format(keyConfig))
		notfound.append(keyConfig)
		## a large value
		return 10000000000

	dataOfConfig = dataBestConfigurations[keyConfig]
	editScriptAvgSize = dataOfConfig[AVG_CONSTANT]#dataOfConfig['bs']
	#print("--> Config {} edSize {} ".format(keyConfig, editScriptAvgSize))
	## As fmin aims at minimizing, so shortest avg is the best
	return editScriptAvgSize


def recreateConfigurationKey(params):
	algo = params['space']['algorithm']
	key = [algo]
	for iParameter in propertiesPerMatcher[algo]:
		key.append(str(params['space']["{}_{}".format(algo, iParameter)]))
	keyConfig = ("_".join(key)).replace("_1.0", "_1")
	return keyConfig

def computeAvgLengthsFromHyperop(path, algo):
	print(algo)
	#the 0 is index
	#the 1 is the config
	#the 2 is the performance on training
	#the 3 is the performance on testing
	#
	performances = readCSVToFloatList(path, indexToKeep=3)
	#print("& {:.5f}\% & {:.5f} ".format(np.mean(performances)* 100, np.std(performances)* 100))
	print("& {:.2f}\%  (st {:.2f})".format(np.mean(performances)* 100, np.std(performances)* 100))

from src.processedDataConsumers.RQ3_PerformanceMetamodel_MetaResultsCompareDistribution import *
from src.commons.DiffAlgorithmMetadata import *
def analyzeResultsHyperop2(pathDistances, pathSize, pathBest, pathDefault, algo, model, seed = 0):
	print("\nmodel {} algo {}".format(model, algo))
	#the 0 is index
	#the 1 is the config
	#the 2 is the performance on training
	#the 3 is the performance on testing
	#
	print(pathBest)

	dfDistances = pandas.read_csv(pathDistances, sep=",")
	dfSize = pandas.read_csv(pathSize, sep=",")

	allBestperK = readCSVtoAll(pathBest)
	allDefaultperK = readCSVtoAll(pathDefault)
	# we retrieve the name of the configuration

	allPercentageBest = []
	allPercentageDefault = []

	allMetricBest = []
	allMetricDefault = []

	allConfigurationBestFound = []

	differencesMetric = []

	for i in range(0, len(allBestperK)):
		#for each row (a k fold result)
		# # we retrieve the name of the configuration
		iConfig = str(allBestperK[i][1])
		allConfigurationBestFound.append(iConfig)
		iDefaultConfig = str(allDefaultperK[i][1])

		# we retrieve the performance on testing (metric avg or median)
		bestMetric = float(allBestperK[i][3])
		defaultMetric = float(allDefaultperK[i][3])

		differencesMetric.append(defaultMetric - bestMetric)

		allMetricBest.append(bestMetric)
		allMetricDefault.append(defaultMetric)
		allConfigurationBestFound.append(iConfig)

		print("config {} at K {} ".format(iConfig, i))
		percentageBest, percentageDefault = crossResultsDatasets(dfDistances, dfSize, iConfig, iDefaultConfig)
		print("best {} , default {} ".format(percentageBest, percentageDefault))
		allPercentageBest.append(percentageBest)
		allPercentageDefault.append(percentageDefault)

	print("Partial summary for seed {}:".format(seed))
	print("{} {} Best & {:.2f}\%  (st {:.2f})".format(model,algo, np.mean(allPercentageBest) * 100, np.std(allPercentageBest) * 100))
	print("{} {} Default & {:.2f}\%  (st {:.2f})".format(model, algo, np.mean(allPercentageDefault) * 100, np.std(allPercentageDefault) * 100))
	print("-----")
	return allPercentageBest, allPercentageDefault, allMetricBest, allMetricDefault, allConfigurationBestFound, differencesMetric



def saveAvgPerformancePerConfigTPEAll(out, name, datasetname,  algorithm, franctiondataset, evals, isTPE = True, randomseed = 0, useAvg = True,data = {}):
	executionmode = "hyper_op" if isTPE else "random_op"
	algoName = "allAlgorithms" if algorithm is None else algorithm
	randomparentfolder = "{}/{}/dataset_{}/algorithm_{}/seed_{}/fractionds_{}/".format(out,executionmode, datasetname, algoName,
																					randomseed,
																					franctiondataset)
	if not os.path.exists(randomparentfolder):
		os.makedirs(randomparentfolder)

	filename = "{}/{}_{}_proportions_{}_{}_evals_{}_f_{}_{}.csv".format(randomparentfolder,executionmode, datasetname, name, algoName, evals, franctiondataset, "avg" if useAvg else "median")
	fout1 = open(filename, 'w')

	allconfigs = list(data.keys())
	allconfigs = sorted(allconfigs, key=lambda x: data[x], reverse=True)

	for xconf in allconfigs:
		fout1.write("{},{}\n".format(xconf, data[xconf]))
		fout1.flush()
	fout1.close()
	print("Save results at {}".format(filename))

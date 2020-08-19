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

AVG_CONSTANT = 'av'

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
#editscript_size_per_diff
def computeHyperOpt(pathResults, dfcomplete = None, kFold=5, runTpe = True, max_evals=1000, random_seed = 0, fractiondata= 0.1,  dataset = "alldata", algorithm = None,  out = RESULTS_PROCESSED_LOCATION):
	out = "{}/{}/".format(out, "TPE" if runTpe else "random")
	print("GT space size: {}".format(len(rangeGT_BUM_SMT) * len(rangeGT_BUM_SZT) * len(rangeMH)))
	print("SimpleGT space size: {}".format(len(rangeSBUP) * len(rangeMH)))
	print("CD space size: {}".format(len(rangeLSIM) * len(rangeML) * len(rangeSSIM1) * len((rangeSSIM2))))
	print("XY space size: {}".format(len(rangeXYSIM) * len(rangeMH)))
	print("Run TPE? {}".format(runTpe))
	print("Algorithm {}".format(algorithm))
	print("Overwrite results? {}".format(overwriteResult))

	if not overwriteResult and alreadyAnalyzedTPE(out = out, datasetname = dataset,  algorithm=algorithm, evals= max_evals,franctiondataset = fractiondata, isTPE=runTpe, randomseed=random_seed):
		print("EARLY END: Config already analyzed {} {} {} {} {} ".format(out, dataset, algorithm, fractiondata, random_seed))
		return None

	print("Executing new config")

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

	k_fold = KFold(kFold, random_state=0)

	performanceBestInTraining = []
	performanceBestInTesting = []
	bestConfigs = []

	if df.shape[0] <= kFold:
		return

	# For each Fold
	for k, (train, test) in enumerate(k_fold.split(allDiff)):
		X_train = set([])
		X_test = set([])
		print("\n---------Running fold {}".format(k))

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

		print("\nTraining {} ".format(k))

		## let's compute first the metrics for each configuration
		configsTraining, rankedBestTraining = findESAverageRanking(X_train, allConfig, df, indexOfColumns = indexOfConfig)

		configsTrainingMaps = {}
		for config in configsTraining:
			configsTrainingMaps[config['c']] = config

		print("Total Training configs {}".format(len(configsTrainingMaps.keys())))

		## Now the same for testing:

		configsTesting, rankedBestTesting = findESAverageRanking(X_test, allConfig, df, indexOfColumns=indexOfConfig)

		configsTestingMaps = {}
		for config in configsTesting:
			configsTestingMaps[config['c']] = config

		print("Total Testing configs {}".format(len(configsTrainingMaps.keys())))

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
		keyConfig = recreateConfigurationKey(eval)
		#print("eval {} {}".format(len(eval),eval))
		bestConfigs.append(keyConfig)

		if keyConfig not in configsTrainingMaps:
			continue

		dataOfConfig = configsTrainingMaps[keyConfig]
		## this is a value between 0 (config not best in any diff) and 1 (config best in all diffs)
		bestPercentage = dataOfConfig[AVG_CONSTANT]
		print("Results k {} config {} best testing {} ".format(k, keyConfig, bestPercentage))

		performanceBestInTraining.append(bestPercentage)

		## the same but the testing:

		dataOfConfig = configsTestingMaps[keyConfig]
		## this is a value between 0 (config not best in any diff) and 1 (config best in all diffs)
		bestPercentage = dataOfConfig[AVG_CONSTANT]
		print("Results k {} config {} best training {} ".format(k, keyConfig, bestPercentage))

		performanceBestInTesting.append(bestPercentage)

	saveList(out = out,bestTraining = performanceBestInTraining , bestTesting = performanceBestInTesting,names = bestConfigs, datasetname = dataset,  algorithm=algorithm, evals= max_evals,franctiondataset = fractiondata, isTPE=runTpe, randomseed=random_seed)
	return dfcomplete

def findESAverageRanking(X_train, allConfig, df, indexOfColumns):
	valuesPerConfig, presentPerConfig = analyzeConfigurationsFromDiffs(df, X_train, allConfig, indexOfColumns)
	configs, rankedBest = computeAvgSizeConfiguration(allConfig, presentPerConfig, valuesPerConfig)
	return configs, rankedBest


def computeAvgSizeConfiguration(allConfig, presentPerConfig, valuesPerConfig):

	averages = [0 for x in (allConfig)]
	configs= []
	for i in range(0, len(allConfig)):
		averages[i] = np.mean(valuesPerConfig[i]) if len((valuesPerConfig[i])) <= 2 else np.median(valuesPerConfig[i])

		configs.append({'c': allConfig[i], AVG_CONSTANT: averages[i]})

	##Sorting according number of best
	bestOrder = sorted(configs, key=lambda x: x[AVG_CONSTANT], reverse=False)
	print("Bests avg ({}) {}".format(len(bestOrder),bestOrder[0]))

	for i in range(0, len(bestOrder)):
		bestOrder[i]["i"] = i

	return configs, bestOrder

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

def alreadyAnalyzedTPE(out, datasetname,  algorithm, franctiondataset, evals, isTPE = True,  randomseed = 0):
	executionmode = "hyper_op" if isTPE else "random_op"
	algoName = "allAlgorithms" if algorithm is None else algorithm
	randomparentfolder = "{}/{}/dataset_{}/algorithm_{}/seed_{}/fractionds_{}/".format(out, executionmode, datasetname,
																					   algoName,
																					   randomseed,
																					   franctiondataset)

	filename = "{}/{}_{}_{}_evals_{}_f_{}.csv".format(randomparentfolder, executionmode , datasetname, algoName, evals, franctiondataset)
	print("checking existance of {}".format(filename))
	return  os.path.exists(filename)

def saveList(out,bestTraining, bestTesting,names, datasetname,  algorithm, franctiondataset, evals, isTPE = True, randomseed = 0):
	executionmode = "hyper_op" if isTPE else "random_op"
	algoName = "allAlgorithms" if algorithm is None else algorithm
	randomparentfolder = "{}/{}/dataset_{}/algorithm_{}/seed_{}/fractionds_{}/".format(out,executionmode, datasetname, algoName,
																					randomseed,
																					franctiondataset)
	if not os.path.exists(randomparentfolder):
		os.makedirs(randomparentfolder)

	filename = "{}/{}_{}_{}_evals_{}_f_{}.csv".format(randomparentfolder,executionmode , datasetname, algoName, evals, franctiondataset)
	fout1 = open(filename, 'w')
	for i in range(0, len(bestTraining)):
			fout1.write("{},{},{},{}\n".format(i,names[i], bestTraining[i],bestTesting[i]))
	fout1.flush()
	fout1.close()
	print("Save data on {}".format(filename))

def saveDiffFromFoldTPE(out, data, typeset, k, algo ="", name ="", fraction=1,  isTPE = True, randomseed=0):
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


def objectiveFunctionDAT(params):

	## we attach the data in the parameter space.
	dataBestConfigurations = params['data']

	keyConfig = recreateConfigurationKey(params)

	if keyConfig not in dataBestConfigurations.keys():
		# as fmin aims at minimizing, let's send 1
		print("{} not found ".format(keyConfig))
		notfound.append(keyConfig)
		return 1

	dataOfConfig = dataBestConfigurations[keyConfig]
	## this is a value between 0 (config not best in any diff) and 1 (config best in all diffs)
	bestPercentage = dataOfConfig[AVG_CONSTANT]#dataOfConfig['bs']
	print("config {} best {} ".format(keyConfig, bestPercentage))
	## As fmin aims at minimizing
	return 1 - bestPercentage


def recreateConfigurationKey(params):
	algo = params['space']['algorithm']
	key = [algo]
	print("Algorithm {}".format(algo))
	for iParameter in propertiesPerMatcher[algo]:
		key.append(str(params['space']["{}_{}".format(algo, iParameter)]))
	keyConfig = "_".join(key)
	print("key {}".format(keyConfig))
	return keyConfig

def analyzeResultsHyperop(path, algo):
	print(algo)
	#the zero is the config
	#the one is the performance on training
	#the second is the performance on testing
	performances = readCSVToFloatList(path, indexToKeep=2)
	#print("& {:.5f}\% & {:.5f} ".format(np.mean(performances)* 100, np.std(performances)* 100))
	print("& {:.2f}\%  (st {:.2f})".format(np.mean(performances)* 100, np.std(performances)* 100))
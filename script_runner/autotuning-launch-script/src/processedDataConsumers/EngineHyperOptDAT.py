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
X_trainingGlobal = None
trainingGlobal = None
dfGlobal = None

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

	global dfGlobal

	dfGlobal = df

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

	testingSets = []
	allBestFromTraining = []

	# For each Fold
	defaultInTestingK = []
	defaultInTrainingK = []
	bestInTrainingK = []
	bestInTestingK = []

	allProportionBestK = []
	allProportionDefaultK = []
	allProportionEqualsK = []



	defaultConfigurationKey = None

	if algorithm is not None:
		keyDefault = None
		if algorithm is None or "Gumtree" in algorithm:
			keyDefault = "ClassicGumtree"
		else:
			keyDefault = algorithm

		defaultConfigurationKey = defaultConfigurations[keyDefault]


	if df.shape[0] <= kFold:
		return


	elapsed_time_setup = time.time() - start_time_setup
	print("end Time setup: {}".format(time.strftime("%H:%M:%S", time.gmtime(elapsed_time_setup))))

	start_time_kfold = time.time()

	# For each Fold
	for k, (train, test) in enumerate(k_fold.split(allDiff)):
		X_train = []
		X_test = []

		print("\n---------Running fold {}/{}".format(k,kFold))
		start_time = time.time()
		# Create the training dataset
		for i in train:
			X_train.append(allDiff[i])

		# Create the testing dataset
		for i in test:
			X_test.append(allDiff[i])

		saveDiffFromFold(out=out, data=X_train, typeset=dataset, k=k, algo=algorithm, name="diffOnTraining",
						 fraction=fractiondata, randomseed=random_seed)
		saveDiffFromFold(out=out, data=X_test, typeset=dataset, k=k, algo=algorithm, name="diffOnTesting",
						 fraction=fractiondata, randomseed=random_seed)


		print("\nCreating Training DS{} #diff({})".format(k, len(X_train)))
		print("\nCreating Testing DS {} #diff({})".format(k, len(X_test)))

		global X_trainingGlobal
		global trainingGlobal

		X_trainingGlobal = X_train
		trainingGlobal = train

		keyBestConfigFound_k = None

		if runTpe:
			print("Running TPE")
			spaceAlgorithms = createSpace(algorithm=algorithm)
			search_space = { "space": hp.choice('algorithm_type', spaceAlgorithms),
			## A hack to pass the fitness of each configuration to the object function
			#'data' :  (X_train, train)
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

				configsForTesting = [selectedConfig]
				performanceTrainingOfBest = computeAvgPerdiff(X_train, trainingGlobal, configsForTesting,
															  None, df)

				dataOfConfig = performanceTrainingOfBest[selectedConfig]
				editScriptAvgSize = dataOfConfig[AVG_CONSTANT]

				#print("#eval {} random Selected config {} length {} ".format(iEval, selectedConfig,editScriptAvgSize))
				if editScriptAvgSize < minEdlength:
					minEdlength = editScriptAvgSize
					bestConfigFound = selectedConfig

			if bestConfigFound is not None:
				keyBestConfigFound_k = bestConfigFound

		##End search
		print("Best config found: {}".format(keyBestConfigFound_k))


		if keyBestConfigFound_k not in allBestFromTraining:
			allBestFromTraining.append(keyBestConfigFound_k)

		print("Configs that could not be eval ({}) {}".format(len(notfound), notfound))

		##Let's retrieve the values of training:
		configsForTesting = [keyBestConfigFound_k, defaultConfigurationKey]

		performanceTrainingOfBest = computeAvgPerdiff(X_train, train, configsForTesting, None, df)

		print("Performance training {} in kfold {}".format(performanceTrainingOfBest, k))

		bestInTrainingK.append(performanceTrainingOfBest[keyBestConfigFound_k]['av'])
		defaultInTrainingK.append(performanceTrainingOfBest[defaultConfigurationKey]['av'])



		##Testing
		print("\nTesting {} size #diff: {}".format(k, len(X_test)))
		testingSets.append((X_test, test))
		performanceTestingOfBest = computeAvgPerdiff(X_test, test, configsForTesting, None, df)

		print("Performance testing {} in kfold {}".format(performanceTestingOfBest, k))

		bestInTestingK.append(performanceTestingOfBest[keyBestConfigFound_k]['av'])
		defaultInTestingK.append(performanceTestingOfBest[defaultConfigurationKey]['av'])

		saveBestPerformances(out=out, data=performanceTestingOfBest, typeset=dataset, k=k, algo=algorithm,
							 name="performanceTesting",
							 fraction=fractiondata, randomseed=random_seed)

		totalPairsCompared, proportionBest, proportionDefault, proportionEqualsBestDefault = computeImprovementsOnTesting(
			X_test, test, allDiff, df, defaultConfig=defaultConfigurationKey, bestConfigFromTraining=keyBestConfigFound_k)
		allProportionBestK.append(proportionBest)
		allProportionDefaultK.append(proportionDefault)
		allProportionEqualsK.append(proportionEqualsBestDefault)

		##

		elapsed_time = time.time() - start_time
		print("Time for k {} {}".format(k, time.strftime("%H:%M:%S", time.gmtime(elapsed_time))))
		## end K

	saveList(out, datasetname=dataset, algorithm=algorithm, data=bestInTrainingK,
			 name="performanceBestOnTraining", fraction=fractiondata, randomseed=random_seed)
	saveList(out, datasetname=dataset, algorithm=algorithm, data=bestInTestingK,
			 name="performanceBestOnTesting", fraction=fractiondata, randomseed=random_seed)
	saveList(out, datasetname=dataset, algorithm=algorithm, data=defaultInTrainingK,
			 name="performanceDefaultOnTraining", fraction=fractiondata, randomseed=random_seed)
	saveList(out, datasetname=dataset, algorithm=algorithm, data=defaultInTestingK,
			 name="performanceDefaultOnTesting", fraction=fractiondata, randomseed=random_seed)

	saveList(out, datasetname=dataset, algorithm=algorithm, data=allProportionDefaultK,
			 name="proportionDefaultOnTesting", fraction=fractiondata, randomseed=random_seed)

	saveList(out, datasetname=dataset, algorithm=algorithm, data=allProportionBestK,
			 name="proportionBestOnTesting", fraction=fractiondata, randomseed=random_seed)

	saveList(out, datasetname=dataset, algorithm=algorithm, data=allProportionEqualsK,
			 name="proportionEqualsOnTesting", fraction=fractiondata, randomseed=random_seed)

	# now all validate
	bestOnTesting = {}
	for c in allBestFromTraining:
		bestOnTesting[c] = []

	print("Now, k fold")
	for k in range(0, kFold):
		X_test_i, test_i = testingSets[k]
		performanceTestingOfBest = computeAvgPerdiff(X_test_i, test_i, allBestFromTraining, allDiff, df)
		print("Performance testing {} in kfold {}".format(performanceTestingOfBest, k))

		for configInTraining in performanceTestingOfBest.keys():
			bestOnTesting[configInTraining].append(performanceTestingOfBest[configInTraining]['av'])

	saveAvgPerformancePerConfig(out=out, data=bestOnTesting, typeset=dataset, algo=algorithm,
								name=PERFORMANCE_TESTING, fraction=fractiondata, randomseed=random_seed)

	elapsed_time_kfold = time.time() - start_time_kfold
	print("Time kfolds: {}".format(time.strftime("%H:%M:%S", time.gmtime(elapsed_time_kfold))))

	elapsed_time = time.time() - inittime
	print("END total time after {} k {}".format(kFold, time.strftime("%H:%M:%S", time.gmtime(elapsed_time))))

	return dfcomplete


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


def objectiveFunctionDAT(params):

	global X_trainingGlobal
	global trainingGlobal
	global dfGlobal

	keyConfig = recreateConfigurationKey(params)

	configsForTraining = [keyConfig]

	performanceTrainingOfBest = computeAvgPerdiff(X_trainingGlobal, trainingGlobal, configsForTraining, None, dfGlobal)

	dataOfConfig = performanceTrainingOfBest[keyConfig]
	editScriptAvgSize = dataOfConfig[AVG_CONSTANT]
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


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
currentI = None
defaultConfigurationKey =None

def computeLocalHyperOpt(pathResults, overwrite = OVERWRITE_RESULTS, useAverage = USE_AVG, dfcomplete = None, kFold=5, runTpe = True, max_evals=1000, random_seed = 0, fractiondata= 0.1, dataset ="alldata", algorithm = None, out = RESULTS_PROCESSED_LOCATION):
	out = "{}/{}/".format(out, "TPE_local" if runTpe else "random_local")
	print("LOCAL OPT")
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

	evalsString = "_evals_{}".format(max_evals)
	# useAvg = useAverage
	if not overwrite and alreadyAnalyzed(out = out, datasetname = dataset,  algorithm=algorithm, evals= evalsString,franctiondataset = fractiondata, randomseed=random_seed):
		print("EARLY END: Config already analyzed {} {} {} {} {} ".format(out, dataset, algorithm, fractiondata, random_seed))
		#return None

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

	global defaultConfigurationKey

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

	totalBest = 0
	totalEquals = 0
	totalWorst = 0
	distances = []
	global currentI
	print("Default config {}".format(defaultConfigurationKey))
	total = 0
	for ind in df.index:
		try :
			print(df['diff'][ind], df[defaultConfigurationKey][ind])

			sizeDefaultConfig = (df[defaultConfigurationKey][ind])

			if math.isnan(sizeDefaultConfig):
				continue

			sizeDefaultConfig = int(sizeDefaultConfig)

			currentI = ind

			keyBestConfigFound_k = None

			if runTpe:
				spaceAlgorithms = createSpace(algorithm=algorithm)
				search_space = {"space": hp.choice('algorithm_type', spaceAlgorithms),
								}
				trials = Trials()
				best = fmin(
					fn=exterior,
					space=search_space,
					algo=tpe.suggest if runTpe else rand.suggest,
					max_evals=max_evals,
					trials=trials,
				)

				eval = hyperopt.space_eval(search_space, best)

				keyBestConfigFound_k = recreateConfigurationKey(eval)
			else:
				#print("Running Random total configs: {}".format(len(allConfig)))
				minEdlength = 10000000;
				bestConfigFound = None
				for iEval in range(0, max_evals):
					iRandom = random.randint(0, len(allConfig))
					try:
						selectedConfig = allConfig[iRandom]


						editScriptSelectedConfig = df[selectedConfig][ind]

						if math.isnan(editScriptSelectedConfig):
							continue

						editScriptSelectedConfig = int(editScriptSelectedConfig)

						# print("#eval {} random Selected config {} length {} ".format(iEval, selectedConfig,editScriptAvgSize))
						if editScriptSelectedConfig < minEdlength:
							minEdlength = editScriptSelectedConfig
							bestConfigFound = selectedConfig
					except:
						continue


				if bestConfigFound is not None:
					keyBestConfigFound_k = bestConfigFound

			##End search
			#print("{} Best config found: {}".format(total, keyBestConfigFound_k))
			fitnessBestConfig = (dfGlobal[keyBestConfigFound_k][currentI])


			if math.isnan(fitnessBestConfig):
				continue

			fitnessBestConfig = int(fitnessBestConfig)

			#print("best {} vs default  {} ".format(fitnessBestConfig, sizeDefaultConfig))

			if fitnessBestConfig < sizeDefaultConfig:
				totalBest+=1
			elif fitnessBestConfig == sizeDefaultConfig:
				totalEquals+=1
			else:
				totalWorst+=1
			total+=1
			if total % 1000 ==0:
				print("Partial results {}:".format(total))
				print("totalBest {} ".format(totalBest))
				print("totalEquals {} ".format(totalEquals))
				print("totalWorst {} ".format(totalWorst))
				#print("distances {}".format(distances))

			distances.append(fitnessBestConfig - sizeDefaultConfig)
		except Exception as e:
			print("Error ")
			print(e)

	elapsed_time = time.time() - inittime

	print("totalBest {} ".format(totalBest))
	print("totalEquals {} ".format(totalEquals))
	print("totalWorst {} ".format(totalWorst))
	#print("distances {}".format(distances))

	print("END total time after {} k {}".format(kFold, time.strftime("%H:%M:%S", time.gmtime(elapsed_time))))

	saveList(out, datasetname=dataset, algorithm=algorithm, data=[totalBest],
			 name="performanceBestOnTraining", fraction=fractiondata, randomseed=random_seed, evals=evalsString)

	saveList(out, datasetname=dataset, algorithm=algorithm, data=[totalEquals],
			 name="performanceEqualsOnTraining", fraction=fractiondata, randomseed=random_seed, evals=evalsString)

	saveList(out, datasetname=dataset, algorithm=algorithm, data=[totalWorst],
			 name="performanceWorstOnTraining", fraction=fractiondata, randomseed=random_seed, evals=evalsString)

	saveList(out, datasetname=dataset, algorithm=algorithm, data=[totalBest/total],
			 name="proportionBestOnTraining", fraction=fractiondata, randomseed=random_seed, evals=evalsString)

	saveList(out, datasetname=dataset, algorithm=algorithm, data=[totalEquals/total],
			 name="proportionEqualsOnTraining", fraction=fractiondata, randomseed=random_seed, evals=evalsString)

	saveList(out, datasetname=dataset, algorithm=algorithm, data=[totalWorst/total],
			 name="proportionWorstOnTraining", fraction=fractiondata, randomseed=random_seed, evals=evalsString)

	saveList(out, datasetname=dataset, algorithm=algorithm, data=distances,
			 name="distancias", fraction=fractiondata, randomseed=random_seed, evals=evalsString)

	return dfcomplete


def exterior(params ):
	global currentI
	global dfGlobal
	global defaultConfigurationKey
	#print("outside {} {} {} ".format(currentI ,defaultConfigurationKey, dfGlobal[defaultConfigurationKey][currentI]))
	keyConfig = recreateConfigurationKey(params)

	if math.isnan(currentI):
		print("Nan")
		return 100000

	fitness =  (dfGlobal[keyConfig][currentI])

	#print("i: {} Fitness for {} {} ".format(currentI, keyConfig, fitness))
	return fitness

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


import hyperopt
from hyperopt import tpe, hp, fmin, Trials
import pandas
import numpy as np
from src.commons.DiffAlgorithmMetadata import *
from src.processedDataConsumers.ResultGridSearchKfoldValidation import *
from src.commons.DiffAlgorithmMetadata import *

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

def computeHyperOpt(pathResults ="../../../../plots/data/distance_per_diff.csv", kFold=5, runTpe = True, max_evals=1000, random_seed = 0, fractiondata= 0.1,  dataset = "alldata", algorithm = "allalgo",  out = "../../plots/data/"):
	print("GT space size: {}".format(len(rangeGT_BUM_SMT) * len(rangeGT_BUM_SZT) * len(rangeMH)))
	print("SimpleGT space size: {}".format(len(rangeSBUP) * len(rangeMH)))
	print("CD space size: {}".format(len(rangeLSIM) * len(rangeML) * len(rangeSSIM1) * len((rangeSSIM2))))
	print("XY space size: {}".format(len(rangeXYSIM) * len(rangeMH)))

	df = pandas.read_csv(pathResults, sep=",")
	print("dataset before random {}".format(df.shape))

	df = df.sample(frac=fractiondata, random_state=random_seed).reset_index(drop=True)

	print("dataset after random {}".format(df.shape))

	columns = list(df.columns)
	# We get the name of the configurations
	allConfig = columns[1:]
	print(allConfig)

	indexOfConfig = {}

	# we start in 1 because the first is the diff
	for i in range(1, len(columns)):
		indexOfConfig[columns[i]] = i


	diffs = df['diff']

	allDiff = list(diffs.values)

	k_fold = KFold(kFold, random_state=0)

	performanceBestInTraining = []
	performanceBestInTesting = []
	bestConfigs = []

	# For each Fold
	for k, (train, test) in enumerate(k_fold.split(allDiff)):
		X_train = []
		X_test = []
		print("\n---------Running fold {}".format(k))

		# Create the training dataset
		for i in train:
			X_train.append(allDiff[i])

		# Create the testing dataset
		for i in test:
			X_test.append(allDiff[i])

		print("\nTraining {} ".format(k))

		## let's compute first the metrics for each configuration
		configsTraining, rankedBestTraining = findBestRanking(X_train, allConfig, df, indexOfColumns = indexOfConfig)

		configsTrainingMaps = {}
		for config in configsTraining:
			configsTrainingMaps[config['c']] = config

		print("Total Training configs {}".format(len(configsTrainingMaps.keys())))

		## Now the same for testing:

		configsTesting, rankedBestTesting = findBestRanking(X_test, allConfig, df, indexOfColumns=indexOfConfig)

		configsTestingMaps = {}
		for config in configsTesting:
			configsTestingMaps[config['c']] = config

		print("Total Testing configs {}".format(len(configsTrainingMaps.keys())))

		spaceAlgorithms = createSpace()
		search_space = { "space": hp.choice('algorithm_type', spaceAlgorithms),
		## A hack to pass the fitness of each configuration to the object function
		'data' :  configsTrainingMaps
		}
		trials = Trials()
		best = fmin(
			fn=objectiveFunctionDAT,
			space=search_space,
			algo=tpe.suggest if runTpe else hyperopt.random.suggest,
			max_evals=max_evals,
			trials=trials,

		)

		print("best {}".format(best))
		print(trials.trials)
		print(trials.results)
		print(trials.argmin)

		eval = hyperopt.space_eval(search_space, best)
		keyConfig = recreateConfigurationKey(eval)
		print("eval {} {}".format(len(eval),eval))
		bestConfigs.append(keyConfig)
		dataOfConfig = configsTrainingMaps[keyConfig]
		## this is a value between 0 (config not best in any diff) and 1 (config best in all diffs)
		bestPercentage = dataOfConfig['bs']
		print("Results k {} config {} best testing {} ".format(k, keyConfig, bestPercentage))

		performanceBestInTraining.append(bestPercentage)

		## the same but the testing:

		dataOfConfig = configsTestingMaps[keyConfig]
		## this is a value between 0 (config not best in any diff) and 1 (config best in all diffs)
		bestPercentage = dataOfConfig['bs']
		print("Results k {} config {} best training {} ".format(k, keyConfig, bestPercentage))

		performanceBestInTesting.append(bestPercentage)

	saveList(out = out,bestTraining = performanceBestInTraining , bestTesting = performanceBestInTesting,names = bestConfigs, datasetname = dataset,  algorithm=algorithm, franctiondataset = fractiondata)

def createSpace():
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
	return spaceAlgorithms


def saveList(out,bestTraining, bestTesting,names, datasetname,  algorithm, franctiondataset):

	filename = "{}/hyper_op_{}_{}_f_{}.csv".format(out,datasetname, algorithm, franctiondataset)
	fout1 = open(filename, 'w')
	for i in range(0, len(bestTraining)):
			fout1.write("{},{},{},{}\n".format(i,names[i], bestTraining[i],bestTesting[i]))
	fout1.flush()
	fout1.close()

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
	bestPercentage = dataOfConfig['bs']
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

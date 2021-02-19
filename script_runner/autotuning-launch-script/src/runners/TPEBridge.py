import sys
import hyperopt
from hyperopt import tpe, hp, fmin, Trials, rand
import pandas
import numpy as np
import os
import zipfile
import time
import random
import json
import requests as req
import html
propertiesPerMatcher = {}

defaultConfigurations = {
	#"SimpleGumtree":"SimpleGumtree_0.4_2", ##as the threshold does not count, we use 0.5, which we have computed it
"SimpleGumtree":"SimpleGumtree_0.5_2",
"ClassicGumtree":"ClassicGumtree_0.5_1000_1"	, ## "CompleteGumtreeMatcher_0.5_1000_2", GT2 uses H = 1 , GT 3 uses 2
"CompleteGumtreeMatcher": "CompleteGumtreeMatcher_0.5_1000_1",#"CompleteGumtreeMatcher_0.5_1000_2",
"ChangeDistiller": "ChangeDistiller_0.5_4_0.6_0.4",
"XyMatcher": "XyMatcher_2_0.5"
			}

sizeSearchSpace = {}
sizeSearchSpace["Gumtree"] = 2050
sizeSearchSpace["ChangeDistiller"] = 375
sizeSearchSpace["XyMatcher"] = 50





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

rangePriority= ["size", "height"]


def computeHyperOpt(runTpe = True, max_evals=100, cp = "", algorithm = None):

	##elapsed_time_setup = time.time() - start_time_setup

	print("Running DatTPE")
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
	keyBestConfigFound_k = recreateConfigurationKey(eval)
	print("Best config {}".format(keyBestConfigFound_k))

propertiesPerMatcher["SimpleGumtree"] = ["st_minprio", "st_priocalc"]
propertiesPerMatcher["ClassicGumtree"] = ["bu_minsim", "bu_minsize", "st_minprio", "st_priocalc"]
propertiesPerMatcher["CompleteGumtreeMatcher"] = ["bu_minsim", "bu_minsize", "st_minprio", "st_priocalc"]
propertiesPerMatcher["ChangeDistiller"] = ["cd_labsim", "cd_maxleaves","cd_structsim1",  "cd_structsim2"]
propertiesPerMatcher["XyMatcher"] = ["st_minprio", "st_priocalc"]


def createSpace(algorithm = None):
	spaceAlgorithms = [
		{  # ["GT_BUM_SMT_SBUP", "GT_STM_MH"]
			'algorithm': 'SimpleGumtree',
			"SimpleGumtree_st_minprio": hp.choice("SimpleGumtree_st_minprio", rangeMH),
			"SimpleGumtree_st_priocalc": hp.choice("SimpleGumtree_st_priocalc", rangePriority),
		},
		{  # ["GT_BUM_SMT", "GT_BUM_SZT", "GT_STM_MH"]
			'algorithm': 'ClassicGumtree',
			"ClassicGumtree_bu_minsim": hp.choice("ClassicGumtree_bu_minsim", rangeGT_BUM_SMT),
			"ClassicGumtree_bu_minsize": hp.choice("ClassicGumtree_bu_minsize", rangeGT_BUM_SZT),
			"ClassicGumtree_st_minprio": hp.choice("ClassicGumtree_st_minprio", rangeMH),
			"ClassicGumtree_st_priocalc": hp.choice("ClassicGumtree_st_priocalc", rangePriority),
		},
		{  # ["GT_BUM_SMT", "GT_BUM_SZT", "GT_STM_MH"]
			'algorithm': 'CompleteGumtreeMatcher',
			"CompleteGumtreeMatcher_bu_minsim": hp.choice("CompleteGumtreeMatcher_bu_minsim", rangeGT_BUM_SMT),
			"CompleteGumtreeMatcher_bu_minsize": hp.choice("CompleteGumtreeMatcher_bu_minsize", rangeGT_BUM_SZT),
			"CompleteGumtreeMatcher_st_minprio": hp.choice("CompleteGumtreeMatcher_st_minprio", rangeMH),
			"CompleteGumtreeMatcher_st_priocalc": hp.choice("CompleteGumtreeMatcher_st_priocalc", rangePriority),
		},
		{  # ["GT_CD_LSIM", "GT_CD_ML","GT_CD_SSIM1",  "GT_CD_SSIM2"]
			'algorithm': 'ChangeDistiller',
			"ChangeDistiller_cd_labsim": hp.choice("ChangeDistiller_cd_labsim", rangeLSIM),
			"ChangeDistiller_cd_maxleaves": hp.choice("ChangeDistiller_cd_maxleaves", rangeML),
			"ChangeDistiller_cd_structsim1": hp.choice("ChangeDistiller_cd_structsim1", rangeSSIM1),
			"ChangeDistiller_cd_structsim2": hp.choice("ChangeDistiller_cd_structsim2", rangeSSIM2),
		},
		# ["GT_STM_MH", "GT_XYM_SIM"]
		{
			'algorithm': 'XyMatcher',
			"XyMatcher_st_minprio": hp.choice("XyMatcher_st_minprio", rangeMH),
			"XyMatcher_st_priocalc": hp.choice("XyMatcher_st_priocalc", rangePriority),
		},
	]
	if algorithm is not None:
		spaceAlgorithms =  list(filter(lambda x: algorithm in x['algorithm'], spaceAlgorithms))

	return spaceAlgorithms


def objectiveFunctionDAT(params):

	print("param {}".format(params))

	print("-->{} {}".format(cp, javahome))
	keyConfig = recreateConfigurationKey(params)

	algo = params['space']['algorithm']

	keys = params['space'].keys()
	print(keys)
	separator = "-"
	parameters = algo
	for k in keys:
		if "algorithm" != k:
			parameters += "{}{}{}{}".format(separator , k.replace(algo+"_", "") , separator, params['space'][k])

	print("Receiving parameters: {} ".format(parameters))


	connectionString = "http://{}:{}/{}?action=run&parameters={}".format(host, port, path, parameters)
	print("Connection string: {}\n".format(connectionString))
	resp = req.get(connectionString)


	print("Response{}".format(resp.text))
	##status=ok, actions=1}

	responseJson = json.loads(html.unescape(str(resp.text)))

	## As fmin aims at minimizing, so shortest avg is the best

	if responseJson["status"] is 'ok':
		fitness = int(responseJson[actions])
		return fitness
	else:

		return 1000000


def recreateConfigurationKey(params):
	algo = params['space']['algorithm']
	key = [algo]
	for iParameter in propertiesPerMatcher[algo]:
		key.append(str(params['space']["{}_{}".format(algo, iParameter)]))
	keyConfig = ("_".join(key)).replace("_1.0", "_1")
	return keyConfig

###
print("Hello from TPE Python {} {}".format( sys.argv[1],  sys.argv[2]))
global cp
global javahome
global host
global port
global path


cp = sys.argv[1]
javahome = sys.argv[2]
host = sys.argv[3]
port = sys.argv[4]
path= sys.argv[5]

print("Running TPEBridge: {} {} {} {} {} ".format(host, port, path, cp, javahome))
computeHyperOpt()
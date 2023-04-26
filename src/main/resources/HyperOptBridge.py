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
import statistics
import numpy
propertiesPerMatcher = {}

sizeSearchSpace = {}
sizeSearchSpace["Gumtree"] = 2050
sizeSearchSpace["ChangeDistiller"] = 375
sizeSearchSpace["XyMatcher"] = 50

AVG_CONSTANT = 'av'

rangeST_MIN_PRIO = [round(x, 2) for x in np.arange(1, 6, 1)]
rangeGT_BUM_SMT = [ round(x,2) for x in np.arange(0.1,1.1,0.1)]
rangeGT_BUM_SZT = [ x for x in range(100,2001,100)]
rangePriority= ["size", "height"]

#rangeSBUP = [ round(x,2) for x in np.arange(0.1,1.1,0.2)]
#rangeLSIM= [ round(x,2) for x in np.arange(0.1,1.1,0.2)]
#rangeML = [ x for x in range(2,7,2)]
#rangeSSIM1= [ round(x,2) for x in np.arange(0.2,1.1,0.2)]
#rangeSSIM2= [ round(x,2) for x in np.arange(0.2,1.1,0.2)]

#rangeXYSIM= [ round(x,2) for x in np.arange(0.1,1.1,0.1)]




def computeHyperOpt(algoToRun, max_evals=100, cp = "", algorithm = None, xseed = 0):

	##elapsed_time_setup = time.time() - start_time_setup
	rstate = np.random.default_rng(xseed) #numpy.random.RandomState(seed=xseed)
	print("Running Hyperopt algo to rub {} max eval {} seed {}".format(algoToRun.__name__, max_evals, xseed))
	spaceAlgorithms = createSpace(algorithm=algorithm)
	search_space = { "space": hp.choice('algorithm_type', spaceAlgorithms),
					 ## A hack to pass the fitness of each configuration to the object function
					 #'data' :  (X_train, train)
					 }
	trials = Trials()



	best = fmin(
		fn=objectiveFunctionDAT,
		space=search_space,
		algo= algoToRun,  #tpe.suggest if runTpe else rand.suggest,
		max_evals=max_evals,
		trials=trials,
		rstate=rstate
	)

	eval = hyperopt.space_eval(search_space, best)
	keyBestConfigFound_k =  recreateParametersString(eval)
	print("{}{}".format(header_res, keyBestConfigFound_k))


propertiesPerMatcher["SimpleGumtree"] = ["st_minprio", "st_priocalc"]
propertiesPerMatcher["ClassicGumtree"] = ["bu_minsim", "bu_minsize", "st_minprio", "st_priocalc"]
propertiesPerMatcher["HybridGumtree"] = ["bu_minsize", "st_minprio", "st_priocalc"]


def createSpace(algorithm = None):
	spaceAlgorithms = [
		{ 
			'algorithm': 'SimpleGumtree',
			"SimpleGumtree_st_minprio": hp.choice("SimpleGumtree_st_minprio", rangeST_MIN_PRIO),
			"SimpleGumtree_st_priocalc": hp.choice("SimpleGumtree_st_priocalc", rangePriority),
		},
		{  
			'algorithm': 'ClassicGumtree',
			"ClassicGumtree_bu_minsim": hp.choice("ClassicGumtree_bu_minsim", rangeGT_BUM_SMT),
			"ClassicGumtree_bu_minsize": hp.choice("ClassicGumtree_bu_minsize", rangeGT_BUM_SZT),
			"ClassicGumtree_st_minprio": hp.choice("ClassicGumtree_st_minprio", rangeST_MIN_PRIO),
			"ClassicGumtree_st_priocalc": hp.choice("ClassicGumtree_st_priocalc", rangePriority),
		},
		{ 
			'algorithm': 'HybridGumtree',
			"HybridGumtree_bu_minsize": hp.choice("HybridGumtree_bu_minsize", rangeGT_BUM_SZT),
			"HybridGumtree_st_minprio": hp.choice("HybridGumtree_st_minprio", rangeST_MIN_PRIO),
			"HybridGumtree_st_priocalc": hp.choice("HybridGumtree_st_priocalc", rangePriority),
		},
	]
	if algorithm is not None:
		spaceAlgorithms =  list(filter(lambda x: algorithm in x['algorithm'], spaceAlgorithms))

	return spaceAlgorithms


def objectiveFunctionDAT(params):

	print("param {}".format(params))

	parameters = recreateParametersString(params)

	print("Receiving parameters: {} ".format(parameters))


	connectionString = "http://{}:{}/{}?action=run&parameters={}".format(host, port, path, parameters)
	print("Connection string: {}\n".format(connectionString))
	resp = req.get(connectionString)


	print("Response{}".format(resp.text))

	responseJson = json.loads(html.unescape(str(resp.text)))

	## As fmin aims at minimizing, so shortest avg is the best

	if responseJson["status"] == 'ok':
		return responseJson["fitness"]
	else:
		return 10000000


def recreateParametersString(params):
	algo = params['space']['algorithm']
	separator = "-"
	parameters = algo
	keys = params['space'].keys()
	for k in keys:
		if "algorithm" != k:
			parameters += "{}{}{}{}".format(separator, k.replace(algo + "_", ""), separator, params['space'][k])
	return parameters


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
global header_res


cp = sys.argv[1]
javahome = sys.argv[2]
host = sys.argv[3]
port = sys.argv[4]
path= sys.argv[5]
header_res = sys.argv[6]
tpeparam = sys.argv[7].lower()
numberEval = sys.argv[8]
seed = sys.argv[9]

#print("Famework_Setup: Hyperopt TPEBridge: host {} port {} path {} cp {} javahome {} header {} tpe? {} nr eval {} seed{}".format(host, port, path, cp, javahome, header_res, tpeparam, numberEval,seed))


algoToRun = None
classToRun = None
if tpeparam.lower() == "TPE_HYPEROPT".lower():
		algoToRun = tpe.suggest
		classToRun = tpe
elif tpeparam.lower() == "RANDOM_HYPEROPT".lower():
		algoToRun = rand.suggest
		classToRun = rand
elif tpeparam.lower() == "ADAPTIVE".lower():
		algoToRun = hyperopt.atpe.suggest
		classToRun =  hyperopt.atpe
elif tpeparam.lower() == "Annealing".lower():
		algoToRun = hyperopt.anneal.suggest
		classToRun = hyperopt.anneal
else:
	print("Unknown {}".format(tpeparam))
	exit()
print("Famework_Setup: Hyperopt TPEBridge: host {} port {} cp {} javahome {} header {} tpe {} algo_to_run {}/{} nr eval {} seed{}".format(host, port, path, javahome, header_res, tpeparam, classToRun.__name__,  algoToRun.__name__,  numberEval,seed))

#print("running {} {}".format( tpeparam,  algoToRun.__name__))
computeHyperOpt(algoToRun, max_evals=int(numberEval), xseed=int(seed))
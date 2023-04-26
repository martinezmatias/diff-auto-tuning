import optuna
import sys
import os
import json
import requests as req
import html

propertiesPerMatcher = {}
propertiesPerMatcher["SimpleGumtree"] = ["st_minprio", "st_priocalc"]
propertiesPerMatcher["ClassicGumtree"] = ["bu_minsim", "bu_minsize", "st_minprio", "st_priocalc"]
propertiesPerMatcher["HybridGumtree"] = ["bu_minsize", "st_minprio", "st_priocalc"]

def objective(trial):
	params = {}
	valueAlgorithm = trial.suggest_categorical("algorithm", ["SimpleGumtree", "ClassicGumtree", "HybridGumtree"])
	value_st_priocalc = trial.suggest_categorical("st_priocalc", ["size", "height"])
	value_st_minprio = trial.suggest_int("st_minprio", 1, 5, step=1)
	value_bu_minsim =  trial.suggest_float("bu_minsim",0.1, 1.0, step=0.1)
	value_bu_minsize = trial.suggest_int("bu_minsize", 100, 2000, step=100)

	params["algorithm"] = valueAlgorithm
	if valueAlgorithm == "SimpleGumtree":
		params["st_minprio"] = value_st_minprio
		params["st_priocalc"] = value_st_priocalc
	elif valueAlgorithm == "ClassicGumtree":
		params["st_minprio"] = value_st_minprio
		params["st_priocalc"] = value_st_priocalc
		params["bu_minsim"] = value_bu_minsim
		params["bu_minsize"] = value_bu_minsize
	elif valueAlgorithm == "HybridGumtree":
		params["st_minprio"] = value_st_minprio
		params["st_priocalc"] = value_st_priocalc
		params["bu_minsize"] = value_bu_minsize
	else:
		print("Error: 'algorithm' known ", valueAlgorithm)

	#print("params {}".format(params), recreateConfigurationKey(params), recreateParametersString(params))
	#x = trial.suggest_float('x', -10, 10)
	#print("Calling obj", str(x))

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
	print(params)
	algo = params['algorithm']
	separator = "-"
	parameters = algo
	keys = params.keys()
	for k in keys:
		if "algorithm" != k:
			parameters += "{}{}{}{}".format(separator, k, separator, params[k])
	print("out", parameters)
	return parameters

def recreateConfigurationKey(params):
	algo = params['algorithm']
	key = [algo]
	for iParameter in propertiesPerMatcher[algo]:
		key.append(str(params["{}".format(iParameter)]))
	keyConfig = ("_".join(key)).replace("_1.0", "_1")
	return keyConfig



######################### Main

####

cp = sys.argv[1]
javahome = sys.argv[2]
host = sys.argv[3]
port = sys.argv[4]
path= sys.argv[5]
header_res = sys.argv[6]
tpeparam = sys.argv[7].lower()
numberEval = int(sys.argv[8])
seed = sys.argv[9]
print("Running TPEBridge: host {} port {} path {} cp {} javahome {} header {} tpe? {} nr eval {} seed{}".format(host, port, path, cp, javahome, header_res, tpeparam, numberEval,seed))


#### Select the Sampler
if tpeparam == "Grid".lower():
	study = optuna.create_study(sampler=optuna.samplers.GridSampler({}))

elif tpeparam == "Random_OPTUNA".lower():
	study = optuna.create_study(sampler=optuna.samplers.RandomSampler())

elif tpeparam == "TPE_OPTUNA".lower():
	study = optuna.create_study(sampler=optuna.samplers.TPESampler())

elif tpeparam == "CmaEs".lower():
	study = optuna.create_study(sampler=optuna.samplers.CmaEsSampler())

elif tpeparam == "PartialFixed".lower():
	study = optuna.create_study(sampler=optuna.samplers.PartialFixedSampler({}))

elif tpeparam == "NSGAII".lower():
	study = optuna.create_study(sampler=optuna.samplers.NSGAIISampler())

elif tpeparam == "QMC".lower():
	study = optuna.create_study(sampler=optuna.samplers.QMCSampler())
else:
	print("Error : not defined {}".format(tpeparam))


print(f"Sampler is {study.sampler.__class__.__name__}")

print("Famework_Setup Optuna: mode {} method {} ".format(study.sampler.__class__.__name__ , tpeparam) )
study.optimize(objective, n_trials=numberEval, n_jobs=1)

bestConfig = study.best_params  # E.g. {'x': 2.002108042}
keyBestConfigFound_k =  recreateParametersString(bestConfig)
print(bestConfig)
#keyBestConfigFound_k =  recreateParametersString(eval)
print("{}{}".format(header_res, keyBestConfigFound_k))
print("Best Trial: {}".format(study.best_value))
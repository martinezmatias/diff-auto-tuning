
import sys
from src.processedDataConsumers.EngineHyperOptDAT import *
from src.commons.Datalocation import *
from src.processedDataConsumers.CostParameters import *
import traceback


def runTPERangeValues(editscriptsizepath,datasetname, runTPE, algo, seed):
	#The first
	datasetDF = None

	evals_To_execute = []
	print("percentage evals? ".format(USE_PERCENTAGE_EVAL))
	if USE_PERCENTAGE_EVAL:

		sizeSpaceAlgo = sizeSearchSpace[algo]

		for per in PERCENTAGE_EVALS_RANGE:
			# round
			totalEval = int(per * sizeSpaceAlgo)
			# if is zero, we put 1
			totalEval = 1 if totalEval is 0 else totalEval

			if totalEval not in evals_To_execute:
				evals_To_execute.append(totalEval)

	else:
		evals_To_execute = ABSOLUTE_EVALS_RANGE

	print("Evals to execute {} for {}".format(evals_To_execute, algo))

	for i_eval in evals_To_execute:
		kfold = KFOLD_VALUE
		for i_ratio in RATIO_DATASET:
			try:
				datasetDF = computeHyperOpt(pathResults=editscriptsizepath, dfcomplete=datasetDF, kFold=kfold, max_evals=i_eval , algorithm=algo,fractiondata= i_ratio,  dataset = datasetname, runTpe= (runTPE.lower() == "true"), out=RESULTS_PROCESSED_LOCATION, random_seed=seed )
			except:
				print("Error executing Hyperopt")
				print(traceback.format_exc())

editscriptsizepath = sys.argv[1]

datasetname = sys.argv[2]

runTPE = str(sys.argv[3])

algoSelected = str(sys.argv[4])

seed = int(sys.argv[5])

runTPERangeValues(editscriptsizepath,datasetname, runTPE, algoSelected, seed)
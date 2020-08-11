
import sys
from src.processedDataConsumers.EngineHyperOptDAT import *
from src.commons.Datalocation import *


def runTPERangeValues(distancespath,datasetname, runTPE):
	evals_range = [10, 20, 50, 100, 200, 500, 1000]
	ratio = [0.0001, 0.00025, 0.0005, 0.00075, 0.001, 0.0025, 0.005, 0.0075, 0.01, 0.025, 0.05,0.075, 0.1, 0.25, 0.5, 0.75,  1]

	for i_eval in evals_range:
		kfold = 10
		for i_ratio in ratio:

			computeHyperOpt(pathResults=distancespath, kFold=kfold, max_evals=i_eval ,fractiondata= i_ratio,  dataset = datasetname, runTpe= (runTPE.lower() == "true"), out=RESULTS_PROCESSED_LOCATION )


distancespath = sys.argv[1]

datasetname = sys.argv[2]

runTPE = str(sys.argv[3])

runTPERangeValues(distancespath,datasetname, runTPE)
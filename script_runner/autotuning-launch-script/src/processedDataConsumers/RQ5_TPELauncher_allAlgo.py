
import sys
from src.processedDataConsumers.EngineHyperOptDAT import *
from src.commons.Datalocation import *
from src.processedDataConsumers.CostParameters import *


def runTPERangeValues(distancespath,datasetname, runTPE):

	for i_eval in evals_range:
		kfold = 10
		for i_ratio in ratioDataset:

			computeHyperOpt(pathResults=distancespath, kFold=kfold, max_evals=i_eval ,fractiondata= i_ratio,  dataset = datasetname, runTpe= (runTPE.lower() == "true"), out=RESULTS_PROCESSED_LOCATION )


distancespath = sys.argv[1]

datasetname = sys.argv[2]

runTPE = str(sys.argv[3])

runTPERangeValues(distancespath,datasetname, runTPE)
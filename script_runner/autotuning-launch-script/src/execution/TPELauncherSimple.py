
import sys
from src.processedDataConsumers.EngineHyperOptDAT import *
from src.commons.Datalocation import *

distancespath = sys.argv[1]

datasetname = sys.argv[2]

runTPE = str(sys.argv[3])

evals_range = [10, 20, 50, 100, 200, 500]
ratio = [0.01, 0.025, 0.05, 0.1, 0.2, 0.5]

for i_eval in evals_range:
	kfold = 10
	for i_ratio in ratio:

		computeHyperOpt(pathResults=distancespath, kFold=kfold, max_evals=i_eval ,fractiondata= i_ratio,  dataset = datasetname, runTpe= (runTPE.lower() == "true"), out=RESULTS_PROCESSED_LOCATION )

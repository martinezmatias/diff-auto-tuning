
import sys
from src.processedDataConsumers.EngineHyperOptDAT import *
from src.commons.Datalocation import *
from src.processedDataConsumers.CostParameters import *

def runTPERangeValues(distancespath,datasetname, runTPE, algo):

	for i_eval in evals_range:
		kfold = 10
		for i_ratio in ratioDataset:
			try:
				computeHyperOpt(pathResults=distancespath, kFold=kfold, max_evals=i_eval , algorithm=algo,fractiondata= i_ratio,  dataset = datasetname, runTpe= (runTPE.lower() == "true"), out=RESULTS_PROCESSED_LOCATION )
			except:
				print("Error")

distancespath = sys.argv[1]

datasetname = sys.argv[2]

runTPE = str(sys.argv[3])

algoSelected = str(sys.argv[4])

runTPERangeValues(distancespath,datasetname, runTPE, algoSelected)
from src.commons.Datalocation import RESULTS_PROCESSED_LOCATION
from src.processedDataConsumers.EngineGridSearchKfoldValidation import computeGridSearchKFold
import os, sys
from src.processedDataConsumers.CostParameters import *
from src.commons.DiffAlgorithmMetadata import *
def main(distancespath,algorithm,kfold,datasetname, seed):

	datasetComplete = None
	for i_fraction in ratioDataset:

		keyDefault = None
		if algorithm is None or "Gumtree" in algorithm:
			keyDefault = "ClassicGumtree"
		else:
			keyDefault = algorithm

		datasetComplete = computeGridSearchKFold(pathResults=distancespath, dfcomplete=datasetComplete, kFold=kfold,
							   algorithm=None if algorithm.lower() == "none" else algorithm,
							   defaultId=defaultConfigurations[keyDefault], fration=i_fraction, datasetname=datasetname, random_seed=seed)

distancespath = sys.argv[1]

algorithm = str(sys.argv[2])

kfold = int(sys.argv[3])

datasetname = sys.argv[4]

seed = int(sys.argv[5])

main(distancespath, algorithm, kfold, datasetname, seed )
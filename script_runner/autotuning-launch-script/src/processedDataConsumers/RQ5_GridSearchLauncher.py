from src.commons.Datalocation import RESULTS_PROCESSED_LOCATION
from src.processedDataConsumers.EngineGridSearchKfoldValidation import computeGridSearchKFold
import os, sys
from src.processedDataConsumers.CostParameters import *

def main(distancespath,algorithm,kfold,datasetname):


	for i_fraction in ratioDataset:

		computeGridSearchKFold(pathResults=distancespath, kFold=kfold,
							   algorithm=None if algorithm.lower() == "none" else algorithm,
							   defaultId="ClassicGumtree_0.5_1000_1", fration=i_fraction, datasetname=datasetname)

distancespath = sys.argv[1]

algorithm = str(sys.argv[2])

kfold = int(sys.argv[3])

datasetname = sys.argv[4]


main(distancespath, algorithm, kfold, datasetname )
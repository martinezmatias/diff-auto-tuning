from src.commons.Datalocation import RESULTS_PROCESSED_LOCATION
from src.processedDataConsumers.EngineGridSearchKfoldValidation import computeGridSearchKFold
import os, sys


def main(distancespath,algorithm,kfold,datasetname,fraction):
	computeGridSearchKFold(pathResults=distancespath, kFold=kfold,
						   algorithm=None if algorithm.lower() == "none" else None,
						   defaultId="ClassicGumtree_0.5_1000_2", fration=fraction, datasetname=datasetname)

distancespath = sys.argv[1]

algorithm = str(sys.argv[2])

kfold = int(sys.argv[3])

datasetname = sys.argv[4]

fraction = float(sys.argv[5])

main(distancespath, algorithm, kfold, datasetname, fraction )
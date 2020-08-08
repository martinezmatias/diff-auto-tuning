from src.commons.Datalocation import RESULTS_PROCESSED_LOCATION
from src.processedDataConsumers.EngineGridSearchKfoldValidation import computeGridSearchKFold
import os, sys


def main(distancespath,algorithm,kfold,datasetname):
	for i_fraction in [0.0001, 0.00025, 0.0005, 0.00075, 0.001, 0.0025, 0.005, 0.0075, 0.01, 0.025, 0.05,0.075, 0.1, 0.25, 0.5, 0.75,  1]:

		computeGridSearchKFold(pathResults=distancespath, kFold=kfold,
							   algorithm=None if algorithm.lower() == "none" else None,
							   defaultId="ClassicGumtree_0.5_1000_2", fration=i_fraction, datasetname=datasetname)

distancespath = sys.argv[1]

algorithm = str(sys.argv[2])

kfold = int(sys.argv[3])

datasetname = sys.argv[4]


main(distancespath, algorithm, kfold, datasetname )
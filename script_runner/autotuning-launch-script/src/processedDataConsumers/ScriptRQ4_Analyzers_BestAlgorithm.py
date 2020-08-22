import unittest
from src.commons.Datalocation import *
from src.rowDataConsumers.RQ0_Setup_ComputeFitnessDistanceOfConfiguationsFromRowData import *
from src.rowDataConsumers.RQ0_Setup_ComputeEDSizeOfConfiguationsFromRowData import *
from src.processedDataConsumers.EngineGridSearchKfoldValidation import *
from src.commons.DiffAlgorithmMetadata import *
class TestRQ4(unittest.TestCase):

	'''This computes the fitness matrix (distance.csv)'''
	def _test_ComputeFitnessFast(self):
		folderToAnalyze = NAME_FOLDER_ASTJDT
		computeFitness("{}/{}/".format(RESULTS_ROW_LOCATION,folderToAnalyze), suffix=folderToAnalyze)

	def test_ComputeEditScriptFast(self):

		for folderToAnalyze in [NAME_FOLDER_ASTJDT, NAME_FOLDER_ASTSPOON]:
			computeEditScriptSize("{}/{}/".format(RESULTS_ROW_LOCATION,folderToAnalyze), suffix=folderToAnalyze)

	'''This script is similar to RQ 1, but the call it once, with a single distance matrix (fitness) considering all algorithms.'''
	def _test_ComputeBestKFoldComplete(self):
		for folderToAnalyze in [NAME_FOLDER_ASTJDT,
								NAME_FOLDER_ASTSPOON
								]:
			print("\nAnalyzing {}".format(folderToAnalyze))
			kvalue = 10
			random_seed_value = 0

			computeGridSearchKFold("{}/distance_per_diff_{}.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze), kFold=kvalue, algorithm=None, defaultId=defaultConfigurations["ClassicGumtree"], random_seed=random_seed_value, datasetname=folderToAnalyze)
			computeGridSearchKFold("{}/distance_per_diff_{}.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze), kFold=kvalue, algorithm=None, defaultId=defaultConfigurations["ClassicGumtree"], random_seed=random_seed_value, datasetname=folderToAnalyze)


	'''This script is similar to RQ 1, but the call it once, with a distance matrix (fitness) considering all algorithms.'''
	def _test_ComparisonBest_TableRQ1(self):

		for astModel in [NAME_FOLDER_ASTJDT, NAME_FOLDER_ASTSPOON]:
			for algorithm in ["Gumtree", "ChangeDistiller", "Xy"]:

				computeBestAndDefaultByFoldFiles(astModel, "algorithm",
												 "{}/summary_{}_{}_performanceTestingBestOnTraining.csv".format(RESULTS_PROCESSED_LOCATION,astModel, algorithm)
												 , "{}/summary_{}_{}_performanceTestingDefaultOnTraining.csv".format(RESULTS_PROCESSED_LOCATION, astModel, algorithm))



if __name__ == '__main__':
	unittest.main()

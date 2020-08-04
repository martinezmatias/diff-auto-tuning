import unittest
from src.commons.Datalocation import *
from src.rowDataConsumers.RQ0_Setup_ComputeFitnessOfConfiguationsFromRowData import *
from src.processedDataConsumers.EngineGridSearchKfoldValidation import *
class TestRQ4(unittest.TestCase):

	'''This computes the fitness matrix (distance.csv)'''
	def _test_ComputeFitnessFast(self):
		folderToAnalyze = NAME_FOLDER_ASTJDT
		computeFitness("{}/{}/".format(RESULTS_ROW_LOCATION,folderToAnalyze), suffix=folderToAnalyze)

	'''This script is similar to RQ 1, but the call it once, with a single distance matrix (fitness) considering all algorithms.'''
	def test_ComputeBestKFoldComplete(self):
		for folderToAnalyze in [NAME_FOLDER_ASTJDT,
								#NAME_FOLDER_ASTSPOON
								]:
			print("\nAnalyzing {}".format(folderToAnalyze))
			kvalue = 2
			random_seed_value = 0
			allOptimized = []
			allDefault = []
			optimizedgt, defaultgt,  rp_index,srho_index,pmann_index, pwilcoxon_index, rp_performance,srho_performance,pmann_performance,pwilcoxon_performance = \
				computeGridSearchKFold("{}/distance_per_diff_{}.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze), kFold=kvalue, algorithm=None, defaultId="ClassicGumtree_0.5_1000_2", random_seed=random_seed_value, datasetname=folderToAnalyze)
			allOptimized.append(optimizedgt)
			allDefault.append(defaultgt)

	'''This script is similar to RQ 1, but the call it once, with a distance matrix (fitness) considering all algorithms.'''
	def _test_ComparisonBest_TableRQ1(self):

		for folderToAnalyze in [NAME_FOLDER_ASTJDT, NAME_FOLDER_ASTSPOON]:
			for algorithm in ["Gumtree", "ChangeDistiller", "Xy"]:

				computeBestAndDefaultByFoldFiles("GumTree",
												 "{}/summary_{}_{}_performanceTestingBestOnTraining.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze, algorithm)
												 , "{}/summary_{}_{}_performanceTestingDefaultOnTraining.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze, algorithm))



if __name__ == '__main__':
	unittest.main()

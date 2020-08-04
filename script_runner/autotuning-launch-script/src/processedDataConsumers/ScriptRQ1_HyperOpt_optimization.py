import unittest
from src.processedDataConsumers.EngineHyperOptDAT import *
from src.commons.Datalocation import *
class TestHyperOp(unittest.TestCase):


	def _test_CompteHyperOpt_single_by_algo(self):
			''''only 1000 '''''
			kfold = 10
			for folderToAnalyze in [NAME_FOLDER_ASTSPOON, NAME_FOLDER_ASTJDT]:
				for algorithm in ["Gumtree", "ChangeDistiller",
								  "XyMatcher"]:
					print("\nanalyzing {}".format(folderToAnalyze))
					computeHyperOpt("{}/distance_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze, algorithm), kFold=kfold, max_evals=1000,fractiondata= 1,  dataset = folderToAnalyze, algorithm = algorithm)

	def _test_CompteHyperOpt_single(self):
			''''only 1000 '''''
			kfold = 10
			for folderToAnalyze in [NAME_FOLDER_ASTJDT, NAME_FOLDER_ASTSPOON]:

					print("\nanalyzing {}".format(folderToAnalyze))
					computeHyperOpt("{}/distance_per_diff_{}.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze), kFold=kfold, max_evals=1000,fractiondata= 1,  dataset = folderToAnalyze, algorithm = None)



	def testAnalyzeHyperopResults(self):
		for algo in ["Gumtree", "ChangeDistiller", "XyMatcher"]:
			dataset = NAME_FOLDER_ASTJDT
			path = "{}/hyper_op_{}_4_{}_evals_100_f_0.5.csv".format(RESULTS_ROW_LOCATION, dataset, algo)
			analyzeResultsHyperop(path, algo)

if __name__ == '__main__':
	unittest.main()

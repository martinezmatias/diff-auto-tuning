import unittest
from src.processedDataConsumers.EngineHyperOptDAT import *
from src.commons.Datalocation import *
from src.rowDataConsumers.RQ0_Setup_ComputeEDSizeOfConfiguationsFromRowData import *
from src.processedDataConsumers.CostParameters import *
class TestHyperOp(unittest.TestCase):

	def _test_A_ComputeDistanceFastPerAlgorithm(self):

		for folderToAnalyze in [NAME_FOLDER_ASTJDT,  NAME_FOLDER_ASTSPOON]:
			for algorithm in ["Gumtree", "ChangeDistiller", "XyMatcher"]:
				computeEditScriptSize("{}/{}/".format(RESULTS_ROW_LOCATION, folderToAnalyze), suffix="{}_{}".format(folderToAnalyze, algorithm), key = algorithm)


	def _test_CompteHyperOpt_single_by_algo(self):
			''''only 1000 '''''
			kfold = 2
			maxeval = 10
			franction = 0.001
			seed=20
			for folderToAnalyze in [NAME_FOLDER_ASTSPOON, NAME_FOLDER_ASTJDT]:
				for algorithm in ["Gumtree", "ChangeDistiller",
								  "XyMatcher"]:
					print("\nanalyzing {}".format(folderToAnalyze))
					computeHyperOpt(pathResults="{}/editscript_size_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze, algorithm), kFold=kfold, max_evals=maxeval,fractiondata= franction,  dataset = folderToAnalyze, algorithm = algorithm, out=RESULTS_PROCESSED_LOCATION, random_seed=seed )

	def test_debug_CompteHyperOpt_single_by_algo(self):
			kfold = 2
			maxeval = 10
			franction = 0.1
			seed=20
			TPE = False
			for folderToAnalyze in [NAME_FOLDER_ASTJDT]:
				for algorithm in ["Gumtree", "ChangeDistiller",
								  "XyMatcher"]:
					print("\nanalyzing {}".format(folderToAnalyze))
					computeHyperOpt(pathResults="{}/editscript_size_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze, algorithm),overwrite=True, runTpe=TPE, kFold=kfold, max_evals=maxeval,fractiondata= franction,  dataset = folderToAnalyze, algorithm = algorithm, out=RESULTS_PROCESSED_LOCATION, random_seed=seed )
					break

	def _test_CompteHyperOpt_allAlgos(self):
			''''only 1000 '''''
			kfold = 10
			for folderToAnalyze in [NAME_FOLDER_ASTJDT, NAME_FOLDER_ASTSPOON]:

					print("\nanalyzing {}".format(folderToAnalyze))
					computeHyperOpt(pathResults="{}/editscript_size_per_diff_{}.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze), kFold=kfold, max_evals=1000,fractiondata= 1,  dataset = folderToAnalyze, algorithm = None, out=RESULTS_PROCESSED_LOCATION )



	def _testAnalyzeHyperopResults(self):
		for algo in ["Gumtree", "ChangeDistiller", "XyMatcher"]:
			dataset = NAME_FOLDER_ASTJDT
			path = "{}/hyper_op_{}_4_{}_evals_100_f_0.5.csv".format(RESULTS_ROW_LOCATION, dataset, algo)
			analyzeResultsHyperop(path, algo)

if __name__ == '__main__':
	unittest.main()

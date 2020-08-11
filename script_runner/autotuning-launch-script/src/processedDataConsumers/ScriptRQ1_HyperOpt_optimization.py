import unittest
from src.processedDataConsumers.EngineHyperOptDAT import *
from src.commons.Datalocation import *
from src.rowDataConsumers.RQ0_Setup_ComputeEDSizeOfConfiguationsFromRowData import *
class TestHyperOp(unittest.TestCase):

	def _test_A_ComputeDistanceFastPerAlgorithm(self):

		for folderToAnalyze in [NAME_FOLDER_ASTJDT,  NAME_FOLDER_ASTSPOON]:
			for algorithm in ["Gumtree", "ChangeDistiller", "XyMatcher"]:
				computeEditScriptSize("{}/{}/".format(RESULTS_ROW_LOCATION, folderToAnalyze), suffix="{}_{}".format(folderToAnalyze, algorithm), key = algorithm)


	def _test_CompteHyperOpt_single_by_algo(self):
			''''only 1000 '''''
			kfold = 10
			for folderToAnalyze in [NAME_FOLDER_ASTSPOON, NAME_FOLDER_ASTJDT]:
				for algorithm in ["Gumtree", "ChangeDistiller",
								  "XyMatcher"]:
					print("\nanalyzing {}".format(folderToAnalyze))
					computeHyperOpt(overwriteResult=True, pathResults="{}/distance_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze, algorithm), kFold=kfold, max_evals=1000,fractiondata= 1,  dataset = folderToAnalyze, algorithm = algorithm, out=RESULTS_PROCESSED_LOCATION )

	def test_CompteHyperOpt_single(self):
			''''only 1000 '''''
			kfold = 10
			for folderToAnalyze in [NAME_FOLDER_ASTJDT, NAME_FOLDER_ASTSPOON]:

					print("\nanalyzing {}".format(folderToAnalyze))
					computeHyperOpt(overwriteResult=True,pathResults="{}/editscript_size_per_diff_{}.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze), kFold=kfold, max_evals=1000,fractiondata= 1,  dataset = folderToAnalyze, algorithm = None, out=RESULTS_PROCESSED_LOCATION )



	def _testAnalyzeHyperopResults(self):
		for algo in ["Gumtree", "ChangeDistiller", "XyMatcher"]:
			dataset = NAME_FOLDER_ASTJDT
			path = "{}/hyper_op_{}_4_{}_evals_100_f_0.5.csv".format(RESULTS_ROW_LOCATION, dataset, algo)
			analyzeResultsHyperop(path, algo)

if __name__ == '__main__':
	unittest.main()

import unittest
from src.processedDataConsumers.EngineHyperOptDAT import *

class TestHyperOp(unittest.TestCase):


	def _test_CompteHyperOpt_single_by_algo(self):
			''''only 1000 '''''
			kfold = 10
			for folderToAnalyze in ["merge_gtJDT_5_CDJDT_4",
									"merge_gt6_cd_5"]:
				for algorithm in ["Gumtree", "ChangeDistiller",
								  "XyMatcher"]:
					print("\nanalyzing {}".format(folderToAnalyze))
					computeHyperOpt("../../plots/data/distance_per_diff_{}_{}.csv".format(folderToAnalyze, algorithm), kFold=kfold, max_evals=1000,fractiondata= 1,  dataset = folderToAnalyze, algorithm = algorithm)

	def _test_CompteHyperOpt_single(self):
			''''only 1000 '''''
			kfold = 10
			for folderToAnalyze in ["merge_gtJDT_5_CDJDT_4",
									"merge_gt6_cd_5"]:

					print("\nanalyzing {}".format(folderToAnalyze))
					computeHyperOpt("../../plots/data/distance_per_diff_{}.csv".format(folderToAnalyze), kFold=kfold, max_evals=1000,fractiondata= 1,  dataset = folderToAnalyze, algorithm = None)



	def testAnalyzeHyperopResults(self):
		for algo in ["Gumtree", "ChangeDistiller", "XyMatcher"]:
			path = "/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/hyper_op_merge_gtJDT_5_CDJDT_4_{}_evals_100_f_0.5.csv".format(
				algo)
			analyzeHyperop(path, algo)

if __name__ == '__main__':
	unittest.main()

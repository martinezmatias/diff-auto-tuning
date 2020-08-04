import unittest
import unittest

from src.processedDataConsumers.EngineGridSearchKfoldValidation import *
from src.processedDataConsumers.plotPerformance import *
from src.rowDataConsumers.ResultsAnalyzeDiffConfiguration import *

class MyTestCase(unittest.TestCase):
	def test_something(self):
		self.assertEqual(True, False)


	def _test_ComputeFitnessFastPerAlgorithm(self):

		for folderToAnalyze in ["merge_gtJDT_5_CDJDT_4",  "merge_gt6_cd_5"]:
			for algorithm in ["Gumtree", "ChangeDistiller", "XyMatcher"]:
				computeBestConfigurationsFast("../../results/{}/".format(folderToAnalyze), suffix="{}_{}".format(folderToAnalyze,algorithm), key = algorithm)

	def test_ComputeBestKFoldComplete(self):
		for folderToAnalyze in ["merge_gtJDT_5_CDJDT_4",
								#"merge_gt6_cd_5"
								]:
			print("\nanalyzing {}".format(folderToAnalyze))
			kvalue = 10
			random_seed_value = 0
			allOptimized = []
			allDefault = []
			optimizedgt, defaultgt,  rp_index,srho_index,pmann_index, pwilcoxon_index, rp_performance,srho_performance,pmann_performance,pwilcoxon_performance = computeGridSearchKFold("../../plots/data/distance_per_diff_{}_Gumtree.csv".format(folderToAnalyze), kFold=kvalue, algorithm="Gumtree", defaultId="ClassicGumtree_0.5_1000_2", random_seed=random_seed_value, datasetname=folderToAnalyze)
			allOptimized.append(optimizedgt)
			allDefault.append(defaultgt)

			optimizedcd, defaultcd,  rp_index,srho_index,pmann_index, pwilcoxon_index, rp_performance,srho_performance,pmann_performance,pwilcoxon_performance =computeGridSearchKFold("../../plots/data/distance_per_diff_{}_ChangeDistiller.csv".format(folderToAnalyze), kFold=kvalue,   algorithm="ChangeDistiller", defaultId="ChangeDistiller_0.5_4_0.6_0.4", random_seed=random_seed_value, datasetname=folderToAnalyze)
			allOptimized.append(optimizedcd)
			allDefault.append(defaultcd)

			optimizedxy, defaultxy,  rp_index,srho_index,pmann_index, pwilcoxon_index, rp_performance,srho_performance,pmann_performance,pwilcoxon_performance =computeGridSearchKFold("../../plots/data/distance_per_diff_{}_Xy.csv".format(folderToAnalyze), kFold=kvalue,	algorithm="Xy", defaultId="XyMatcher_2_0.5", random_seed=random_seed_value, datasetname=folderToAnalyze)
			allOptimized.append(optimizedxy)
			allDefault.append(defaultxy)

			print("END: printing summary")

			print("optimizedgt: {}".format(optimizedgt))
			print("defaultgt: {}".format(defaultgt))
			print("optimizedcd: {}".format(optimizedcd))
			print("defaultcd: {}".format(defaultcd))
			print("optimizedxy: {}".format(optimizedxy))
			print("defaultxy: {}".format(defaultxy))

			plotImprovements(improvements=allOptimized, defaults=allDefault, key = folderToAnalyze)
			print("\nanalyzing {}".format(folderToAnalyze))

	def _test_ComputeBestKFoldSingle(self):
		folderToAnalyze = "merge_gt6_cd_5"
		kvalue = 2

		#optimized, default,  rp_index,srho_index,pmann_index, pwilcoxon_index, rp_performance,srho_performance,pmann_performance,pwilcoxon_performance =\
		computeGridSearchKFold("../../plots/data/distance_per_diff_{}_ChangeDistiller.csv".format(folderToAnalyze), kFold=kvalue,   algorithm="ChangeDistiller", defaultId="ChangeDistiller_0.5_4_0.6_0.4", random_seed=0,  datasetname=folderToAnalyze)



	'''For Table of  RQ 1'''
	def _test_ComparisonBest_TableRQ1(self):
		computeBestAndDefaultByFoldFiles("GumTree",
										 "/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/summary_merge_gtJDT_5_CDJDT_4_Gumtree_performanceTestingBestOnTraining.csv"
										 , "/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/summary_merge_gtJDT_5_CDJDT_4_Gumtree_performanceTestingDefaultOnTraining.csv")

		computeBestAndDefaultByFoldFiles("ChangeDistiller",
										 "/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/summary_merge_gtJDT_5_CDJDT_4_ChangeDistiller_performanceTestingBestOnTraining.csv"
										 ,
										 "/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/summary_merge_gtJDT_5_CDJDT_4_ChangeDistiller_performanceTestingDefaultOnTraining.csv")

		computeBestAndDefaultByFoldFiles("Xy",
										 "/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/summary_merge_gtJDT_5_CDJDT_4_Xy_performanceTestingBestOnTraining.csv"
										 ,
										 "/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/summary_merge_gtJDT_5_CDJDT_4_Xy_performanceTestingDefaultOnTraining.csv")

	'''For Table of  RQ 1'''
	def _test_ComparisonBest_PlotRQ1(self):
		plotDistributionAvg("/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/summary_avg_performance_performance_merge_gtJDT_5_CDJDT_4_GumTree.csv",
							"/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/summary_avg_performance_performance_merge_gtJDT_5_CDJDT_4_ChangeDistiller.csv",
							"/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/summary_avg_performance_performance_merge_gtJDT_5_CDJDT_4_Xy.csv",
							 "/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/summary_merge_gtJDT_5_CDJDT_4_Gumtree_performanceTestingDefaultOnTraining.csv",
							 "/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/summary_merge_gtJDT_5_CDJDT_4_ChangeDistiller_performanceTestingDefaultOnTraining.csv",
							"/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/summary_merge_gtJDT_5_CDJDT_4_Xy_performanceTestingDefaultOnTraining.csv")

	'''Deprecated, not used any more, those metrics are computed based on the  summary_avg_performance_performance'''
	def _testCheckTest(self):
		for algo in ["Gumtree", "ChangeDistiller", "Xy"]:
			for t in ["pmann", "pwilcoxon"]:
				for p in ["performance", "index"]:
					countHigherValuesFile(
						"/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/summary_merge_gtJDT_5_CDJDT_4_{}_{}_{}.csv".format(algo, t,p), thr=0.05)
			print("-----")

	'''Deprecated, not used any more, those metrics are computed based on the  summary_avg_performance_performance'''
	def _testCorrelation(self):
		for algo in ["Gumtree", "ChangeDistiller", "Xy"]:
			for t in ["rp", "srho"]:
				for p in ["performance", "index"]:
					countHigherValuesFile(
						"/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/summary_merge_gtJDT_5_CDJDT_4_{}_{}_{}.csv".format(algo, t,p), thr=0.90)
			print("-----")

if __name__ == '__main__':
	unittest.main()

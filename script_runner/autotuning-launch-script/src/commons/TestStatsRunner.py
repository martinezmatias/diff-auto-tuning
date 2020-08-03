import unittest
from src.commons.TestStats import *
from sklearn.linear_model import LinearRegression
class TestStatsRunner(unittest.TestCase):
	def _test_ComparisonBest(self):
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


	def _test_ComparisonBest(self):
		plotDistributionAvg("/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/summary_avg_performance_performance_merge_gtJDT_5_CDJDT_4_GumTree.csv",
							"/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/summary_avg_performance_performance_merge_gtJDT_5_CDJDT_4_ChangeDistiller.csv",
							"/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/summary_avg_performance_performance_merge_gtJDT_5_CDJDT_4_Xy.csv",
							 "/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/summary_merge_gtJDT_5_CDJDT_4_Gumtree_performanceTestingDefaultOnTraining.csv",
							 "/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/summary_merge_gtJDT_5_CDJDT_4_ChangeDistiller_performanceTestingDefaultOnTraining.csv",
							"/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/summary_merge_gtJDT_5_CDJDT_4_Xy_performanceTestingDefaultOnTraining.csv")

	def testCheckTest(self):
		for algo in ["Gumtree", "ChangeDistiller", "Xy"]:
			for t in ["pmann", "pwilcoxon"]:
				for p in ["performance", "index"]:
					countHigherValuesFile(
						"/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/summary_merge_gtJDT_5_CDJDT_4_{}_{}_{}.csv".format(algo, t,p), thr=0.05)
			print("-----")

	def _testCorrelation(self):
		for algo in ["Gumtree", "ChangeDistiller", "Xy"]:
			for t in ["rp", "srho"]:
				for p in ["performance", "index"]:
					countHigherValuesFile(
						"/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/summary_merge_gtJDT_5_CDJDT_4_{}_{}_{}.csv".format(algo, t,p), thr=0.90)
			print("-----")

	def _testSingleCorrelation(self):
		runReadResultsCrossValidation(path = "/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/summary_performance_performance_{}_K_{}_{}.csv", dataset ="merge_gtJDT_5_CDJDT_4", onlytop = False)

	def testHyperop1(self):
		for algo in ["Gumtree", "ChangeDistiller", "XyMatcher"]:
			#path = "/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/hyper_op_merge_gtJDT_5_CDJDT_4_{}_evals_1000_f_1.csv".format(algo)
			path = "/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/hyper_op_merge_gtJDT_5_CDJDT_4_{}_evals_100_f_0.5.csv".format(algo)
			analyzeHyperop(path, algo)


def isOutlier(value, mean,  std, m = 2):
	return abs(value - mean) > m * std

if __name__ == '__main__':
	unittest.main()

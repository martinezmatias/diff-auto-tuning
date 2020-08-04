import unittest
from src.commons.TestStats import *
from sklearn.linear_model import LinearRegression
class TestStatsRunner(unittest.TestCase):

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





if __name__ == '__main__':
	unittest.main()

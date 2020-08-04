import unittest
from src.commons.TestStats import *
from sklearn.linear_model import LinearRegression
from src.commons.Datalocation import *
class TestStatsRunner(unittest.TestCase):

	def testCheckTest(self):
		for algo in ["Gumtree", "ChangeDistiller", "Xy"]:
			for t in ["pmann", "pwilcoxon"]:
				for p in ["performance", "index"]:
					pass
					#countHigherValuesFile("{}/summary_{}_{}_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION,NAME_FOLDER_ASTJDT,algo, t,p), thr=0.05)
			print("-----")

	def _testCorrelation(self):
		for algo in ["Gumtree", "ChangeDistiller", "Xy"]:
			for t in ["rp", "srho"]:
				for p in ["performance", "index"]:
					pass
					#countHigherValuesFile(		"{}/summary_{}_{}_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION,NAME_FOLDER_ASTJDT, algo, t,p), thr=0.90)
			print("-----")

	def _testSingleCorrelation(self):
		#runReadResultsCrossValidation(path = RESULTS_PROCESSED_LOCATION+ "/summary_performance_performance_{}_K_{}_{}.csv", dataset =NAME_FOLDER_ASTJDT, onlytop = False)
		pass




if __name__ == '__main__':
	unittest.main()

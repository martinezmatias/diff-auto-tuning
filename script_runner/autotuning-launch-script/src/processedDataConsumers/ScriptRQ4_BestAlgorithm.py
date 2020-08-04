import unittest

from src.rowDataConsumers.ResultsAnalyzeDiffConfiguration import *

class TestRQ4(unittest.TestCase):

	def _test_ComputeFitnessFast(self):
		#folderToAnalyze = "merge_gt6_cd_5"
		folderToAnalyze = "merge_gtJDT_5_CDJDT_4"
		computeBestConfigurationsFast("../../results/{}/".format(folderToAnalyze), suffix=folderToAnalyze)

if __name__ == '__main__':
	unittest.main()

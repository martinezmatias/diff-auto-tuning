import unittest
from src.processedDataConsumers.deprecated.ResultsReadCheckPositionDefault import *


class MyTestCase(unittest.TestCase):
	def _test_AnalyzeTimeSize(self):
		getPositionDefalt(fileLocation="../../results/summary/best_configurations_summary.csv")

if __name__ == '__main__':
	unittest.main()

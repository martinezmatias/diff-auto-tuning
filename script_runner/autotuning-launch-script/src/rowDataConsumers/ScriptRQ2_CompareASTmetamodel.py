import unittest
from src.rowDataConsumers.EngineCompareASTMetadata import  *
from src.commons.Datalocation import *
class MyTestCase(unittest.TestCase):
	def _test_compareASTModel(self):
		m1 = retrieveTreeMetric(RESULTS_ROW_ASTSPOON, useSize=True) #../../results/out10bis5_4gt/"
		m2 = retrieveTreeMetric (RESULTS_ROW_ASTJDT, useSize=True) # "../../results/out4gtJDT_3/"

		compareTwoSamples(m1, m2, "size")


	def _test_compareASTModelHeigth(self):
		m1 = retrieveTreeMetric(RESULTS_ROW_ASTSPOON, useSize=False)
		m2 = retrieveTreeMetric (RESULTS_ROW_ASTJDT, useSize=False)

		compareTwoSamples(m1, m2, "heigth")


if __name__ == '__main__':
	unittest.main()

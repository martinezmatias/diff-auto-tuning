import unittest
from src.rowDataConsumers.EngineCompareASTMetadata import  *

class MyTestCase(unittest.TestCase):
	def _test_compareASTModel(self):
		m1 = retrieveTreeMetric("../../results/out10bis5_4gt/", useSize=True)
		m2 = retrieveTreeMetric ("../../results/out4gtJDT_3/", useSize=True)

		compareTwoSamples(m1, m2, "size")


	def _test_compareASTModelHeigth(self):
		m1 = retrieveTreeMetric("../../results/out10bis5_4gt/", useSize=False)
		m2 = retrieveTreeMetric ("../../results/out4gtJDT_3/", useSize=False)

		compareTwoSamples(m1, m2, "heigth")


if __name__ == '__main__':
	unittest.main()

import unittest
from ResultsAnalyzers import *
from ReadCSV import *

class MyTestCase(unittest.TestCase):

	def _test_CompareTwoExecutions(self):

		compareNewExecutedBetweenTwoExecutions("lastindex/lastIndext_GTSPOON_1592923835.802567.json",
											   "lastindex/lastIndext_GTSPOON_1592924043.853812.json")

	def _test_something(self):
		saveExecutionsIndexOfBatches(out)

	def test_ParserResults(self):
		parserCSV("./out/")
if __name__ == '__main__':
	unittest.main()

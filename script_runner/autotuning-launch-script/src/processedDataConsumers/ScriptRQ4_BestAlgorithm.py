import unittest
from src.commons.Datalocation import *
from src.rowDataConsumers.RQ0_Setup_ComputeFitnessOfConfiguationsFromRowData import *

class TestRQ4(unittest.TestCase):

	def _test_ComputeFitnessFast(self):
		folderToAnalyze = NAME_FOLDER_ASTJDT
		computeFitness("{}/{}/".format(RESULTS_ROW_LOCATION,folderToAnalyze), suffix=folderToAnalyze)

if __name__ == '__main__':
	unittest.main()

import unittest
from src.rowDataConsumers.RQ0_Setup_ResultsCountPairsAnalyzed import *
from src.commons.DatasetMerger import *
from src.commons.Datalocation import *
class TestSetup(unittest.TestCase):

	def _test_countAnalyzed(self):
		countFilePairsAnalyzed(RESULTS_ROW_ASTSPOON)
		countFilePairsAnalyzed(RESULTS_ROW_ASTJDT)


	def _test_mergedatasetsSpoon(self):
		ds1 = "outgt6"
		ds2 = "outCD_5"
		merge(location1="{}/{}/".format(RESULTS_ROW_LOCATION, ds1),
			location2="{}/{}".format(RESULTS_ROW_LOCATION, ds2),
			destination="{}/merge_{}_{}".format(RESULTS_ROW_LOCATION, ds1, ds2))

	def _test_mergedatasetsJDT(self):
		ds1 = "outgtJDT_5"
		ds2 = "outCDJDT_4"
		merge(location1="{}/{}/".format(RESULTS_ROW_LOCATION, ds1),
			location2="{}/{}".format(RESULTS_ROW_LOCATION, ds2),
			destination="{}/merge_{}_{}".format(RESULTS_ROW_LOCATION, ds1, ds2))

if __name__ == '__main__':
	unittest.main()

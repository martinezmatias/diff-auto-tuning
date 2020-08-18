import unittest
from src.rowDataConsumers.RQ0_Setup_ResultsCountPairsAnalyzed import *
from src.commons.DatasetMerger import *
from src.commons.Datalocation import *
class TestSetup(unittest.TestCase):

	def test_countAnalyzedMerged(self):
		countFilePairsAnalyzed(RESULTS_ROW_ASTSPOON)
		countFilePairsAnalyzed(RESULTS_ROW_ASTJDT)
	
	def _test_countAnalyzedSingleCD(self):
		#countFilePairsAnalyzed(RESULTS_ROW_ASTSPOON)
		countFilePairsAnalyzed("{}/{}/".format(RESULTS_ROW_LOCATION, "outgtJDT_7"))
		countFilePairsAnalyzed("{}/{}/".format(RESULTS_ROW_LOCATION, "outCDJDT_6"))
		countFilePairsAnalyzed("{}/{}/".format(RESULTS_ROW_LOCATION, "outgt_8"))
		countFilePairsAnalyzed("{}/{}/".format(RESULTS_ROW_LOCATION, "outCD_7"))

	def _test_mergedatasetsSpoon(self):
		ds1 = "outgt_8"
		ds2 = "outCD_7"
		merge(location1="{}/{}/".format(RESULTS_ROW_LOCATION, ds1),
			location2="{}/{}".format(RESULTS_ROW_LOCATION, ds2),
			destination="{}/merge_{}_{}".format(RESULTS_ROW_LOCATION, ds1, ds2))

	def _test_mergedatasetsJDT(self):
		ds1 = "outgtJDT_7"
		ds2 = "outCDJDT_6"
		merge(location1="{}/{}/".format(RESULTS_ROW_LOCATION, ds1),
			location2="{}/{}".format(RESULTS_ROW_LOCATION, ds2),
			destination="{}/merge_{}_{}".format(RESULTS_ROW_LOCATION, ds1, ds2))

if __name__ == '__main__':
	unittest.main()

import unittest
from src.rowDataConsumers.RQ0_Setup_ResultsCountPairsAnalyzed import *
from src.commons.DatasetMerger import *
from src.commons.Datalocation import *
class TestSetup(unittest.TestCase):

	def _test_countAnalyzed(self):
		countFilePairsAnalyzed(RESULTS_ROW_ASTSPOON)
		countFilePairsAnalyzed(RESULTS_ROW_ASTJDT)


	def _test_mergedatasetsSpoon(self):
		merge(location1="{}/outgt6/".format(RESULTS_ROW_LOCATION),
			location2="{}/outCD_5".format(RESULTS_ROW_LOCATION),
			destination="{}/merge_gt6_cd_5".format(RESULTS_ROW_LOCATION))

	def _test_mergedatasetsJDT(self):
		merge(location1="{}/outgtJDT_5/".format(RESULTS_ROW_LOCATION),
			location2="{}/outCDJDT_4".format(RESULTS_ROW_LOCATION),
			destination="{}/merge_gtJDT_5_CDJDT_4".format(RESULTS_ROW_LOCATION))

if __name__ == '__main__':
	unittest.main()

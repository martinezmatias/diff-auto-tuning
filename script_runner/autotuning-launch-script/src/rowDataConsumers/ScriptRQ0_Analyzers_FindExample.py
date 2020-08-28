import unittest
from src.rowDataConsumers.RQ0_Setup_SearchExample import *
from src.rowDataConsumers.deprecated.ResultsAnalyzeTimes import *

class TestFindExample(unittest.TestCase):
	def _test_A_findexample(self):
		for folderToAnalyze in [NAME_FOLDER_ASTJDT,  NAME_FOLDER_ASTSPOON]:
				searchExampleForPaper("{}/{}/".format(RESULTS_ROW_LOCATION, folderToAnalyze), suffix=(folderToAnalyze), key = None)

		print("End")

	def _test_B_sampleforsurvey(self):
		for folderToAnalyze in [
			NAME_FOLDER_ASTJDT,
			  NAME_FOLDER_ASTSPOON
								]:
				## the key and the algorithm can be different: the algorithm is used for retrieving the default config, the key for filtering (we can filter several gumtrees -simple, classic-)
				searchExampleForPaper("{}/{}/".format(RESULTS_ROW_LOCATION, folderToAnalyze), suffix=(folderToAnalyze), key = "Gumtree", algorithm="ClassicGumtree", thresholdEDsize = 100000, thresholdEDsizeDefault = 10000000 )

		print("End")

	def test_c_times(self):
		for folderToAnalyze in [
			"merge_gtJDT_7_CDJDT_6" ##NAME_FOLDER_ASTJDT,
			# "merge_gt_8_CD_7" # NAME_FOLDER_ASTSPOON
								]:
				## the key and the algorithm can be different: the algorithm is used for retrieving the default config, the key for filtering (we can filter several gumtrees -simple, classic-)
				#searchExampleForPaper("{}/{}/".format(RESULTS_ROW_LOCATION, folderToAnalyze), suffix=(folderToAnalyze), key = "Gumtree", algorithm="ClassicGumtree", thresholdEDsize = 100000, thresholdEDsizeDefault = 10000000 )

				analyzeExecutionTime("{}/{}/".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze), "JDT" if "JDT" in folderToAnalyze else "Spoon");
		print("End")
if __name__ == '__main__':
	unittest.main()

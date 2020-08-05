import unittest
from src.rowDataConsumers.RQ0_Setup_SearchExample import *

class TestFindExample(unittest.TestCase):
	def test_A_findexample(self):
		for folderToAnalyze in [NAME_FOLDER_ASTJDT,  NAME_FOLDER_ASTSPOON]:
				searchExampleForPaper("{}/{}/".format(RESULTS_ROW_LOCATION, folderToAnalyze), suffix=(folderToAnalyze), key = None)

		print("End")
if __name__ == '__main__':
	unittest.main()

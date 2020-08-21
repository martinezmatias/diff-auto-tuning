from src.processedDataConsumers.RQ3_PerformanceMetamodel_MetaResultsCompareDistribution import *
import unittest
from src.commons.Datalocation import *
from src.commons.DiffAlgorithmMetadata import *
class TestHyperOp(unittest.TestCase):

	def _test_CompareDistributionBestDefault(self):
		folderToAnalyze = NAME_FOLDER_ASTSPOON
		compareDistributions(pathResults="{}/distance_per_diff_{}.csv".format(RESULTS_ROW_LOCATION,folderToAnalyze), keyBestConfiguration="ClassicGumtree_0.1_2000_1", keyDefaultConfiguration=defaultConfigurations["ClassicGumtree"])


	def _test_CompareCrossDistributionBestDefault(self):
		folderToAnalyze = NAME_FOLDER_ASTSPOON
		algorithm = "Gumtree"
		crossResults(pathDistances="{}/distance_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze, algorithm),
					 pathSize="{}/editscript_size_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze,
																		   algorithm),
					 keyBestConfiguration="ClassicGumtree_0.1_2000_1",
					 keyDefaultConfiguration=defaultConfigurations["ClassicGumtree"])

	def test_CompareCrossDistributionBestDefaultGTSpoon(self):
		##compare with the default of gtspoon
		folderToAnalyze = NAME_FOLDER_ASTSPOON
		algorithm = "Gumtree"
		crossResults(pathDistances="{}/distance_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze, algorithm),
					 pathSize="{}/editscript_size_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze,
																		   algorithm),
					 keyBestConfiguration="ClassicGumtree_0.6_1000_1",
					 keyDefaultConfiguration=defaultConfigurations["ClassicGumtree"])

from src.processedDataConsumers.RQ3_PerformanceMetamodel_MetaResultsCompareDistribution import *
import unittest
from src.commons.Datalocation import *
from src.commons.DiffAlgorithmMetadata import *
class TestHyperOp(unittest.TestCase):

	def _test_CompareDistributionBestDefault(self):
		folderToAnalyze = NAME_FOLDER_ASTSPOON
		compareDistributions(pathResults="{}/editscript_size_per_diff_{}_GumTree.csv".format(RESULTS_ROW_LOCATION,folderToAnalyze),
							 keyBestConfiguration="ClassicGumtree_0.1_2000_1", keyDefaultConfiguration=defaultConfigurations["ClassicGumtree"])


	def _test_CompareCrossDistributionBestDefault(self):
		folderToAnalyze = NAME_FOLDER_ASTSPOON
		algorithm = "Gumtree"
		crossResults(pathDistances="{}/distance_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze, algorithm),
					 pathSize="{}/editscript_size_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze,
																		   algorithm),
					 keyBestConfiguration="ClassicGumtree_0.1_2000_1",
					 keyDefaultConfiguration=defaultConfigurations["ClassicGumtree"])

	def _test_CompareCrossDistributionBestDefaultGTSpoon(self):
		##compare with the default of gtspoon
		folderToAnalyze = NAME_FOLDER_ASTSPOON
		algorithm = "Gumtree"
		crossResults(pathDistances="{}/distance_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze, algorithm),
					 pathSize="{}/editscript_size_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze,
																		   algorithm),
					 keyBestConfiguration="ClassicGumtree_0.6_1000_1",
					 keyDefaultConfiguration=defaultConfigurations["ClassicGumtree"])




	def _test_CompareDistributionBestGridAndTPE(self):
		folderToAnalyze = NAME_FOLDER_ASTJDT
		## we want to compare the differences between the best from grid and the best ##SimpleGumtree_0.1_1 from TPE ClassicGumtree_0.1_2000_1
		bestGrid = "SimpleGumtree_0.1_1"
		bestTPE =  "ClassicGumtree_0.1_2000_1"

		algorithm = "Gumtree"
		pathSizeMatrix = "{}/editscript_size_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze,
																	 algorithm)
		crossResults(pathDistances="{}/distance_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze,
																		   algorithm),
					 pathSize=pathSizeMatrix,
					 keyBestConfiguration=bestGrid,
					 keyDefaultConfiguration=bestTPE)

		#compareDistributions(pathResults=pathSizeMatrix, keyBestConfiguration=bestGrid, keyDefaultConfiguration=bestTPE)


	def test_CompareAutotune(self):
		for model in [  NAME_FOLDER_ASTJDT, NAME_FOLDER_ASTSPOON
					   ]:

			bestConfig = {}

			bestConfig[NAME_FOLDER_ASTJDT] = "ClassicGumtree_0.1_2000_1"#"SimpleGumtree_0.1_1"
			bestConfig[NAME_FOLDER_ASTSPOON] = "ClassicGumtree_0.1_2000_1"
			algorithm = "Gumtree"
			pathSizeMatrix = "{}/editscript_size_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION, model,
																		 algorithm)
			pathDistances = "{}/distance_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION, model,
																	algorithm)

			#print("reading data for {}".format(pathDistances))
			#dfDistances = pandas.read_csv(pathDistances, sep=",")
			print("reading data for {}".format(pathSizeMatrix))
			dfSize = pandas.read_csv(pathSizeMatrix, sep=",")

			experimentAutoTuning(dfDistances=None,
						 dfSize=dfSize,
						keyBestConfiguration=bestConfig[model],
						 keyDefaultConfiguration=defaultConfigurations["ClassicGumtree"])
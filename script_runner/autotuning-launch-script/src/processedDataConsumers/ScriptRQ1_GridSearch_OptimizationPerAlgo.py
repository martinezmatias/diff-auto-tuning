import unittest

from src.processedDataConsumers.EngineGridSearchKfoldValidation import *
from src.processedDataConsumers.deprecated.plotPerformance import *
from src.rowDataConsumers.RQ0_Setup_ComputeFitnessDistanceOfConfiguationsFromRowData import *
from src.commons.Datalocation import *

class TestGrid(unittest.TestCase):


	def _test_A_ComputeFitnessFastPerAlgorithm(self):

		for folderToAnalyze in [NAME_FOLDER_ASTJDT,  NAME_FOLDER_ASTSPOON]:
			for algorithm in ["Gumtree", "ChangeDistiller", "XyMatcher"]:
				computeFitness("{}/{}/".format(RESULTS_ROW_LOCATION, folderToAnalyze), suffix="{}_{}".format(folderToAnalyze, algorithm), key = algorithm)

	def test_B_ComputeBestKFoldComplete(self):
		for folderToAnalyze in [NAME_FOLDER_ASTJDT,
								NAME_FOLDER_ASTSPOON
								]:
			print("\nanalyzing {}".format(folderToAnalyze))
			kvalue = 10
			random_seed_value = 0
			allOptimized = []
			allDefault = []
			optimizedgt, defaultgt,  rp_index,srho_index,pmann_index, pwilcoxon_index, rp_performance,srho_performance,pmann_performance,pwilcoxon_performance = computeGridSearchKFold("{}/distance_per_diff_{}_Gumtree.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze), kFold=kvalue, algorithm="Gumtree", defaultId="ClassicGumtree_0.5_1000_2", random_seed=random_seed_value, datasetname=folderToAnalyze)
			allOptimized.append(optimizedgt)
			allDefault.append(defaultgt)

			optimizedcd, defaultcd,  rp_index,srho_index,pmann_index, pwilcoxon_index, rp_performance,srho_performance,pmann_performance,pwilcoxon_performance =computeGridSearchKFold("{}/distance_per_diff_{}_ChangeDistiller.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze), kFold=kvalue,   algorithm="ChangeDistiller", defaultId="ChangeDistiller_0.5_4_0.6_0.4", random_seed=random_seed_value, datasetname=folderToAnalyze)
			allOptimized.append(optimizedcd)
			allDefault.append(defaultcd)

			optimizedxy, defaultxy,  rp_index,srho_index,pmann_index, pwilcoxon_index, rp_performance,srho_performance,pmann_performance,pwilcoxon_performance =computeGridSearchKFold("{}/distance_per_diff_{}_Xy.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze), kFold=kvalue,	algorithm="Xy", defaultId="XyMatcher_2_0.5", random_seed=random_seed_value, datasetname=folderToAnalyze)
			allOptimized.append(optimizedxy)
			allDefault.append(defaultxy)

			print("END: printing summary")

			print("optimizedgt: {}".format(optimizedgt))
			print("defaultgt: {}".format(defaultgt))
			print("optimizedcd: {}".format(optimizedcd))
			print("defaultcd: {}".format(defaultcd))
			print("optimizedxy: {}".format(optimizedxy))
			print("defaultxy: {}".format(defaultxy))

			plotImprovements(improvements=allOptimized, defaults=allDefault, key = folderToAnalyze)
			print("\nanalyzing {}".format(folderToAnalyze))


	'''For Table of  RQ 1'''
	def _test_C_ComparisonBest_TableRQ1(self):

		for folderToAnalyze in [NAME_FOLDER_ASTJDT, NAME_FOLDER_ASTSPOON]:
			for algorithm in ["Gumtree", "ChangeDistiller", "Xy"]:

				computeBestAndDefaultByFoldFiles("GumTree",
												 "{}/summary_{}_{}_performanceTestingBestOnTraining.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze, algorithm)
												 , "{}/summary_{}_{}_performanceTestingDefaultOnTraining.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze, algorithm))



	'''For Table of  RQ 1'''
	def _test_D_ComparisonBest_PlotRQ1(self):
		dataset = NAME_FOLDER_ASTSPOON
		plotDistributionAvg("{}/summary_avg_performance_performance_{}_GumTree.csv".format(RESULTS_PROCESSED_LOCATION, dataset),
							"{}/summary_avg_performance_performance_{}_ChangeDistiller.csv".format(RESULTS_PROCESSED_LOCATION, dataset),
							"{}/summary_avg_performance_performance_{}_Xy.csv".format(RESULTS_PROCESSED_LOCATION, dataset),
							 "{}/summary_{}_Gumtree_performanceTestingDefaultOnTraining.csv".format(RESULTS_PROCESSED_LOCATION, dataset),
							 "{}/summary_{}_ChangeDistiller_performanceTestingDefaultOnTraining.csv".format(RESULTS_PROCESSED_LOCATION, dataset),
							"{}/summary_{}_Xy_performanceTestingDefaultOnTraining.csv".format(RESULTS_PROCESSED_LOCATION, dataset))

	'''Deprecated, not used any more, those metrics are computed based on the  summary_avg_performance_performance'''
	def _testCheckTest(self):
		dataset = NAME_FOLDER_ASTSPOON
		for algo in ["Gumtree", "ChangeDistiller", "Xy"]:
			for t in ["pmann", "pwilcoxon"]:
				for p in ["performance", "index"]:
					countHigherValuesFile(
						"{}/summary_{}_{}_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION, dataset, algo, t,p), thr=0.05)
			print("-----")

	'''Deprecated, not used any more, those metrics are computed based on the  summary_avg_performance_performance'''
	def _testCorrelation(self):
		dataset = NAME_FOLDER_ASTSPOON
		for algo in ["Gumtree", "ChangeDistiller", "Xy"]:
			for t in ["rp", "srho"]:
				for p in ["performance", "index"]:
					countHigherValuesFile(
						"{}/summary_{}_{}_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION,dataset, algo, t,p), thr=0.90)
			print("-----")

if __name__ == '__main__':
	unittest.main()

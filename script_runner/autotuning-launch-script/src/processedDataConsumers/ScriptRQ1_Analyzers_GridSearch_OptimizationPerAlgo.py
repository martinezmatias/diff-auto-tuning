import unittest

from src.processedDataConsumers.EngineGridSearchKfoldValidation import *
from src.processedDataConsumers.deprecated.plotPerformance import *
from src.rowDataConsumers.RQ0_Setup_ComputeFitnessDistanceOfConfiguationsFromRowData import *
from src.commons.Datalocation import *
from src.commons.DiffAlgorithmMetadata import *

class TestGrid(unittest.TestCase):


	def _test_A_ComputeFitnessFastPerAlgorithm(self):

		for folderToAnalyze in [NAME_FOLDER_ASTJDT,  NAME_FOLDER_ASTSPOON]:
			for algorithm in ["Gumtree", "ChangeDistiller", "XyMatcher"]:
				computeFitness("{}/{}/".format(RESULTS_ROW_LOCATION, folderToAnalyze), suffix="{}_{}".format(folderToAnalyze, algorithm), key = algorithm)

	def _test_B_ComputeBestKFoldComplete(self):
		for folderToAnalyze in [NAME_FOLDER_ASTJDT,
								NAME_FOLDER_ASTSPOON
								]:
			print("\nanalyzing {}".format(folderToAnalyze))
			kvalue = 3#10
			random_seed_value = 35
			fraction = 0.1

			computeGridSearchKFold("{}/distance_per_diff_{}_Gumtree.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze), kFold=kvalue, algorithm="Gumtree", defaultId=defaultConfigurations["ClassicGumtree"], random_seed=random_seed_value, datasetname=folderToAnalyze, fration=fraction)


			computeGridSearchKFold("{}/distance_per_diff_{}_ChangeDistiller.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze), kFold=kvalue,   algorithm="ChangeDistiller", defaultId=defaultConfigurations["ChangeDistiller"], random_seed=random_seed_value, datasetname=folderToAnalyze, fration=fraction)


			computeGridSearchKFold("{}/distance_per_diff_{}_XyMatcher.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze), kFold=kvalue,	algorithm="XyMatcher", defaultId=defaultConfigurations["XyMatcher"], random_seed=random_seed_value, datasetname=folderToAnalyze, fration=fraction)

	def _test_B_ComputeBestKFoldComplete(self):
		for folderToAnalyze in [NAME_FOLDER_ASTJDT,
								NAME_FOLDER_ASTSPOON
								]:
			print("\nanalyzing {}".format(folderToAnalyze))
			kvalue = 5#10
			random_seed_value = 1
			fraction = 1

			computeGridSearchKFold("{}/editscript_size_per_diff_{}_Gumtree.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze),overwrite=True, kFold=kvalue, algorithm="Gumtree", defaultId=defaultConfigurations["ClassicGumtree"], random_seed=random_seed_value, datasetname=folderToAnalyze, fration=fraction)

	def test_B_ComputeBestKFoldtest(self):
		for folderToAnalyze in [NAME_FOLDER_ASTJDT,
								NAME_FOLDER_ASTSPOON
								]:
			print("\nanalyzing {}".format(folderToAnalyze))
			kvalue = 2#10
			random_seed_value = 1
			fraction = 0.001

			computeGridSearchKFold("{}/editscript_size_per_diff_{}_Gumtree.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze),overwrite=True, kFold=kvalue, algorithm="Gumtree", defaultId=defaultConfigurations["ClassicGumtree"], random_seed=random_seed_value, datasetname=folderToAnalyze, fration=fraction)



	def _test_C_plot(self):
		##TODO: the data must be readen from files
		for folderToAnalyze in [NAME_FOLDER_ASTJDT,
								NAME_FOLDER_ASTSPOON
								]:
			print("\nanalyzing {}".format(folderToAnalyze))
			kvalue = 4#10
			random_seed_value = 1
			fraction = 1
			allOptimized = []
			allDefault = []
			optimizedgt, defaultgt,  rp_index,srho_index,pmann_index, pwilcoxon_index, rp_performance,srho_performance,pmann_performance,pwilcoxon_performance = computeGridSearchKFold("{}/distance_per_diff_{}_Gumtree.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze), kFold=kvalue, algorithm="Gumtree", defaultId=defaultConfigurations["ClassicGumtree"], random_seed=random_seed_value, datasetname=folderToAnalyze, fration=fraction)
			allOptimized.append(optimizedgt)
			allDefault.append(defaultgt)

			optimizedcd, defaultcd,  rp_index,srho_index,pmann_index, pwilcoxon_index, rp_performance,srho_performance,pmann_performance,pwilcoxon_performance =computeGridSearchKFold("{}/distance_per_diff_{}_ChangeDistiller.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze), kFold=kvalue,   algorithm="ChangeDistiller", defaultId=defaultConfigurations["ChangeDistiller"], random_seed=random_seed_value, datasetname=folderToAnalyze, fration=fraction)
			allOptimized.append(optimizedcd)
			allDefault.append(defaultcd)

			optimizedxy, defaultxy,  rp_index,srho_index,pmann_index, pwilcoxon_index, rp_performance,srho_performance,pmann_performance,pwilcoxon_performance =computeGridSearchKFold("{}/distance_per_diff_{}_XyMatcher.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze), kFold=kvalue,	algorithm="XyMatcher", defaultId=defaultConfigurations["XyMatcher"], random_seed=random_seed_value, datasetname=folderToAnalyze, fration=fraction)
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
		#GridSearch
		#/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/results/summaryResults/summaryResults/GridSearch/dataset_merge_out4gt_outCD/algorithm_Gumtree/seed_0/fractionds_1

		for astModel in [NAME_FOLDER_ASTJDT, NAME_FOLDER_ASTSPOON]:
			for algorithm in ["Gumtree", "ChangeDistiller", "XyMatcher"]:
				## for proportion 1, we took any seed, all have the same results

				fraction = 1
				locationFileResults = "{}/summaryResults/GridSearch/dataset_{}/algorithm_{}/seed_0/fractionds_{}/".format(RESULTS_PROCESSED_LOCATION,astModel, algorithm, fraction)
				computeBestAndDefaultByFoldFiles(astModel, algorithm,
												 "{}/summary_{}_{}_performanceTestingBestOnTraining_f_{}.csv".format(locationFileResults,astModel, algorithm,fraction)
												 , "{}/summary_{}_{}_performanceTestingDefaultOnTraining_f_{}.csv".format(locationFileResults, astModel, algorithm,fraction))

	'''For Table of  RQ 1'''
	def _test_D_ComparisonBest_PlotRQ1(self):
		for astModel in [NAME_FOLDER_ASTJDT, NAME_FOLDER_ASTSPOON]:
			fraction = 1

			locationFileResultsGumTree = "{}/summaryResults/GridSearch/dataset_{}/algorithm_{}/seed_0/fractionds_{}/".format(
				RESULTS_PROCESSED_LOCATION, astModel, "Gumtree", fraction)

			locationFileResultsChangeDistiller = "{}/summaryResults/GridSearch/dataset_{}/algorithm_{}/seed_0/fractionds_{}/".format(
				RESULTS_PROCESSED_LOCATION, astModel, "ChangeDistiller", fraction)
			locationFileResultsXyMatcher = "{}/summaryResults/GridSearch/dataset_{}/algorithm_{}/seed_0/fractionds_{}/".format(
				RESULTS_PROCESSED_LOCATION, astModel, "XyMatcher", fraction)

			plotDistributionAvg( "{}/avg_performance_performanceOnTraining_{}_GumTree_f_{}.csv".format(locationFileResultsGumTree, astModel, fraction),
								"{}/avg_performance_performanceOnTraining_{}_ChangeDistiller_f_{}.csv".format(locationFileResultsChangeDistiller, astModel, fraction),
								"{}/avg_performance_performanceOnTraining_{}_XyMatcher_f_{}.csv".format(locationFileResultsXyMatcher, astModel, fraction),
								 "{}/summary_{}_Gumtree_performanceTestingDefaultOnTraining_f_{}.csv".format(locationFileResultsGumTree, astModel, fraction),
								 "{}/summary_{}_ChangeDistiller_performanceTestingDefaultOnTraining_f_{}.csv".format(locationFileResultsChangeDistiller, astModel, fraction),
								"{}/summary_{}_XyMatcher_performanceTestingDefaultOnTraining_f_{}.csv".format(locationFileResultsXyMatcher, astModel, fraction),
								 model= "JDT" if "JDT" in astModel else "Spoon", out=RESULTS_PROCESSED_LOCATION)

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

import unittest
from src.processedDataConsumers.EngineHyperOptDAT import *
from src.processedDataConsumers.EngineLocalHyperOptDAT import *
from src.commons.Datalocation import *
from src.rowDataConsumers.RQ0_Setup_ComputeEDSizeOfConfiguationsFromRowData import *
from src.processedDataConsumers.CostParameters import *

from collections import Counter

class TestHyperOp(unittest.TestCase):

	def _test_A_ComputeDistanceFastPerAlgorithm(self):

		for folderToAnalyze in [NAME_FOLDER_ASTJDT,  NAME_FOLDER_ASTSPOON]:
			for algorithm in ["Gumtree", "ChangeDistiller", "XyMatcher"]:
				computeEditScriptSize("{}/{}/".format(RESULTS_ROW_LOCATION, folderToAnalyze), suffix="{}_{}".format(folderToAnalyze, algorithm), key = algorithm)


	def _test_CompteHyperOpt_single_by_algo(self):
			kfold = 10
			maxeval = 202#1000
			franction = 1
			seed=20
			for folderToAnalyze in [NAME_FOLDER_ASTSPOON, NAME_FOLDER_ASTJDT]:
				for algorithm in ["Gumtree", "ChangeDistiller",
								  "XyMatcher"]:
					print("\nanalyzing {}".format(folderToAnalyze))
					computeHyperOpt(pathResults="{}/editscript_size_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze, algorithm), kFold=kfold, max_evals=maxeval,fractiondata= franction,  dataset = folderToAnalyze, algorithm = algorithm, out=RESULTS_PROCESSED_LOCATION, random_seed=seed )

	def test_LocalCompteHyperOpt_single_by_algo_GumTree(self):

			kfold = 5
			maxeval = 10
			franction = 1
			seed=1
			TPE = True
			measure  = [True]
			for folderToAnalyze in [NAME_FOLDER_ASTJDT, NAME_FOLDER_ASTSPOON]:
				for algorithm in ["Gumtree"]:
					print("\nanalyzing {}".format(folderToAnalyze))
					for useAvg in measure:
						computeLocalHyperOpt(pathResults="{}/editscript_size_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze, algorithm),overwrite=False,useAverage=useAvg, runTpe=TPE,  kFold=kfold, max_evals=maxeval,fractiondata= franction,  dataset = folderToAnalyze, algorithm = algorithm, out=RESULTS_PROCESSED_LOCATION, random_seed=seed )



	def _test_CompteHyperOpt_single_by_algo_ChangeDistiller(self):
			''''only 1000 '''''
			kfold = 2
			maxeval = 10
			franction = 0.01
			seed=10
			TPE = True
			measure  = [True]
			for folderToAnalyze in [NAME_FOLDER_ASTSPOON, NAME_FOLDER_ASTJDT]:
				for algorithm in ["ChangeDistiller"]:
					print("\nanalyzing {}".format(folderToAnalyze))
					for useAvg in measure:
						computeHyperOpt(pathResults="{}/editscript_size_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze, algorithm),overwrite=True,useAverage=useAvg, runTpe=TPE,  kFold=kfold, max_evals=maxeval,fractiondata= franction,  dataset = folderToAnalyze, algorithm = algorithm, out=RESULTS_PROCESSED_LOCATION, random_seed=seed )


	def _test_CompteHyperOpt_single_by_algo_ChangeDistiller(self):
			''''only 1000 '''''
			kfold = 2
			maxeval = 10
			franction = 0.01
			seed=10
			TPE = True
			measure  = [True]
			for folderToAnalyze in [NAME_FOLDER_ASTSPOON, NAME_FOLDER_ASTJDT]:
				for algorithm in ["ChangeDistiller"]:
					print("\nanalyzing {}".format(folderToAnalyze))
					for useAvg in measure:
						computeHyperOpt(pathResults="{}/editscript_size_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze, algorithm),overwrite=True,useAverage=useAvg, runTpe=TPE,  kFold=kfold, max_evals=maxeval,fractiondata= franction,  dataset = folderToAnalyze, algorithm = algorithm, out=RESULTS_PROCESSED_LOCATION, random_seed=seed )


	def _test_CompteHyperOpt_single_by_algo_Xy(self):
			''''only 1000 '''''
			kfold = 5
			maxeval = 1000
			franction = 1
			seed=10
			TPE = True
			measure  = [True]
			for folderToAnalyze in [NAME_FOLDER_ASTSPOON, NAME_FOLDER_ASTJDT]:
				for algorithm in ["XyMatcher"]:
					print("\nanalyzing {}".format(folderToAnalyze))
					for useAvg in measure:
						computeHyperOpt(pathResults="{}/editscript_size_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze, algorithm),overwrite=True,useAverage=useAvg, runTpe=TPE,  kFold=kfold, max_evals=maxeval,fractiondata= franction,  dataset = folderToAnalyze, algorithm = algorithm, out=RESULTS_PROCESSED_LOCATION, random_seed=seed )



	def _test_CompteHyperOpt_single_by_algo_GumTree_useAverage(self):

			kfold = 2
			maxeval = 10
			franction = 0.001
			seed=20
			model = NAME_FOLDER_ASTJDT
			for algorithm in ["Gumtree"]:
				print("\nanalyzing {}".format(model))
				computeHyperOpt(pathResults="{}/editscript_size_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION,model, algorithm),
								useAverage=True,
								overwrite=True,
								kFold=kfold, max_evals=maxeval,fractiondata= franction,
								dataset = model, algorithm = algorithm, out="{}/test/".format(RESULTS_PROCESSED_LOCATION), random_seed=seed )





	def _test_debug_CompteHyperOpt_single_by_algo(self):
			kfold = 2
			maxeval = 10
			franction = 0.01
			seed=20
			TPE = False
			for folderToAnalyze in [NAME_FOLDER_ASTJDT]:
				for algorithm in ["Gumtree", "ChangeDistiller",
								  "XyMatcher"]:
					print("\nanalyzing {}".format(folderToAnalyze))
					computeHyperOpt(pathResults="{}/editscript_size_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze, algorithm),overwrite=True, runTpe=TPE, kFold=kfold, max_evals=maxeval,fractiondata= franction,  dataset = folderToAnalyze, algorithm = algorithm, out=RESULTS_PROCESSED_LOCATION, random_seed=seed )
					break

	def _test_CompteHyperOpt_allAlgos(self):
			''''only 1000 '''''
			kfold = 10
			for folderToAnalyze in [NAME_FOLDER_ASTJDT, NAME_FOLDER_ASTSPOON]:

					print("\nanalyzing {}".format(folderToAnalyze))
					computeHyperOpt(pathResults="{}/editscript_size_per_diff_{}.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze), kFold=kfold, max_evals=1000,fractiondata= 1,  dataset = folderToAnalyze, algorithm = None, out=RESULTS_PROCESSED_LOCATION )



	def _testAnalyzeHyperopResults(self):

		fraction = 1
		parentMethodFolder = None
		childMethodFolder = None

		proportionEvals = 0.1
		isTPE = True
		useAvg = True
		print("Run tpe? {}".format(isTPE))
		if isTPE:
			parentMethodFolder = "TPE"
			childMethodFolder = "hyper_op"
		else:
			parentMethodFolder = "random"
			childMethodFolder = "random_op"

		for modelToAnalyze in [NAME_FOLDER_ASTJDT, NAME_FOLDER_ASTSPOON]:
			modelName = "JDT" if "JDT" in modelToAnalyze else "Spoon"
			for algo in ["Gumtree", "ChangeDistiller", "XyMatcher"]:

				sizeSpaceAlgo = sizeSearchSpace[algo]
				evals = int(proportionEvals * sizeSpaceAlgo)
					# if is zero, we put 1
				evals = 1 if evals is 0 else evals
				print("Total evals for {} {}".format(algo,evals))

				allPercentageBestAllSeed = []
				allPercentageDefaultAllSeed = []

				allMetricBestAllSeed = []
				allMetricDefaultAllSeed = []
				differencesMetricAllSeed = []

				for iseed in range(0,5):
					try:
						print("Analyzing seed {}".format(iseed))
						location = "{}/SummaryResults/{}/{}/dataset_{}/algorithm_{}/seed_{}/fractionds_{}/"
						location = location.format(RESULTS_PROCESSED_LOCATION, parentMethodFolder, childMethodFolder, modelToAnalyze, algo, iseed, fraction)

						print("\nBest: ")
						pathBest = "{}/{}_{}_bestConfigsPerformance_{}_evals_{}_f_{}_{}.csv".format(location,childMethodFolder,
																											 modelToAnalyze,
																											 algo, evals,
																											 fraction, "avg" if useAvg else "median")
						pathDefault = "{}/{}_{}_defaultConfigsPerformance_{}_evals_{}_f_{}_{}.csv".format(location,childMethodFolder,
																											 modelToAnalyze,
																											 algo, evals,
																											 fraction, "avg" if useAvg else "median")

						allPercentageBest, allPercentageDefault, allMetricBest, allMetricDefault, allConfigurationBestFound,differencesMetric = analyzeResultsHyperop2(
							pathDistances="{}/distance_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION, modelToAnalyze,
																				  algo),
							pathSize="{}/editscript_size_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION, modelToAnalyze,
																					algo),
							pathBest=pathBest,pathDefault=pathDefault, algo=algo, model=modelName, seed=iseed
						)

						print("all config found {}".format(Counter(allConfigurationBestFound)))

						allPercentageBestAllSeed.extend(allPercentageBest)
						allPercentageDefaultAllSeed.extend(allPercentageDefault)

						allMetricBestAllSeed.extend(allMetricBest)
						allMetricDefaultAllSeed.extend(allMetricDefault)

						differencesMetricAllSeed.extend(differencesMetric)

						print("----")
					except Exception as ex:
						print("Error")
						print(ex)

				print("Summarizing results all seeds:")
				print("Percentages: ")
				meanPercentageBest = np.mean(allPercentageBestAllSeed)
				print("{} {} Best & {:.2f}\%  (st {:.2f})".format(modelName, algo, meanPercentageBest * 100,
																  np.std(allPercentageBestAllSeed) * 100))
				meanPercentageDefault = np.mean(allPercentageDefaultAllSeed)
				print("{} {} Default & {:.2f}\%  (st {:.2f})".format(modelName, algo, meanPercentageDefault * 100,
																	 np.std(allPercentageDefaultAllSeed) * 100))

				print("{} {} Difference & {:.2f}\%  ".format(modelName, algo, (meanPercentageBest - meanPercentageDefault )* 100))

				print("Metrics: ")
				meanMetricBest = np.mean(allMetricBestAllSeed)
				print("{} {} Best & {:.2f}  (st {:.2f})".format(modelName, algo,
																  meanMetricBest ,
																  np.std(allMetricBestAllSeed) ))
				meanMetricDefault = np.mean(allMetricDefaultAllSeed)
				print("{} {} Default & {:.2f}  (st {:.2f})".format(modelName, algo,
																	 meanMetricDefault,
																	 np.std(allMetricDefaultAllSeed)))

				print("{} {} Difference & {:.2f}\%  ".format(modelName, algo,
															 ((meanMetricDefault - meanMetricBest)/meanMetricDefault) * 100))

				meanDifferencesMetric = np.mean(differencesMetricAllSeed)
				print("{} {} Difference from mean & {:.2f}\%  ".format(modelName, algo,
															 ((meanDifferencesMetric) / meanMetricDefault) * 100))
				print("latex:  & {:.2f}\% & {:.2f}\% & {:.2f}\%".format(
					float(meanPercentageBest * 100),
					(meanPercentageBest - meanPercentageDefault )* 100,
					(meanDifferencesMetric / meanMetricDefault) * 100))

	'''this test computes the differences between using AVG and Median'''
	def _testAnalyzeHyperopResultsCompareAvgMediam(self):
		evals = 200#1000
		fraction = 1
		parentMethodFolder = None
		childMethodFolder = None

		isTPE = True

		if isTPE:
			parentMethodFolder = "TPE"
			childMethodFolder = "hyper_op"
		else:
			parentMethodFolder = "random"
			childMethodFolder = "random_op"

		for modelToAnalyze in [NAME_FOLDER_ASTJDT, NAME_FOLDER_ASTSPOON]:
			for algo in ["Gumtree"#, "ChangeDistiller", "XyMatcher"
						 ]:
				iseed = 10
				allPercentageBest = []
				allPercentageDefault = []

				bestFromAvg = []
				bestFromMedian = []
				for useAvg in [True, False]:
					location = "{}/{}/{}/dataset_{}/algorithm_{}/seed_{}/fractionds_{}/".format(RESULTS_PROCESSED_LOCATION,parentMethodFolder,childMethodFolder, modelToAnalyze, algo,iseed,fraction)
					#/TPE/hyper_op/dataset_merge_out4gt_outCD/algorithm_Gumtree/seed_3/fractionds_1/hyper_op_merge_out4gt_outCD_defaultConfigsPerformance_Gumtree_evals_1000_f_1.csv

					print("\nBest: ")
					pathBest = "{}/{}_{}_bestConfigsPerformance_{}_evals_{}_f_{}_{}.csv".format(location,childMethodFolder,
																										 modelToAnalyze,
																										 algo, evals,
																										 fraction, "avg" if useAvg else "median")
					pathDefault = "{}/{}_{}_defaultConfigsPerformance_{}_evals_{}_f_{}_{}.csv".format(location,childMethodFolder,
																										 modelToAnalyze,
																										 algo, evals,
																										 fraction, "avg" if useAvg else "median")
					allPercentageBest, allPercentageDefault, allMetricBest, allMetricDefault, allConfigurationBestFound = analyzeResultsHyperop2(
						pathDistances="{}/distance_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION, modelToAnalyze,
																			  algo),
						pathSize="{}/editscript_size_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION, modelToAnalyze,
																				algo),
						pathBest=pathBest,pathDefault=pathDefault, algo=algo, model="JDT" if "JDT" in modelToAnalyze else "Spoon", seed=iseed
					)

					if useAvg:
						bestFromAvg = allPercentageBest
					else:
						bestFromMedian = allPercentageBest

					print("all config found {}".format(Counter(allConfigurationBestFound)))

					print("----")
				print("Now we compare avg {} vs median {} ".format(np.mean(bestFromAvg), np.mean(bestFromMedian)))



if __name__ == '__main__':
	unittest.main()

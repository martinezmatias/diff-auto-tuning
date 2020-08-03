import unittest
from src.rowDataConsumers.ResultsAnalyzers import *
from src.rowDataConsumers.ResultsAnalyzeBestComplete import *
from src.rowDataConsumers.ResultsAnalyzeDiffConfiguration import *
from src.rowDataConsumers.ResultsAnalyzeTimes import *
from src.processedDataConsumers.ResultsReadCheckPositionDefault import *
from src.processedDataConsumers.ResultsHyperOptDAT import *
from src.rowDataConsumers.ResultsAnalyzeRelationTimeSize import *
from src.rowDataConsumers.ResultsCountPairsAnalyzed import *
from src.rowDataConsumers.ResultsCompareASTMetadata import *
from src.processedDataConsumers.ResultGridSearchKfoldValidation import *
from src.commons.DatasetMerger import *
from src.commons.TestStats import *
from src.processedDataConsumers.ResultsCompareDistribution import  *
from SALib.sample import saltelli
from SALib.analyze import sobol
from SALib.test_functions import Ishigami
import numpy as np
from src.processedDataConsumers.plotPerformance import *

class MyTestCase(unittest.TestCase):

	def _test_CompareTwoExecutions(self):

		compareNewExecutedBetweenTwoExecutions("lastindex/lastIndext_GTSPOON_1592923835.802567.json",
											   "lastindex/lastIndext_GTSPOON_1592924043.853812.json")

	def _testSaveIndexLastExecution(self):
		saveExecutionsIndexOfBatches("./results/out5")

	def _test_ParserResults(self):
		plotExecutionTime("./results/out10bis5_4gt/")

	def _test_ComputeFitness(self):
		computeBestConfigurations("../../results/out10bis5_4gt/")

	def _test_ComputeFitnessFast(self):
		#folderToAnalyze = "merge_gt6_cd_5"
		folderToAnalyze = "merge_gtJDT_5_CDJDT_4"
		computeBestConfigurationsFast("../../results/{}/".format(folderToAnalyze), suffix=folderToAnalyze)

	def _test_ComputeFitnessFastTest(self):
		#folderToAnalyze = "merge_gt6_cd_5"
		folderToAnalyze = "merge_gtJDT_5_CDJDT_4"
		computeBestConfigurationsFast("../../results/{}/".format(folderToAnalyze), suffix="{}_test".format(folderToAnalyze), key="Gumtree")

	def _test_ComputeFitnessFastPerAlgorithm(self):

		for folderToAnalyze in ["merge_gtJDT_5_CDJDT_4",  "merge_gt6_cd_5"]:
			for algorithm in ["Gumtree", "ChangeDistiller", "XyMatcher"]:
				computeBestConfigurationsFast("../../results/{}/".format(folderToAnalyze), suffix="{}_{}".format(folderToAnalyze,algorithm), key = algorithm)



	def _test_ComputeBestKFoldComplete(self):
		for folderToAnalyze in [#"merge_gtJDT_5_CDJDT_4",
								"merge_gt6_cd_5"]:
			print("\nanalyzing {}".format(folderToAnalyze))
			kvalue = 10
			random_seed_value = 0
			allOptimized = []
			allDefault = []
			optimizedgt, defaultgt,  rp_index,srho_index,pmann_index, pwilcoxon_index, rp_performance,srho_performance,pmann_performance,pwilcoxon_performance = computeGridSearchKFold("../../plots/data/distance_per_diff_{}_Gumtree.csv".format(folderToAnalyze), kFold=kvalue, algorithm="Gumtree", defaultId="ClassicGumtree_0.5_1000_2", random_seed=random_seed_value, datasetname=folderToAnalyze)
			allOptimized.append(optimizedgt)
			allDefault.append(defaultgt)

			optimizedcd, defaultcd,  rp_index,srho_index,pmann_index, pwilcoxon_index, rp_performance,srho_performance,pmann_performance,pwilcoxon_performance =computeGridSearchKFold("../../plots/data/distance_per_diff_{}_ChangeDistiller.csv".format(folderToAnalyze), kFold=kvalue,   algorithm="ChangeDistiller", defaultId="ChangeDistiller_0.5_4_0.6_0.4", random_seed=random_seed_value, datasetname=folderToAnalyze)
			allOptimized.append(optimizedcd)
			allDefault.append(defaultcd)

			optimizedxy, defaultxy,  rp_index,srho_index,pmann_index, pwilcoxon_index, rp_performance,srho_performance,pmann_performance,pwilcoxon_performance =computeGridSearchKFold("../../plots/data/distance_per_diff_{}_Xy.csv".format(folderToAnalyze), kFold=kvalue,	algorithm="Xy", defaultId="XyMatcher_2_0.5", random_seed=random_seed_value, datasetname=folderToAnalyze)
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

	def _test_ComputeBestKFoldSingle(self):
		folderToAnalyze = "merge_gt6_cd_5"
		kvalue = 2

		#optimized, default,  rp_index,srho_index,pmann_index, pwilcoxon_index, rp_performance,srho_performance,pmann_performance,pwilcoxon_performance =\
		computeGridSearchKFold("../../plots/data/distance_per_diff_{}_ChangeDistiller.csv".format(folderToAnalyze), kFold=kvalue,   algorithm="ChangeDistiller", defaultId="ChangeDistiller_0.5_4_0.6_0.4", random_seed=0,  datasetname=folderToAnalyze)


	def _test_CompteHyperOpt_range(self):
		evals_range = [1, 5, 10, 20, 50, 100, 200, 500] #[10, 20, 50, 100, 200, 500]
		ratio = [0.001, 0.0025, 0.005, 0.0001,0.00025] #[0.01, 0.025, 0.05, 0.1, 0.2, 0.5]
		for i_eval in evals_range:
			kfold = 10
			for i_ratio in ratio:
				for folderToAnalyze in ["merge_gtJDT_5_CDJDT_4",
										"merge_gt6_cd_5"]:
					for algorithm in ["Gumtree", "ChangeDistiller",
									  "XyMatcher"]:
						print("\nanalyzing {}".format(folderToAnalyze))
						computeHyperOpt("../../plots/data/distance_per_diff_{}_{}.csv".format(folderToAnalyze, algorithm), kFold=kfold, max_evals=i_eval,fractiondata= i_ratio,  dataset = folderToAnalyze, algorithm = algorithm)

	def _test_CompteHyperOpt_single_by_algo(self):
			''''only 1000 '''''
			kfold = 10
			for folderToAnalyze in ["merge_gtJDT_5_CDJDT_4",
									"merge_gt6_cd_5"]:
				for algorithm in ["Gumtree", "ChangeDistiller",
								  "XyMatcher"]:
					print("\nanalyzing {}".format(folderToAnalyze))
					computeHyperOpt("../../plots/data/distance_per_diff_{}_{}.csv".format(folderToAnalyze, algorithm), kFold=kfold, max_evals=1000,fractiondata= 1,  dataset = folderToAnalyze, algorithm = algorithm)

	def _test_CompteHyperOpt_single(self):
			''''only 1000 '''''
			kfold = 10
			for folderToAnalyze in ["merge_gtJDT_5_CDJDT_4",
									"merge_gt6_cd_5"]:

					print("\nanalyzing {}".format(folderToAnalyze))
					computeHyperOpt("../../plots/data/distance_per_diff_{}.csv".format(folderToAnalyze), kFold=kfold, max_evals=1000,fractiondata= 1,  dataset = folderToAnalyze, algorithm = None)


	def test_Evolution_HyperOpt(self):
		evals_range = [10, 20, 50, 100, 200]
		ratio = [0.01, 0.025, 0.05, 0.1, 0.2, 0.5]
		totalAlgorithm = {}
		timeAlgorithm = {}

		totalAlgorithm["Gumtree"] = 2050
		totalAlgorithm["ChangeDistiller"] = 375
		totalAlgorithm["XyMatcher"] = 50
		totalExecuted = 40000

		##In seconds
		timeAlgorithm["Gumtree"] = 0.5
		timeAlgorithm["ChangeDistiller"] = 1
		timeAlgorithm["XyMatcher"] = 0.01

		for folderToAnalyze in ["merge_gtJDT_5_CDJDT_4",
										"merge_gt6_cd_5"]:
			print("\nanalyzing {}".format(folderToAnalyze))

			for algorithm in ["Gumtree", "ChangeDistiller",
									  "XyMatcher"]:
						print("\nanalyzing {} {}".format(algorithm, [ int(x * totalExecuted) for x in ratio ] ))

						timesOfAlgo = []
						performancesOfAlgo = []

						for i_eval in evals_range:
							rations_collected = []
							for i_ratio in ratio:
								totaldiffexecuted = int(i_ratio * totalExecuted) * i_eval
								#computeHyperOpt("../../plots/data/distance_per_diff_{}_{}.csv".format(folderToAnalyze, algorithm), kFold=kfold, max_evals=i_eval,fractiondata= i_ratio,  dataset = folderToAnalyze, algorithm = algorithm)
								path ="../../plots/data/hyper_op_{}_{}_evals_{}_f_{}.csv".format(folderToAnalyze, algorithm, i_eval, i_ratio)
								performances = readCSVToFloatList(path, indexToKeep=3)
								avgperformance = np.mean(performances)
								rations_collected.append(avgperformance)

								time = totaldiffexecuted * timeAlgorithm[algorithm]
								timesOfAlgo.append(time)
								performancesOfAlgo.append(avgperformance)

							print("Ratios collected algo {} eval {} avg {} ".format(algorithm, i_eval, rations_collected))

						plt.scatter(timesOfAlgo,performancesOfAlgo)
						plt.show()

	def _test_AnalyzeTimeSize(self):
		analyzeTimeSize("./results/out10bis5_4gt/")

	def _test_AnalyzeTimeSize(self):
		getPositionDefalt(fileLocation="../../results/summary/best_configurations_summary.csv")

	def _test_CompareDistributionBestDefault(self):
		folderToAnalyze = "merge_gt6_cd_5"
		compareDistributions(pathResults="../../plots/data/distance_per_diff_{}.csv".format(folderToAnalyze), keyBestConfiguration="ClassicGumtree_0.1_2000_1", keyDefaultConfiguration="ClassicGumtree_0.5_1000_2")

	def _test_countAnalyzed(self):
		countFilePairsAnalyzed("../../results/outCDJDT_4/")
		countFilePairsAnalyzed("../../results/outCD_5/")
		countFilePairsAnalyzed("../../results/outgtJDT_5/")
		countFilePairsAnalyzed("../../results/outgt6/")

	def _test_compareASTModel(self):
		m1 = retrieveTreeMetric("../../results/out10bis5_4gt/", useSize=True)
		m2 = retrieveTreeMetric ("../../results/out4gtJDT_3/", useSize=True)

		compareTwoSamples(m1, m2, "size")


	def _test_compareASTModelHeigth(self):
		m1 = retrieveTreeMetric("../../results/out10bis5_4gt/", useSize=False)
		m2 = retrieveTreeMetric ("../../results/out4gtJDT_3/", useSize=False)

		compareTwoSamples(m1, m2, "heigth")

	def _test_mergedatasetsSpoon(self):
		merge(location1="/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/results/outgt6/",
			location2="/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/results/outCD_5",
			destination="/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/results/merge_gt6_cd_5")

	def _test_mergedatasetsJDT(self):
		merge(location1="/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/results/outgtJDT_5/",
			location2="/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/results/outCDJDT_4",
			destination="/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/results/merge_gtJDT_5_CDJDT_4")

	def _test_plot(self):
		import matplotlib.pyplot as plt

		np.random.seed(10)
		collectn_1 = np.random.normal(100, 10, 200)
		collectn_2 = np.random.normal(80, 30, 200)
		collectn_3 = np.random.normal(90, 20, 200)
		collectn_4 = np.random.normal(70, 25, 200)

		## combine these different collections into a list
		data_to_plot = [collectn_1, collectn_2, collectn_3, collectn_4]

		# Create a figure instance
		fig = plt.figure()

		# Create an axes instance
		ax = fig.add_axes([0, 0, 1, 1])
		ax.set_xticks(np.arange(1, 4 + 1))
		# Create the boxplot
		bp = ax.violinplot(data_to_plot)
		ax.set_xticklabels(["A","B", "C", "D"])
		plt.show()

	def _test_ParserResultsParallell(self):
		plotExecutionTime("../../results/executions/out_parallel_4/")

	def _testPlot(self):
		timesCompleteGroupId40 = [4701, 9565, 24477, 27142, 663751, 4120782, 54625, 9006, 470009, 1320863, 586, 1401150, 22043, 166708, 179,
		 13694, 1517, 16224, 95672, 962169, 8761, 708008, 349076, 248671, 110545, 4864, 561955, 83019, 6660, 19146,
		 1295466, 904560, 33170, 15288, 4703, 2729, 44965, 9099, 44, 462473, 644, 708083, 370039, 695808, 1642650, 7315,
		 94927, 411081, 4497, 166655, 10151, 29785, 786218, 477519, 517819, 55184, 3558417, 19128, 26708, 231891,
		 572392, 1056382, 27056, 532789, 3760, 908755, 8038]

		print(timesCompleteGroupId40)
		fig, ax = plt.subplots()
		ax.violinplot(timesCompleteGroupId40,
					  showmedians=True, showmeans=True, vert=True
					  #fliers=False
					  )
		plt.show()

	def _testSensitivyanalysis(self):
		#https://salib.readthedocs.io/en/latest/getting-started.html#installing-salib

		# Define the model inputs
		problem = {
			'num_vars': 3,
			'names': ['x1', 'x2', 'x3'],
			'bounds': [[-3.14159265359, 3.14159265359],
					   [-3.14159265359, 3.14159265359],
					   [-3.14159265359, 3.14159265359]]
		}

		# Generate samples
		param_values = saltelli.sample(problem, 1000)

		# Run model (example)
		Y = Ishigami.evaluate(param_values)

		# Perform analysis
		Si = sobol.analyze(problem, Y, print_to_console=True)

		# Print the first-order sensitivity indices
		print(Si['S1'])

if __name__ == '__main__':
	unittest.main()

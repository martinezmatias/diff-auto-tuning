import unittest
from src.processedDataConsumers.EngineHyperOptDAT import *
from src.commons.Datalocation import *
from src.processedDataConsumers.RQ5_TPELauncher_allAlgo import *
class TestRQ5(unittest.TestCase):


	def _test_CompteHyperOpt_range(self):
		runTPE = True

		for folderToAnalyze in [NAME_FOLDER_ASTSPOON, NAME_FOLDER_ASTJDT]:
						runTPERangeValues(pathResults="{}/distance_per_diff_{}.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze), dataset = folderToAnalyze, runTpe=runTPE )


	''''this method reads the results of hyperopt and plots the evolution'''
	def _test_Study_Evolution_HyperOpt(self):
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
		##TODO not by algo any more
		for folderToAnalyze in [NAME_FOLDER_ASTJDT, NAME_FOLDER_ASTSPOON]:
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
								path ="{}/hyper_op_{}_{}_evals_{}_f_{}.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze, algorithm, i_eval, i_ratio)
								performances = readCSVToFloatList(path, indexToKeep=3)
								avgperformance = np.mean(performances)
								rations_collected.append(avgperformance)

								time = totaldiffexecuted * timeAlgorithm[algorithm]
								timesOfAlgo.append(time)
								performancesOfAlgo.append(avgperformance)

							print("Ratios collected algo {} eval {} avg {} ".format(algorithm, i_eval, rations_collected))

						#plt.scatter(timesOfAlgo,performancesOfAlgo)
						#plt.show()



if __name__ == '__main__':
	unittest.main()
import unittest
from src.rowDataConsumers.ResultsAnalyzers import *
from src.rowDataConsumers.ResultsAnalyzeBest import *
from src.rowDataConsumers.ResultsAnalyzeBestFast import *
from src.rowDataConsumers.ResultsAnalyzeTimes import *
from src.processedDataConsumers.ResultsReadCheckPositionDefault import *
from src.rowDataConsumers.ResultsAnalyzeRelationTimeSize import *
from src.processedDataConsumers.ResultKfoldValidation import *
from SALib.sample import saltelli
from SALib.analyze import sobol
from SALib.test_functions import Ishigami
import numpy as np


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
		#computeBestConfigurationsFast("../../results/out10bis5_4gt/")
		computeBestConfigurationsFast("../../results/out4gtJDT_2/")

	def test_ComputeBestKFold(self):
		computeBestConfigurationKFold("../../plots/data/distance_per_diff_GTSpoon.csv",kFold=5)
		#computeBestConfigurationKFold("../../plots/data/distance_per_diff.csv", kFold=2)

	def _test_AnalyzeTimeSize(self):
		analyzeTimeSize("./results/out10bis5_4gt/")

	def _test_AnalyzeTimeSize(self):
		getPositionDefalt(fileLocation="../../results/summary/best_configurations_summary.csv")

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

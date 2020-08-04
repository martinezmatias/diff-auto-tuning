import unittest
from src.rowDataConsumers.ResultsAnalyzers import *
from src.rowDataConsumers.ResultsAnalyzeBestComplete import *
from src.rowDataConsumers.ResultsAnalyzeDiffConfiguration import *
from src.rowDataConsumers.ResultsAnalyzeTimes import *
from src.processedDataConsumers.ResultsReadCheckPositionDefault import *
from src.processedDataConsumers.EngineHyperOptDAT import *
from src.rowDataConsumers.ResultsAnalyzeRelationTimeSize import *
from src.rowDataConsumers.ResultsCountPairsAnalyzed import *
from src.rowDataConsumers.EngineCompareASTMetadata import *
from src.processedDataConsumers.EngineGridSearchKfoldValidation import *
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


	def _test_AnalyzeTimeSize(self):
		analyzeTimeSize("./results/out10bis5_4gt/")

	def _test_AnalyzeTimeSize(self):
		getPositionDefalt(fileLocation="../../results/summary/best_configurations_summary.csv")

	def _test_CompareDistributionBestDefault(self):
		folderToAnalyze = "merge_gt6_cd_5"
		compareDistributions(pathResults="../../plots/data/distance_per_diff_{}.csv".format(folderToAnalyze), keyBestConfiguration="ClassicGumtree_0.1_2000_1", keyDefaultConfiguration="ClassicGumtree_0.5_1000_2")

	def _test_ParserResultsParallell(self):
		plotExecutionTime("../../results/executions/out_parallel_4/")

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

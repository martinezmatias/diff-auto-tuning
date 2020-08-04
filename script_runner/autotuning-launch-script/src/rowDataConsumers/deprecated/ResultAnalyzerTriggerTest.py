import unittest
from src.rowDataConsumers.deprecated.ResultsAnalyzers import *
from src.rowDataConsumers.deprecated.ResultsAnalyzeBestComplete import *
from src.rowDataConsumers.deprecated.ResultsAnalyzeTimes import *
from src.processedDataConsumers.deprecated.ResultsReadCheckPositionDefault import *
from src.rowDataConsumers.deprecated.ResultsAnalyzeRelationTimeSize import *
from SALib.sample import saltelli
from SALib.analyze import sobol
from SALib.test_functions import Ishigami


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

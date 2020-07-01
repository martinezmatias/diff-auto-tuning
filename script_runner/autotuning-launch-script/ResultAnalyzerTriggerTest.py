import unittest
from ResultsAnalyzers import *
from ReadCSV import *

class MyTestCase(unittest.TestCase):

	def _test_CompareTwoExecutions(self):

		compareNewExecutedBetweenTwoExecutions("lastindex/lastIndext_GTSPOON_1592923835.802567.json",
											   "lastindex/lastIndext_GTSPOON_1592924043.853812.json")

	def _testSaveIndexLastExecution(self):
		saveExecutionsIndexOfBatches("./results/out5")

	def test_ParserResults(self):
		parserCSV("./results/out8matcherparallel/")


	def _test_plot(self):
		#parserCSV("./results/out8matcherparallel/")
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
		parserCSV("../../results/executions/out_parallel_4/")
if __name__ == '__main__':
	unittest.main()

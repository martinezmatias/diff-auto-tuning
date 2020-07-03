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
		#parserCSV("./results/out104gt/")

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


if __name__ == '__main__':
	unittest.main()

import os
import re
import pandas
from statistics import mean

CHANGE_DISTILLER = "ChangeDistiller"

CompleteGT = "CompleteGumtreeMatcher"

ClasicGT = "ClassicGumtree"

SimpleGT = "SimpleGumtree"


def initStructure():
	st = []
	for i in range(1, 41):
		st = {}
		st[SimpleGT] = []
		st[ClasicGT] = []
		st[CompleteGT] = []
		st[CHANGE_DISTILLER] = []



def parserCSV(rootResults):

	files = os.listdir(rootResults)
	totalDiffAnalyzed = 0

	timesoutByGroup = {}
	timesByGroup = {}
	diffAnalyzedByGroup = {}
	problems = []

	executionTimePerDiffAlgorith = {}

	for groupId in files:

		if groupId == ".DS_Store":
			continue

		filesGroup = os.path.join(rootResults, groupId)

		if not os.path.isdir(filesGroup):
			continue

		timesByGroup[groupId] = []
		timesoutByGroup[groupId] = []
		diffAnalyzedByGroup[groupId] = []
		for diff in os.listdir(filesGroup):

			if not diff.endswith(".csv"):
				continue
			print(diff)
			try:
				csvFile = os.path.join(filesGroup, diff)
				df = pandas.read_csv(csvFile)
				print("file {} nr {}".format(diff, len(df["MATCHER"])))

				times = df["TIME"].to_list()
				timeouts = df["TIMEOUT"].to_list()

				sumTimesOfDiff = sum (filter(lambda x: str(x) != 'nan', times))

				countTimeouts = len(list(filter(lambda x: x == 1, timeouts)))
				timesoutByGroup[groupId].append(countTimeouts)

				print("times {}".format(times))
				print("timeso{}".format(timeouts))
				totalDiffAnalyzed+=1

				timesByGroup[groupId].append(sumTimesOfDiff)

				#### Retrieve algoritm


			except Exception as e:
				print("Problems with {}".format(diff))
				print(e)
				problems.append(diff)


		print("Total diff analyzed {}".format(totalDiffAnalyzed))



	## Let'sum all per group

	for groupId in sorted(timesByGroup.keys(), key=lambda x: int(x)):
			sumid = sum(timesByGroup[groupId])
			avgTimeout = mean(timesoutByGroup[groupId])
			print("avg {}".format(avgTimeout))
			print("gid: {}, sum: miliseconds {}, minutes {}, hours {},  avg timeout {} ".format(groupId, sumid, sumid/60000, sumid/3600000, avgTimeout))




###

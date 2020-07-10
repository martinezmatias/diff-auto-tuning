
import os
from statistics import mean, stdev
import matplotlib.pyplot as plt
from sklearn.metrics import cohen_kappa_score
import numpy as np
from DiffAlgorithmMetadata import *
import pandas

keytimesoutByGroup = "timesoutByGroup";
# sum all the times of a pair diff (sum all configurations)
keyTimePairAnalysisByGroup = "keyTimePairAnalysisByGroup"
keyTimeSingleConfigurationByGroup = "keyTimeSingleConfigurationByGroup"
keydiffAnalyzedByGroup = "diffAnalyzedByGroup"
keysuccessfulByGroup = "successfulByGroup";


allkeys = [keytimesoutByGroup, keysuccessfulByGroup, keyTimePairAnalysisByGroup, keydiffAnalyzedByGroup, keyTimeSingleConfigurationByGroup]
#timesByDiffPair = {}

def analyzeTimeSize(rootResults):
	print("Starting analyzing folder {}".format(rootResults))
	files = (os.listdir(rootResults) )
	files = list(filter(lambda x: os.path.isdir(os.path.join(rootResults,x)), files))
	totalDiffAnalyzed = 0

	problems = []


	## Navigate group ids
	for groupId in sorted(files, key= lambda x: int(x), reverse=False):

		if groupId == ".DS_Store":
			continue

		filesGroup = os.path.join(rootResults, groupId)

		if not os.path.isdir(filesGroup):
			continue

		## let's read the diff from csv
		diffFromGroup = 0

		##Navigates diff
		for diff in os.listdir(filesGroup):

			if not diff.endswith(".csv") or diff.startswith("metaInfo"):
				continue
			try:
				csvFile = os.path.join(filesGroup, diff)
				df = pandas.read_csv(csvFile)
				diffFromGroup += 1

				print(diff)

				totalDiffAnalyzed += 1

			except Exception as e:
				print("Problems with {}".format(diff))
				#print(e.with_traceback())
				problems.append(diff)




	print("END")

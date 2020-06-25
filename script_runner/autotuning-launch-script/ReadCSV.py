import os
from statistics import mean, stdev
import matplotlib.pyplot as plt
from sklearn.metrics import cohen_kappa_score

import pandas

CHANGE_DISTILLER = "ChangeDistiller"

CompleteGT = "CompleteGumtreeMatcher"

ClasicGT = "ClassicGumtree"

SimpleGT = "SimpleGumtree"

LIST_DIFF_ALGO = {SimpleGT,ClasicGT, CompleteGT, CHANGE_DISTILLER}

def initStructure():
	st = {}
	for algo in LIST_DIFF_ALGO:
		st[algo] = {}
		initResut(st[algo])
		for i in range(1, 41):
			initGroup(st[algo],str(i))

	return st

keytimesoutByGroup = "timesoutByGroup";
# sum all the times of a pair diff (sum all configurations)
keySumTimesAllConfigOfPairsByGroup = "keySumTimesAllConfigOfPairsByGroup"
keyTimeSingleConfigurationByGroup = "keyTimeSingleConfigurationByGroup"
keydiffAnalyzedByGroup = "diffAnalyzedByGroup"
keysuccessfulByGroup = "successfulByGroup";


allkeys = [keytimesoutByGroup, keysuccessfulByGroup, keySumTimesAllConfigOfPairsByGroup, keydiffAnalyzedByGroup, keyTimeSingleConfigurationByGroup]
timesByDiffPair = {}

def parserCSV(rootResults):

	files = (os.listdir(rootResults) )
	files = list(filter(lambda x: os.path.isdir(os.path.join(rootResults,x)), files))
	totalDiffAnalyzed = 0

	result = {}
	initResut(result)

	problems = []

	resultsAlgoDiff = initStructure()



	for groupId in sorted(files, key= lambda x: int(x)):

		if groupId == ".DS_Store":
			continue

		filesGroup = os.path.join(rootResults, groupId)

		if not os.path.isdir(filesGroup):
			continue

		initGroup(result, groupId)

		## let's read the diff from csv
		diffFromGroup = 0

		for diff in os.listdir(filesGroup):

			if not diff.endswith(".csv"):
				continue
			try:
				csvFile = os.path.join(filesGroup, diff)
				df = pandas.read_csv(csvFile)
				diffFromGroup += 1

				## Store times for all (no filter)

				storeTimes(df, groupId, result, filename=diff)
				for algo in LIST_DIFF_ALGO:
					dfAlgo = df[df["MATCHER"] == algo]
					#storeTimes(dfAlgo, groupId, resultsAlgoDiff[algo], filename=diff)#

				totalDiffAnalyzed += 1

			#### Retrieve algoritm


			except Exception as e:
				print("Problems with {}".format(diff))
				print(e.with_traceback())
				problems.append(diff)



		#end loop diff
		result[keydiffAnalyzedByGroup][groupId] = diffFromGroup
		print("total csv diff of group {}: {}".format(groupId, diffFromGroup))
		print("Total diff analyzed {}".format(totalDiffAnalyzed))
		sumTimes = 0
		for file in sorted(timesByDiffPair.keys(), key= lambda x : timesByDiffPair[x]):
			print(" time {:.2f} min file {} ".format(timesByDiffPair[file]/60000, file ))
			sumTimes += timesByDiffPair[file]
		##Testing
		print("group {} Total time {:.2f} min {:.2f} hr".format(groupId, sumTimes/60000, sumTimes/3600000))
		break
	# end group loop
		#cohen_kappa_score()

	## Let'sum all per group
	printResults(result)


def printResults(result, key = "all"):
	fig, ax = plt.subplots()
	keysGroups  = list(sorted(result[keySumTimesAllConfigOfPairsByGroup].keys(), key=lambda x: int(x)))
	datakeySumTimesOfPairsByGroup = []
	for groupId in keysGroups:
		print("Result group {}".format(groupId))
		## sum times
		datakeySumTimesOfPairsByGroup.append(result[keySumTimesAllConfigOfPairsByGroup][groupId])
		sumTimeAllPairsOfGroup = sum(result[keySumTimesAllConfigOfPairsByGroup][groupId])
		avgTimeAllPairsOfGroup = mean(result[keySumTimesAllConfigOfPairsByGroup][groupId])
		stdTimeAllPairsOfGroup = stdev(result[keySumTimesAllConfigOfPairsByGroup][groupId])
		##	# time by single configuration
		avgTimeSingleConfig = mean(result[keyTimeSingleConfigurationByGroup][groupId])
		stdTimeSingleConfig = stdev(result[keyTimeSingleConfigurationByGroup][groupId])
		## time out:
		avgTimeout = mean(result[keytimesoutByGroup][groupId])
		avgSuccessful = mean(result[keysuccessfulByGroup][groupId])

		## the number of diff is not the number of megadiff's diff folder analyzer: a folder can have several file pairs
		print("gid: {}\n#diff {}\nTotal time group (sum all pairs-configurations of group): minutes {}, hours {}\n"
			  "Avg avg time to run diff pair  minutes {:.2f}, hours {:.2f}, std minutes {:.3f}\n"
			  "Avg Time of Single Conf  seconds {:.2f}, minutes {:.2f}, std secods {:.3f}\n"
			  "avg #timeout {:.2f} avg #successful {}".format(groupId,
															  result[keydiffAnalyzedByGroup][groupId],
															  sumTimeAllPairsOfGroup / 60000,
															  sumTimeAllPairsOfGroup / 3600000,
															  avgTimeAllPairsOfGroup / 60000,
															  #
															  avgTimeAllPairsOfGroup / 3600000,
															  stdTimeAllPairsOfGroup / 60000,
															  avgTimeSingleConfig / 1000, avgTimeSingleConfig / 60000,
															  stdTimeSingleConfig / 1000,
															  avgTimeout, avgSuccessful))

	ax.boxplot(datakeySumTimesOfPairsByGroup)
	ax.set_xticklabels(keysGroups)
	plt.show()

def storeTimes(df, groupId, result, filename = ""):
	times = df["TIME"].to_list()
	timeouts = df["TIMEOUT"].to_list()
	filteredConfigurationTimes = list(filter(lambda x: str(x) != 'nan', times))
	sumTimesOfDiff = sum(filteredConfigurationTimes)
	print("{} #{} sum times config {} , all {} ".format(filename, len(times), sumTimesOfDiff, sorted(filteredConfigurationTimes)))
	avgTimesOfDiff = None if len(filteredConfigurationTimes) == 0 else  mean(filteredConfigurationTimes)
	countTimeouts = len(list(filter(lambda x: x == 1, timeouts)))
	countNotTimeouts = len(list(filter(lambda x: x == 0, timeouts)))
	result[keytimesoutByGroup][groupId].append(countTimeouts)
	result[keysuccessfulByGroup][groupId].append(countNotTimeouts)
	result[keySumTimesAllConfigOfPairsByGroup][groupId].append(sumTimesOfDiff)
	result[keyTimeSingleConfigurationByGroup][groupId].append(avgTimesOfDiff)

	timesByDiffPair[filename] = sumTimesOfDiff


###
def initResut(result = {}):
	for key in allkeys:
		result[key] = {}
	return result

def initGroup(result, groupid):
	for key in allkeys:
		result[key][groupid] = []
	return result
import os
from statistics import mean, stdev
import matplotlib.pyplot as plt
from sklearn.metrics import cohen_kappa_score
import numpy as np


import pandas

CHANGE_DISTILLER = "ChangeDistiller"

CompleteGT = "CompleteGumtreeMatcher"

ClasicGT = "ClassicGumtree"

SimpleGT = "SimpleGumtree"

LIST_DIFF_ALGO = [SimpleGT,ClasicGT, CompleteGT, CHANGE_DISTILLER]

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
					storeTimes(dfAlgo, groupId, resultsAlgoDiff[algo], filename=diff)#

				totalDiffAnalyzed += 1

			#### Retrieve algoritm


			except Exception as e:
				print("Problems with {}".format(diff))
				#print(e.with_traceback())
				problems.append(diff)



		#end loop diff
		result[keydiffAnalyzedByGroup][groupId] = diffFromGroup
		print("total csv diff of group {}: {}".format(groupId, diffFromGroup))
		print("Total diff analyzed {}".format(totalDiffAnalyzed))
		sumTimes = 0
		#for file in sorted(timesByDiffPair.keys(), key= lambda x : timesByDiffPair[x]):
		#	print(" time {:.2f} min file {} ".format(timesByDiffPair[file]/60000, file ))
		#	sumTimes += timesByDiffPair[file]
		##Testing
		print("group {} Total time {:.2f} min {:.2f} hr".format(groupId, sumTimes/60000, sumTimes/3600000))
		#break
	    # end group loop
		#cohen_kappa_score()

	## Let'sum all per group
	printResults(result, "all")

	printSingleConfigTime(resultsAlgoDiff)


def printResults(result, key = "all"):
	fig, ax = plt.subplots()
	keysGroups  = list(sorted(result[keySumTimesAllConfigOfPairsByGroup].keys(), key=lambda x: int(x)))
	# time of each Diff-Pair
	datakeySumTimesOfPairsByGroup = []
	#
	datakeyTimeSingleConfigurationByGroup = []
	for groupId in keysGroups:
		print("Result group {}".format(groupId))
		## sum times
		## let'' pass to minutes

		if len(result[keySumTimesAllConfigOfPairsByGroup][groupId]) < 2:
				print("Not enough data for {} {} ".format(groupId, key))
				continue

		datakeySumTimesOfPairsByGroup.append( [ x/60000 for x in  result[keySumTimesAllConfigOfPairsByGroup][groupId]])
		sumTimeAllPairsOfGroup = sum(result[keySumTimesAllConfigOfPairsByGroup][groupId])
		avgTimeAllPairsOfGroup = mean(result[keySumTimesAllConfigOfPairsByGroup][groupId])
		medianTimeAllPairsOfGroup = np.median(result[keySumTimesAllConfigOfPairsByGroup][groupId])
		stdTimeAllPairsOfGroup = 0 if len(result[keySumTimesAllConfigOfPairsByGroup][groupId]) <= 1 else np.std(result[keySumTimesAllConfigOfPairsByGroup][groupId])
		##	# time by single configuration
		avgTimeSingleConfig = mean(result[keyTimeSingleConfigurationByGroup][groupId])
		stdTimeSingleConfig =  0 if len(result[keyTimeSingleConfigurationByGroup][groupId]) <= 1 else np.std(result[keyTimeSingleConfigurationByGroup][groupId])
		## time out:
		avgTimeout = mean(result[keytimesoutByGroup][groupId])
		avgSuccessful = mean(result[keysuccessfulByGroup][groupId])

		#
		datakeyTimeSingleConfigurationByGroup.append([ x for x in  result[keyTimeSingleConfigurationByGroup][groupId]])


		## the number of diff is not the number of megadiff's diff folder analyzer: a folder can have several file pairs
		print("\n---gid: {}\n#diff {}\nTotal time group (sum all pairs-configurations of group): minutes {}, hours {}\n"
			  "Avg avg time to run diff pair  minutes {:.2f}, hours {:.2f},  median {:.2f} min,  std minutes {:.3f}\n"
			  "Avg Time of Single Conf  seconds {:.2f},  std secods {:.3f}\n"
			  "avg #timeout {:.2f} avg #successful {}".format(groupId,
															  result[keydiffAnalyzedByGroup][groupId],
															  #
															  sumTimeAllPairsOfGroup / 60000,
															  sumTimeAllPairsOfGroup / 3600000,
															  #
															  avgTimeAllPairsOfGroup / 60000,
															  avgTimeAllPairsOfGroup / 3600000,
															  medianTimeAllPairsOfGroup / 60000,
															  stdTimeAllPairsOfGroup / 60000,

															  #
															  avgTimeSingleConfig / 1000,

															  stdTimeSingleConfig / 1000,
															  avgTimeout, avgSuccessful))
		print("group id {} (#{}): {}".format(groupId, len(result[keySumTimesAllConfigOfPairsByGroup][groupId]), result[keySumTimesAllConfigOfPairsByGroup][groupId]))

	print("data diff-pair: {}".format(datakeySumTimesOfPairsByGroup))
	ax.boxplot(datakeySumTimesOfPairsByGroup)
	ax.set_xticklabels(keysGroups)
	plt.title(key)
	plt.ylabel("Time Minutes")
	plt.xlabel("Megadiff group (nr of lines affected)")
	#plt.show()
	plt.savefig("./plots/distribution_diffpair_time_{}.pdf".format(key))

	fig, ax = plt.subplots()
	ax.boxplot(datakeyTimeSingleConfigurationByGroup)
	ax.set_xticklabels(keysGroups)
	plt.title(key)
	plt.ylabel("Time microseconds")
	plt.xlabel("Megadiff group (nr of lines affected)")
	#plt.show()
	plt.savefig("./plots/distribution_singlediffconfig_time_{}.pdf".format(key))


	#print("data config: {}".format(datakeyTimeSingleConfigurationByGroup))
	#fig, ax = plt.subplots()
	#ax.boxplot(datakeyTimeSingleConfigurationByGroup)
	#ax.set_xticklabels(keysGroups)
	#plt.show()

def printSingleConfigTime(resultsAlgoDiff):

	databygroup = {}

	for i in range(1,41):
		databygroup[str(i)] = []

	for algo in LIST_DIFF_ALGO:
		key = algo
		result = resultsAlgoDiff[algo]
		fig, ax = plt.subplots()
		keysGroups  = list(sorted(result[keySumTimesAllConfigOfPairsByGroup].keys(), key=lambda x: int(x)))

		datakeyTimeSingleConfigurationByGroup = []

		for groupId in keysGroups:
			print("Result group {}".format(groupId))
			## sum times
			#if len(result[keySumTimesAllConfigOfPairsByGroup][groupId]) < 2:
			#		print("Not enough data for {} {} ".format(groupId, key))
			#		continue
			timesAlgoGroup = [ x for x in  result[keyTimeSingleConfigurationByGroup][groupId]]
			datakeyTimeSingleConfigurationByGroup.append(timesAlgoGroup)
			databygroup[groupId].append(timesAlgoGroup)

		fig, ax = plt.subplots()
		ax.boxplot(datakeyTimeSingleConfigurationByGroup)
		ax.set_xticklabels(keysGroups)
		plt.title(key)
		plt.ylabel("Time microseconds")
		plt.xlabel("Megadiff group (nr of lines affected)")
		fig.set_size_inches(10, 6)
		#plt.show()
		plt.savefig("./plots/distribution_singlediffconfig_time_{}.pdf".format(key))




	##
	for i in range(1,41):
		dataGroup = databygroup[str(i)]
		fig, ax = plt.subplots()
		ax.boxplot(dataGroup)
		ax.set_xticklabels(LIST_DIFF_ALGO)
		plt.title("Megadiff group {}".format(str(i)))
		plt.ylabel("Time microseconds")
		plt.xlabel("Diff Algorithm")
		fig.set_size_inches(8, 8)
		# plt.show()
		plt.savefig("./plots/distribution_groupalgorithm_{}.pdf".format(i))
		plt.close()


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
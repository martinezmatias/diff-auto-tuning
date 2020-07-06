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

XyMatcher = "XyMatcher"

LIST_DIFF_ALGO = [SimpleGT,ClasicGT, CompleteGT, CHANGE_DISTILLER,
				  XyMatcher]

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
keyTimePairAnalysisByGroup = "keyTimePairAnalysisByGroup"
keyTimeSingleConfigurationByGroup = "keyTimeSingleConfigurationByGroup"
keydiffAnalyzedByGroup = "diffAnalyzedByGroup"
keysuccessfulByGroup = "successfulByGroup";


allkeys = [keytimesoutByGroup, keysuccessfulByGroup, keyTimePairAnalysisByGroup, keydiffAnalyzedByGroup, keyTimeSingleConfigurationByGroup]
#timesByDiffPair = {}

def parserCSV(rootResults):
	print("Starting analyzing folder {}".format(rootResults))
	files = (os.listdir(rootResults) )
	files = list(filter(lambda x: os.path.isdir(os.path.join(rootResults,x)), files))
	totalDiffAnalyzed = 0

	result = {}
	initResut(result)

	problems = []

	resultsAlgoDiff = initStructure()

	## Navigate group ids
	for groupId in sorted(files, key= lambda x: int(x), reverse=False):

		if groupId == ".DS_Store":
			continue

		filesGroup = os.path.join(rootResults, groupId)

		if not os.path.isdir(filesGroup):
			continue

		initGroup(result, groupId)

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
				#print("{} diff {}  ".format(diffFromGroup, diff))

				## Store times for all (no filter)

				storeTimes(df, groupId, result, filename=diff)
				for algo in LIST_DIFF_ALGO:
					dfAlgo = df[df["MATCHER"] == algo]
					storeTimes(dfAlgo, groupId, resultsAlgoDiff[algo], filename=diff, key=algo)#

				totalDiffAnalyzed += 1

			except Exception as e:
				print("Problems with {}".format(diff))
				#print(e.with_traceback())
				problems.append(diff)



		#end loop diff
		result[keydiffAnalyzedByGroup][groupId] = diffFromGroup
		print("total csv diff of group {}: {}".format(groupId, diffFromGroup))
		print("Total diff analyzed {}".format(totalDiffAnalyzed))
		sumTimes = 0

		print("group {} Total time {:.2f} min {:.2f} hr".format(groupId, sumTimes/60000, sumTimes/3600000))
		## Testing
		#break

	## Let'sum all per group
	#printResults(result, "all")

	#printSingleConfigTime(resultsAlgoDiff)

	#for algo in LIST_DIFF_ALGO:
	#	printResults(resultsAlgoDiff[algo], key= algo, plot=False)
	print("END")

def printResults(result, key = "all", outliers = True, plot = True, debug = True):
	fig, ax = plt.subplots()
	keysGroups  = list(sorted(result[keyTimePairAnalysisByGroup].keys(), key=lambda x: int(x)))
	###DATA for plots:
	# time of each Diff-Pair
	datakeySumTimesOfPairsByGroup = []
	datakeyTimeSingleConfigurationByGroup = []
	alldj = []
	for groupId in keysGroups:
		## sum times
		## let'' pass to minutes

		if len(result[keyTimePairAnalysisByGroup][groupId]) < 2:
				print("Not enough data for {} {} ".format(groupId, key))
				continue
		if plot:
			##In the plot we show the time of each pair (sum of all its single configurations)
			datakeySumTimesOfPairsByGroup.append([x / 60000 for x in result[keyTimePairAnalysisByGroup][groupId]])
			saveDistributionPlot(datakeySumTimesOfPairsByGroup, "Megadiff group (nr of lines affected)", "Time Minutes", key,
				 keysGroups, "./plots/distribution_diffpair_time_{}.pdf")

			##the second  plot: distribution of single configuration
			datakeyTimeSingleConfigurationByGroup.append([x for x in result[keyTimeSingleConfigurationByGroup][groupId]])

			saveDistributionPlot(datakeyTimeSingleConfigurationByGroup, "Megadiff group (nr of lines affected)", "Time milliseconds", key,
				 keysGroups, "./plots/distribution_singlediffconfig_time_{}.pdf")



		## The total time of ALL pairs
		sumTimeAllPairsOfGroup = sum(result[keyTimePairAnalysisByGroup][groupId])
		## Avg time of each pairs
		avgTimeAllPairsOfGroup = mean(result[keyTimePairAnalysisByGroup][groupId])
		medianTimeAllPairsOfGroup = np.median(result[keyTimePairAnalysisByGroup][groupId])
		stdTimeAllPairsOfGroup = 0 if len(result[keyTimePairAnalysisByGroup][groupId]) <= 1 else np.std(result[keyTimePairAnalysisByGroup][groupId])

		## Configurations
		## Avg of ALL single configuration
		avgTimeSingleConfig = mean(result[keyTimeSingleConfigurationByGroup][groupId])
		stdTimeSingleConfig = 0 if len(result[keyTimeSingleConfigurationByGroup][groupId]) <= 1 else np.std(
				result[keyTimeSingleConfigurationByGroup][groupId])

		avgTimeout = mean(result[keytimesoutByGroup][groupId])
		avgSuccessful = mean(result[keysuccessfulByGroup][groupId])


		dj = {}
		dj["groupid"] = groupId
		dj["nrdiff"] =result[keydiffAnalyzedByGroup][groupId]
		##Sum Pairs
		dj["sum_total_time_group_sec"] = sumTimeAllPairsOfGroup / 1000
		dj["sum_total_time_group_min"] =  sumTimeAllPairsOfGroup / 60000
		dj["sum_total_time_group_hr"] = sumTimeAllPairsOfGroup / 3600000

		## Single Pairs
		dj["avgTimeAllPairsOfGroup_sec"] = avgTimeAllPairsOfGroup / 1000
		dj["avgTimeAllPairsOfGroup_min"] =  avgTimeAllPairsOfGroup / 60000
		dj["avgTimeAllPairsOfGroup_hr"] =  avgTimeAllPairsOfGroup / 3600000

		dj["medianTimeAllPairsOfGroup_sec"] =  medianTimeAllPairsOfGroup / 1000
		dj["stdTimeAllPairsOfGroup_sec"] =  stdTimeAllPairsOfGroup / 1000
		## Single Config
		dj["avgTimeSingleConfig_sec"] = avgTimeSingleConfig / 1000
		dj["stdTimeSingleConfig_sec"] = stdTimeSingleConfig / 1000

		dj["avgNrSuccessfulConfig"] = avgSuccessful
		dj["avgNrTimeoutConfig"] =avgTimeout

		alldj.append(dj)
		## the number of diff is not the number of megadiff's diff folder analyzer: a folder can have several file pairs
		if debug:
			print("\n-{}--gid: {}\n#diff {}\nTotal time group (sum all pairs-configurations of group): minutes {}, hours {}\n"
				  "Avg avg time to run diff pair  minutes {:.2f}, hours {:.2f},  median {:.2f} min,  std minutes {:.3f}\n"
				  "Avg Time of Single Conf  seconds {:.2f},  std secods {:.3f}\n"
				  "avg #timeout {:.2f} avg #successful {}".format(key,groupId,
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
			print("group id {} times pairs (#{}): {}".format(groupId, len(result[keyTimePairAnalysisByGroup][groupId]), result[keyTimePairAnalysisByGroup][groupId]))
			print("group id {} times single config(#{})".format(groupId, len(result[keyTimeSingleConfigurationByGroup][groupId]),
												 #result[keyTimeSingleConfigurationByGroup][groupId]
																))

	import csv

	with open('./plots/execution_times_{}.csv'.format(key), 'w', newline='') as csvfile:
		fieldnames = alldj[0].keys()
		writer = csv.DictWriter(csvfile, fieldnames=fieldnames) #quotechar=""
		writer.writeheader()
		for a in alldj:
			writer.writerow(a)


def saveDistributionPlot(data, xlabel, ylabel, key, legends, filename):
	fig, ax = plt.subplots()
	# ax.boxplot(datakeyTimeSingleConfigurationByGroup, showfliers=False)
	# ax.set_xticklabels(keysGroups)
	ax.violinplot(data, showmedians=True, showmeans=True)
	legend = [""]
	legend.extend(legends)
	ax.set_xticklabels(legend)
	plt.title(key)
	plt.ylabel(ylabel)
	plt.xlabel(xlabel)
	# plt.show()
	plt.savefig(filename.format(key))
	plt.close()

'''Plots the time of single commits by algorithm'''
def printSingleConfigTime(resultsAlgoDiff):

	##each key (group id) has 4 values: one per each algorith
	databygroup = {}

	for i in range(1,41):
		databygroup[str(i)] = []

	for algo in LIST_DIFF_ALGO:
		key = algo
		result = resultsAlgoDiff[algo]
		keysGroups  = list(sorted(result[keyTimePairAnalysisByGroup].keys(), key=lambda x: int(x)))

		datakeyTimeSingleConfigurationByGroup = []

		for groupId in keysGroups:

			timesAlgoGroup = [ x for x in  result[keyTimeSingleConfigurationByGroup][groupId]]
			datakeyTimeSingleConfigurationByGroup.append(timesAlgoGroup)
			databygroup[groupId].append(timesAlgoGroup)


		if (len(datakeyTimeSingleConfigurationByGroup) == 0 or len(datakeyTimeSingleConfigurationByGroup[0])== 0):
			print("No data for matcher {}".format(algo))
			continue

		fig, ax = plt.subplots()
		#ax.boxplot(datakeyTimeSingleConfigurationByGroup,  showfliers=False)
		#ax.set_xticklabels(keysGroups)
		ax.violinplot(datakeyTimeSingleConfigurationByGroup, showmedians=True, showmeans=True)
		legend = [""]
		legend.extend(keysGroups)
		ax.set_xticklabels(legend)
		plt.title(key)
		plt.ylabel("Time milliseconds")
		plt.xlabel("Megadiff group (nr of lines affected)")
		#fig.set_size_inches(10, 6)
		#plt.show()
		plt.savefig("./plots/distribution_singlediffconfig_time_matcher_{}.pdf".format(key))
		plt.close()

	##
	for i in range(1,41):
		dataGroup = databygroup[str(i)]

		fig, ax = plt.subplots()
		#ax.boxplot(dataGroup, showfliers=False)
		ax.violinplot(dataGroup, showmedians=True, showmeans=True, vert=True)# pos, points=20, widths=0.3,
		#		   showmeans=True, showextrema=True, showmedians=True)
		legend = [""] ###
		for a in LIST_DIFF_ALGO:
			#legend.append("")
			legend.append(a.replace("Matcher", ""))
		#legend.extend(LIST_DIFF_ALGO)
		#ax.set_xticklabels(legend)
		#https://stackoverflow.com/questions/33864578/matplotlib-making-labels-for-violin-plots
		ax.set_xticklabels(legend)
		plt.title("Megadiff group {}".format(str(i)))
		plt.ylabel("Time milliseconds")
		plt.xlabel("Diff Algorithm")
		#fig.set_size_inches(8, 8)
		# plt.show()
		plt.savefig("./plots/distribution_singlediffconfig_time_groupid_{}.pdf".format(i))
		plt.close()


def storeTimes(df, groupId, result, filename = "", key = "all"):
	times = df["TIME"].to_list()
	timeouts = df["TIMEOUT"].to_list()
	filteredConfigurationTimes = list(filter(lambda x: str(x) != 'nan', times))
	sumTimesOfDiff = int(sum(filteredConfigurationTimes))
	#print("{}: {} #{} sum times config {} , all {} ".format(key, filename, len(times), sumTimesOfDiff, sorted(filteredConfigurationTimes)))
	#print("{}: {} #{} sum times config {}  ".format(key, filename, len(times), sumTimesOfDiff))

	## We store if there are at least one configuration correctly executed
	if len(filteredConfigurationTimes) > 0:
		#avgTimesOfDiff = None if len(filteredConfigurationTimes) == 0 else  mean(filteredConfigurationTimes)
		#	result[keyTimeSingleConfigurationByGroup][groupId].append(avgTimesOfDiff)
		# We put all the times, not only the Avg as was done previously
		result[keyTimeSingleConfigurationByGroup][groupId].extend(filteredConfigurationTimes)
		result[keyTimePairAnalysisByGroup][groupId].append(sumTimesOfDiff)
	else:
		#print("None result for {} but with timeouts {}".format(key, len(list(filter(lambda x: x == 1, timeouts)))))
		pass

	countTimeouts = len(list(filter(lambda x: x == 1, timeouts)))
	countNotTimeouts = len(list(filter(lambda x: x == 0, timeouts)))
	result[keytimesoutByGroup][groupId].append(countTimeouts)
	result[keysuccessfulByGroup][groupId].append(countNotTimeouts)

###
def initResut(result = {}):
	for key in allkeys:
		result[key] = {}
	return result

def initGroup(result, groupid):
	for key in allkeys:
		result[key][groupid] = []
	return result


def computeFitnesss(rootResults):

		files = (os.listdir(rootResults))
		files = list(filter(lambda x: os.path.isdir(os.path.join(rootResults, x)), files))
		totalDiffAnalyzed = 0

		results = {}
		entropy = {}
		overlap = {}
		#initResut(results)

		problems = []

		resultsAlgoDiff = initStructure()

		## Navigate group ids
		for groupId in sorted(files, key=lambda x: int(x)):

			if groupId == ".DS_Store":
				continue

			filesGroup = os.path.join(rootResults, groupId)

			if not os.path.isdir(filesGroup):
				continue

			#nitGroup(results, groupId)

			## let's read the diff from csv
			diffFromGroup = 0

			##Navigates diff
			listdir = os.listdir(filesGroup)
			for diff in listdir:
				if not diff.endswith(".csv") or diff.startswith("metaInfo"):
					continue
				try:
					print("groupid {} file {} /{}  total analyzed: {}".format(groupId,diffFromGroup, len(listdir)/2,totalDiffAnalyzed ))
					csvFile = os.path.join(filesGroup, diff)
					df = pandas.read_csv(csvFile)
					diffFromGroup += 1
					computeFitnessOfFilePair(results,diff, df, overlap=overlap, entropyByFileName=entropy)

					totalDiffAnalyzed += 1

				except Exception as e:
					print("Problems with {}".format(diff))
					print(e.with_traceback())
					problems.append(diff)

			## test
			break

		printBest(results, overlap, entropy, limitTop=1000)


propertiesPerMatcher = {}
propertiesPerMatcher["SimpleGumtree"] = ["GT_BUM_SMT_SBUP", "GT_STM_MH"]
propertiesPerMatcher["ClassicGumtree"] = ["GT_BUM_SMT", "GT_BUM_SZT", "GT_STM_MH"]
propertiesPerMatcher["CompleteGumtreeMatcher"] = ["GT_BUM_SMT", "GT_BUM_SZT", "GT_STM_MH"]
propertiesPerMatcher["ChangeDistiller"] = ["GT_CD_LSIM", "GT_CD_ML","GT_CD_SSIM1",  "GT_CD_SSIM2"]
propertiesPerMatcher["XyMatcher"] = ["GT_STM_MH", "GT_XYM_SIM"]

# for each configuration, we store a dict where key are distance, values ocurrences

#best = {}

def computeFitnessOfFilePair(results, filename,datasetofPair, key = "all", entropyByFileName = {}, overlap = []):
	## Calculate the distance for each comfig
	## store the distance
	## store if it's unique
	## store entropy
	#occurrences = collections.Counter(datasetofPair["NRACTIONS"])
	import pandas as pd
	import scipy.stats

	nractions = datasetofPair["NRACTIONS"]
	pd_series = pd.Series(nractions)
	counts = pd_series.value_counts()
	entropy = scipy.stats.entropy(counts)

	#Take the min value of edit script size
	minES = nractions.min(skipna=True)
	#print("\n--{} {}: entropy {} min ES {}".format(filename, key, entropy, minES))
	#ds1 = datasetofPair[datasetofPair["NRACTIONS"] == 2]

	#entropyByFileName[filename] = entropy

	bestOfFile = []
	for rowConfiguration in datasetofPair.iterrows():
		currentNrActions = rowConfiguration[1]['NRACTIONS']
		if(np.isnan(currentNrActions)):
			continue

		distance = int(currentNrActions) - minES

		rowConfigurationKey = getConfigurationkey(rowConfiguration[1])
		if rowConfigurationKey not in results:
			results[rowConfigurationKey] = {}
			overlap[rowConfigurationKey] = {}
			entropyByFileName[rowConfigurationKey] = {}
		incrementOne(results[rowConfigurationKey], distance)

		if distance == 0:
			bestOfFile.append(rowConfigurationKey)

	##let's compute the overlap
	for best in bestOfFile:
		incrementOne(overlap[best], len(bestOfFile))
		incrementOne(entropyByFileName[best], entropy)





def printBest(results, entropy, overlap, limitTop = 1000):
	keySorted = sorted(results.keys(), key=lambda x: (results[x][0] if 0 in results[x] else 0), reverse=True)
	print("Finishing processing")
	top = 0
	for configuration in keySorted:
		nrBest = (results[configuration][0] if 0 in results[configuration] else 0)
		if (nrBest > 0):
			print("{} {} #{} overlap {} ".format(top, configuration, nrBest, plainDict(overlap[configuration])))
		top += 1
		if (top == limitTop):
			break

def plainDict(dic):
	r = []
	for k in dic.key():
		nr = dic[k]
		for i in range(1, nr + 1):
			r.append(k)

	return r

def incrementOne(dict, key, value = 1):
	if key not in dict:
		dict[key] = value
	else:
		dict[key] += value

def showFinalResult():
	for config in results.keys():
		best = len(results[config])


def getConfigurationkey(row):
		matcherName = row['MATCHER']
		key = matcherName;
		for property in propertiesPerMatcher[matcherName]:
			key+="_"+"{:.1f}".format((row[property])).rstrip('0').rstrip('.')

		return key

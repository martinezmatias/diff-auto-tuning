import os
import re
import pandas

'''given an group id, return the max number, so the process can start from there'''
def getMaxAnalyzed(rootResults, groupId, model = "GTSPOON"):
	filesGroup = os.path.join(rootResults, groupId)

	if not os.path.isdir(filesGroup):
		return groupId, None
	maxid = 0
	for diff in os.listdir(filesGroup):

		if model not in diff:
			continue
		#	print(diff)
		keyfiles = int(re.split('_+', diff)[1])
		#	print(keyfiles)
		if keyfiles > maxid:
			maxid = keyfiles


	return groupId, maxid

def parser(rootResults):

	files = os.listdir(rootResults)

	for groupId in files:

		if groupId == ".DS_Store":
			continue

		groupId, maxid = getMaxAnalyzed(rootResults, groupId)

		print("group id {}  max {} ".format(groupId, maxid))


def parserCSV(rootResults):

	files = os.listdir(rootResults)
	totalDiffAnalyzed = 0

	timesByGroup = {}
	problems = []
	for groupId in files:

		if groupId == ".DS_Store":
			continue

		filesGroup = os.path.join(rootResults, groupId)

		if not os.path.isdir(filesGroup):
			continue

		timesByGroup[groupId] = []
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
				sumTimes = sum (filter(lambda x: str(x) != 'nan', times))
				print("times {}".format(times))
				print("timeso{}".format(timeouts))
				totalDiffAnalyzed+=1

				timesByGroup[groupId].append(sumTimes)
			except:
				print("Problems with {}".format(diff))
				problems.append(diff)


		print("Total diff analyzed {}".format(totalDiffAnalyzed))



	## Let'sum all per group

	for groupId in timesByGroup:
			sumid = sum(timesByGroup[groupId])
			print("gid: {} sum miliseconds {} minutes {} hours".format(groupId, sumid, sumid/60000), sumid/3600000)



###
#parserCSV("/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/results/results/out-gtspoon/")
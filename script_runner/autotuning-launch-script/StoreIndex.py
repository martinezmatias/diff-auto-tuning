import os
import re
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
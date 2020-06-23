from StoreIndex import *
import os
import json
import time
out = "out"
'''Saves the last indexes of the folder out in a json file'''
def saveExecutionsIndexOfBatches(out, model = "GTSPOON"):
	results = {}
	for i in range(1, 41):
		groupId, maxid = getMaxAnalyzed(out, str(i), model)
		print(" groupId {} , maxid {} ".format(groupId, maxid))
		results[groupId] = maxid

	with open('./lastindex/lastIndext_{}_{}.json'.format(model, time.time()), 'w') as outfile:
		json.dump(results, outfile)
		print("saving at {}".format(outfile.name))

'''gives the number of new diff executed between two experiments '''
def compareNewExecutedBetweenTwoExecutions(exec1, exec2):
	data1 = None
	data2 = None

	with open(exec1) as f1:
		data1 = json.load(f1)

	with open(exec2) as f2:
		data2 = json.load(f2)

	print(data1)
	#print(data2)

	differences = {}
	maxsecond = 0

	for i in range(1, 41):
		ki = str(i)
		differences[ki] = data2[ki] - data1[ki]

		if  int(differences[ki]) >=  int(data1[ki]):
			maxsecond+=1

	print(differences)
	print(maxsecond)


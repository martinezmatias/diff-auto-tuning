import os
from statistics import mean, stdev
import matplotlib.pyplot as plt
from src.commons.MetaDataReader import *
import numpy as np
import pandas
from src.commons.DiffAlgorithmMetadata import *
import pandas as pd
import scipy.stats
from src.commons.Utils import *
from src.commons.Datalocation import *

indexesOfColumns = {}
indexOfConfig = {}
orderOfConfiguration = []

'''Compute the fitness of all the data given as parameter'''


def countFilePairsAnalyzed(rootResults):

		print("Analyze dataset {}".format(rootResults))
		files = (os.listdir(rootResults))
		files = list(filter(lambda x: os.path.isdir(os.path.join(rootResults, x)), files))
		totalDiffAnalyzed = 0

		# map where the key is the distance, value is the nr of ocurrences

		## Navigate group ids
		for groupId in sorted(files, key=lambda x: int(x)):

			if groupId == ".DS_Store":
				continue

			filesGroup = os.path.join(rootResults, groupId)

			if not os.path.isdir(filesGroup):
				continue

			## let's read the diff from csv
			diffFromGroup = 0

			##Navigates diff
			listdir = os.listdir(filesGroup)
			for diff in listdir:
				if not diff.endswith(".csv") or diff.startswith("metaInfo"):
					continue
				try:

					diffFromGroup += 1
					totalDiffAnalyzed += 1


				except Exception as e:
					print("Problems with {}".format(diff))
					print(e.with_traceback())

			print("ID {} {}".format(groupId,diffFromGroup ))

		print("Total {}".format(totalDiffAnalyzed))


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
import pandas
import scipy.stats
import matplotlib.pyplot as plt
import numpy as np
from src.commons.CohenEffectSize import *
from scipy.stats import wilcoxon, kruskal
indexesOfColumns = {}
indexOfConfig = {}
orderOfConfiguration = []

'''Compute the fitness of all the data given as parameter'''


def retrieveTreeMetric(rootResults, useSize = True):

		metricPerDiff = {}

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

					size, height = getTreeMetricsFromFile(filesGroup, diff)
					key = diff.replace("GTSPOON", "").replace("JDT", "")
					if useSize:

						metricPerDiff[key] = size
					else:
						metricPerDiff[key] = height

					diffFromGroup += 1
					totalDiffAnalyzed += 1


				except Exception as e:
					print("Problems with {}".format(diff))
					print(e.with_traceback())

			print("ID {} {}".format(groupId,diffFromGroup ))

		print("Total {}".format(totalDiffAnalyzed))
		return metricPerDiff


def compareTwoSamples(sample1 = {} , sample2 = {}, name =""):

	## No pairs
	values1 = list(sample1.values())

	values2 = list(sample2.values())


	print("Size values1 {} size values2 {}".format(len(values1), len(values2)))

	print("V1 mean {} std {}".format(mean(values1), stdev(values1)))
	print("V2 mean {} std {}".format(mean(values2), stdev(values2)))

	stat, p = scipy.stats.mannwhitneyu(values1, values2)

	print(' mannwhitneyu stat=%.3f, p=%.3f' % (stat, p))

	d = cohend(values1, values2)
	print('Cohens d: %.3f' % d)


	# https://pingouin-stats.org/index.html
	import pingouin as pg
	print("normality v1: {}".format(pg.normality(values1)))
	print("normality v2: {}".format(pg.normality(values2)))

	# print(pg.multivariate_normality(values1))
	# https://pingouin-stats.org/api.html#effect-sizes
	print("eff size % f" % pg.compute_effsize(values1, values2))
	# https://pingouin-stats.org/generated/pingouin.mwu.html#pingouin.mwu
	stats = pg.mwu(values1, values2, tail='two-sided')
	print("pingouin MWU:\n {}".format(stats))
	# https://pingouin-stats.org/generated/pingouin.wilcoxon.html?highlight=wilcoxon

	print("\nend")
	##https://cran.r-project.org/web/packages/statsExpressions/vignettes/stats_details.html
	pvalues1 = []
	pvalues2 = []
	#https://cran.r-project.org/web/packages/statsExpressions/vignettes/stats_details.html
	for k1 in sample1.keys():
		if k1 in sample2.keys():
			pvalues1.append(sample1[k1])
			pvalues2.append(sample2[k1])

	print("Size paired: {}".format(len(pvalues1)))

	stats = pg.wilcoxon(pvalues1, pvalues2, tail='two-sided')
	print("pingouin wilcoxon:\n {}".format(stats))


	stat, p = wilcoxon(pvalues1, pvalues2)

	print(' wilcoxon stat=%.3f, p=%.3f' % (stat, p))

	stat, p = kruskal(pvalues1, pvalues2)

	print(' kruskal stat=%.3f, p=%.3f' % (stat, p))

	saveFile("../../plots/data/paired_values_{}_1.csv".format(name), pvalues1)
	saveFile("../../plots/data/paired_values_{}_2.csv".format(name), pvalues2)

def saveFile(filename, pvalues1):
	fout1 = open(filename, 'w')
	for i in pvalues1:
		fout1.write("{}\n".format(i))
	fout1.flush()
	fout1.close()

##

import csv

import scipy.stats

from DiffAlgorithmMetadata import *
import numpy as np
import matplotlib.pyplot as plt

##https://stats.idre.ucla.edu/spss/whatstat/what-statistical-analysis-should-i-usestatistical-analyses-using-spss/

def getTimeXBestConfig(fileLocation = "./plots/data/best_configurations_summary.csv", out = "./plots/data/", useAverage = False, prefixAlgorithmName = None):
	configs = list(defaultConfigurations.values())
	print(configs)

	x = []
	xTicks = []
	yTimeBest = []
	yTimeNotBest = []


	with open(fileLocation, mode='r') as csv_file:
		csv_reader = csv.DictReader(csv_file)
		line_count = 0
		for row in csv_reader:
			configuration = row["configuration"]

			## we ignore those configs that do not start with a prefix
			if prefixAlgorithmName is not None and not configuration.startswith(prefixAlgorithmName):
				continue

			x.append(int(line_count))
			xTicks.append(configuration)
			timeBest = row["best_time_avg" if useAverage else "best_time_median"]
			timeNotBest = row["notbest_time_avg"  if useAverage else "notbest_time_median"]
			yTimeBest.append(float(timeBest))
			yTimeNotBest.append(float(timeNotBest))

			line_count += 1

	print("\n---Results: # datapoints {}".format(len(x)))

	if(len(x) == 0):
		print("Not analyzed {}".format(prefixAlgorithmName))
		return

	print("x {} {} ".format(len(x), x))
	print("y best {} {} ".format(len(yTimeBest), yTimeBest))
	print("y not best {} {} ".format(len(yTimeNotBest), yTimeNotBest))
	plt.plot(x, yTimeBest, color='green', marker='o', linestyle='dashed',linewidth = 1, markersize = 1, label='Best Configuration')
	plt.plot(x, yTimeNotBest, color='red', marker='x', linestyle='dashed', linewidth=1, markersize=1, label='Not Best Configuration')
	plt.ylabel("Time (Milliseconds)")
	plt.xlabel("Best from left to right")
	plt.xticks(np.arange(len(xTicks)), xTicks, rotation=90, fontsize=4)
	plt.title(prefixAlgorithmName if prefixAlgorithmName is not None else "" )
	plt.savefig("{}/plot_time_best_{}_{}.pdf".format(out, ("avg" if useAverage else "median"), prefixAlgorithmName if prefixAlgorithmName is not None else "all" ))
	plt.close()

	print("Pearson's r {} ".format(scipy.stats.pearsonr(x, yTimeBest)))

	print("Spearman's rho {} ".format(scipy.stats.spearmanr(x, yTimeBest)))

	print("Kendall's tau {} ".format(scipy.stats.kendalltau(x, yTimeBest)))  # Pearson's r

	from scipy import stats
	slope, intercept, r_value, p_value, std_err = stats.linregress(x, yTimeBest)
	print("Line Best slope {}, intercept {}, r_value {} , p_value {}, std_err {}".format(slope, intercept, r_value, p_value, std_err))

	slope, intercept, r_value, p_value, std_err = stats.linregress(x, yTimeNotBest)
	print("Line Not Best slope {}, intercept {}, r_value {} , p_value {}, std_err {}".format(slope, intercept, r_value,
																						 p_value, std_err))


##
getTimeXBestConfig(useAverage=True)
getTimeXBestConfig(useAverage=False)

for matcher in  propertiesPerMatcher.keys():
	print("\nanalyzing {}".format(matcher))
	getTimeXBestConfig(useAverage=True, prefixAlgorithmName=matcher)
	getTimeXBestConfig(useAverage=False,prefixAlgorithmName=matcher)

print("End")

import os
from src.commons.MetaDataReader import  *
import pandas
from src.commons.Utils import  *
from src.commons.DiffAlgorithmMetadata import *
import numpy as np
import matplotlib.pyplot as plt



indexesOfColumns = {}
def analyzeTimeSize(rootResults, dirOut ="./plots/data/", plot = False):
	print("Starting analyzing folder {}".format(rootResults))
	files = (os.listdir(rootResults) )
	files = list(filter(lambda x: os.path.isdir(os.path.join(rootResults,x)), files))
	totalDiffAnalyzed = 0

	problems = []

	totalRow = 0
	out = "{}/relationSizeTime/".format(dirOut)
	if not os.path.exists(out):
		os.makedirs(out)

	sizePerConfig = {}
	## Navigate group ids
	for groupId in sorted(files, key= lambda x: int(x), reverse=False):
		print("group {}".format(groupId))
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

				size, height = getTreeMetricsFromFile(filesGroup, diff)


				## for the first call to this method, let's store the columns
				columnsToMap(df, indexesOfPropertiesInTable=indexesOfColumns)

				for rowConfiguration in df.itertuples():
					totalRow += 1
					keyConfiguration = getConfigurationKeyFromCSV(rowConfiguration, indexesOfColumns)

					if keyConfiguration not in sizePerConfig:
						sizePerConfig[keyConfiguration] = []

					sizePerConfig[keyConfiguration].append((size,rowConfiguration.TIME))

				totalDiffAnalyzed += 1

			except Exception as e:
				print("Problems with {}".format(diff))
				#print(e.with_traceback())
				problems.append(diff)

			#break

	csvout = "{}/relation_time_size.csv".format(dirOut)
	frelationtimesize = open(csvout, "w")
	frelationtimesize.write("config,nrpoints, slope,intercept,r_value,p_value,std_err\n")
	for config in sizePerConfig.keys():
		pairs = sizePerConfig[config]
		xSize = []
		yTime = []
		for p in pairs:
			if not np.isnan(p[0]) and not np.isnan(p[1]) :
				xSize.append(p[0])
				yTime.append(p[1])
		#print(config)
		#print("x {}".format(xSize))
		#print("y {}".format(yTime))
		if plot :
			plt.clf()
			plt.plot(xSize, yTime,  'ro' #color='red', marker='x', linestyle='dashed', linewidth=1, markersize=1,
					 #label='Not Best Configuration'
					 )
			plt.ylabel("Time (Milliseconds)")
			plt.xlabel("Size")
			#plt.xticks(np.arange(len(xTicks)), xTicks, rotation=90, fontsize=4)
			plt.title(config if config is not None else "")
			plt.savefig("{}/plot_time_best_{}_{}.pdf".format(out, config,"size"))
			#plt.show()
			plt.close()

		from scipy import stats
		slope, intercept, r_value, p_value, std_err = stats.linregress(xSize, yTime)

		frelationtimesize.write("{},{},{},{},{},{},{}\n".format(config,len(xSize),slope, intercept, r_value, p_value, std_err))

	frelationtimesize.close()
	print("END totalDiffAnalyzed {} total config {}".format(totalDiffAnalyzed, totalRow))
	print("saved result at {}".format(csvout))


def computeTime(rootResults, dirOut ="./plots/data/", plot = False):
	print("Starting analyzing folder {}".format(rootResults))
	files = (os.listdir(rootResults) )
	files = list(filter(lambda x: os.path.isdir(os.path.join(rootResults,x)), files))
	totalDiffAnalyzed = 0

	problems = []

	totalRow = 0
	out = "{}/relationSizeTime/".format(dirOut)
	if not os.path.exists(out):
		os.makedirs(out)

	sizePerConfig = {}
	## Navigate group ids
	for groupId in sorted(files, key= lambda x: int(x), reverse=False):
		print("group {}".format(groupId))
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

				size, height = getTreeMetricsFromFile(filesGroup, diff)


				## for the first call to this method, let's store the columns
				columnsToMap(df, indexesOfPropertiesInTable=indexesOfColumns)

				for rowConfiguration in df.itertuples():
					totalRow += 1
					keyConfiguration = getConfigurationKeyFromCSV(rowConfiguration, indexesOfColumns)

					if keyConfiguration not in sizePerConfig:
						sizePerConfig[keyConfiguration] = []

					sizePerConfig[keyConfiguration].append((size,rowConfiguration.TIME))

				totalDiffAnalyzed += 1

			except Exception as e:
				print("Problems with {}".format(diff))
				#print(e.with_traceback())
				problems.append(diff)

			#break

	csvout = "{}/relation_time_size.csv".format(dirOut)
	frelationtimesize = open(csvout, "w")
	frelationtimesize.write("config,nrpoints, slope,intercept,r_value,p_value,std_err\n")
	for config in sizePerConfig.keys():
		pairs = sizePerConfig[config]
		xSize = []
		yTime = []
		for p in pairs:
			if not np.isnan(p[0]) and not np.isnan(p[1]) :
				xSize.append(p[0])
				yTime.append(p[1])
		#print(config)
		#print("x {}".format(xSize))
		#print("y {}".format(yTime))
		if plot :
			plt.clf()
			plt.plot(xSize, yTime,  'ro' #color='red', marker='x', linestyle='dashed', linewidth=1, markersize=1,
					 #label='Not Best Configuration'
					 )
			plt.ylabel("Time (Milliseconds)")
			plt.xlabel("Size")
			#plt.xticks(np.arange(len(xTicks)), xTicks, rotation=90, fontsize=4)
			plt.title(config if config is not None else "")
			plt.savefig("{}/plot_time_best_{}_{}.pdf".format(out, config,"size"))
			#plt.show()
			plt.close()

		from scipy import stats
		slope, intercept, r_value, p_value, std_err = stats.linregress(xSize, yTime)

		frelationtimesize.write("{},{},{},{},{},{},{}\n".format(config,len(xSize),slope, intercept, r_value, p_value, std_err))

	frelationtimesize.close()
	print("END totalDiffAnalyzed {} total config {}".format(totalDiffAnalyzed, totalRow))
	print("saved result at {}".format(csvout))
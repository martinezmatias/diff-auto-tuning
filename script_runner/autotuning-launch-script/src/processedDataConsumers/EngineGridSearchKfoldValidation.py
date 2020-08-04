import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
from sklearn import datasets
from src.commons.DiffAlgorithmMetadata import *
from sklearn.model_selection import KFold, train_test_split
import pandas
import scipy
import numpy
from scipy.stats import wilcoxon, kruskal
import pingouin as pg
from sklearn.utils import shuffle
from src.commons.Utils import  *
from src.commons.Datalocation import  *

def computeGridSearchKFold(pathResults ="{}/distance_per_diff.csv".format(RESULTS_PROCESSED_LOCATION), kFold = 5, algorithm = None, defaultId = None, random_seed = 0, datasetname = None, out = RESULTS_PROCESSED_LOCATION, fration =1 ):

	print("----\nRunning {} algoritm {}".format(pathResults, algorithm))
	k_fold = KFold(kFold, random_state=0)

	df = pandas.read_csv(pathResults, sep=",")

	print("DS size before {} ".format(df.size))
	## let's shuffle the results, otherwise they are grouped by megadiff group id
	df = df.sample(frac=fration, random_state=random_seed).reset_index(drop=True)
	print("DS size after {} ".format(df.size))

	columns = list(df.columns)

	# We get the name of the configurations
	allConfig = []
	if algorithm is None:
		allConfig = columns[1:]
	else:
		for aConfig in columns[1:]:
			if algorithm in aConfig:
				allConfig.append(aConfig)

	indexOfConfig = {}
	# we start in 1 because the first is the diff
	for i in range(1, len(columns)):
		indexOfConfig[columns[i]] = i

	print("All configs considered with algo {}:{}".format(algorithm, len(allConfig)))
	# we get the first column, which has the diff names
	diffs = df['diff']

	allDiff = list(diffs.values)

	resultsByKTraining = []
	resultsByKTestingSorted = []
	resultsByKTestingByConfig = []

	bestOnTestingByFold = {}
	avgIndexOnTesting = {}

	rp_index = []
	srho_index = []
	pmann_index = []
	pwilcoxon_index = []

	rp_performance = []
	srho_performance= []
	pmann_performance = []
	pwilcoxon_performance = []

	# For each Fold
	for k, (train, test) in enumerate(k_fold.split(allDiff)):
		X_train = []
		X_test = []
		print("\n---------Running fold {}".format(k))

		# Create the training dataset
		for i in train:
			X_train.append(allDiff[i])

		# Create the testing dataset
		for i in test:
			X_test.append(allDiff[i])

		print("\nTraining {} ".format(k))

		configsTraining,rankedConfigsTraining = findBestRanking(X_train, allConfig, df, indexOfConfig)

		resultsByKTraining.append(rankedConfigsTraining)

		print("\nTesting {} ".format(k))

		configsTesting,rankedConfigTesting = findBestRanking(X_test, allConfig, df, indexOfConfig)
		resultsByKTestingSorted.append(rankedConfigTesting)
		resultsByKTestingByConfig.append(configsTesting)

		saveBest(out=out, data=configsTesting, typeset=datasetname, k = k, algo=algorithm, name="performance")


		for config in rankedConfigTesting:
			if config['c'] not in bestOnTestingByFold:
				bestOnTestingByFold[config['c']] = []
				avgIndexOnTesting[config['c']] = []

			bestOnTestingByFold[config['c']].append(config['bs'])
			avgIndexOnTesting[config['c']].append(config['i'])


		###maybe only compare top X
		print("\nCheck with defaults: ")
		compareDefaultWithBest(rankedConfigsTraining)

	print("\n--End Kfold:")

	## Once we finish, we compute the correlation between the rankings
	print("\nCheck k-fold rankings: ")
	for i in range(0, len(resultsByKTestingByConfig)):
		for j in range(0, len(resultsByKTestingByConfig)):
			if i > j :
				print("\nCorrelation between testing i:{} j:{} ".format(i,j))
				rp, srho, pmann, pwilcoxon =  computeCorrelation(resultsByKTestingByConfig[i], resultsByKTestingByConfig[j], field = 'i')
				rp_index.append(rp[0])
				srho_index.append(srho[0])
				pmann_index.append(pmann)
				pwilcoxon_index .append(pwilcoxon)

				rp, srho, pmann, pwilcoxon = computeCorrelation(resultsByKTestingByConfig[i], resultsByKTestingByConfig[j],  field = 'bs')
				rp_performance.append(rp[0])
				srho_performance.append(srho[0])
				pmann_performance.append(pmann)
				pwilcoxon_performance.append(pwilcoxon)


	print("\n Getting the best:")
	## As we have compute K folds, we summarize the performance
	iK = 0
	performanceTestingBestOnTraining = []
	indexTestingBestOnTraining = []

	for iResultsFold in resultsByKTraining:
		print("\nAnalyzing kfold {}".format(iK))
		## We retrieve the perfomrance on testing of  the best config from training
		bestConfigInTraining = iResultsFold[0]
		print("K: {} Best configuration given by the training: {}".format(iK, bestConfigInTraining))

		bestConfigInTesting = None
		#Now, we find it in the corresponding training (not necesary is the best i.e. the first one)
		resultTestingOfK = resultsByKTestingSorted[iK]
		for aConfigFromTesting in resultTestingOfK:
			if aConfigFromTesting['c'] == bestConfigInTraining['c']:
				bestConfigInTesting = aConfigFromTesting

		## find the default in Testing:
		performanceDefaultOnTesting = bestOnTestingByFold[defaultId] ## each position has the data of one fold
		indexDefaultOnTesting = avgIndexOnTesting[defaultId]

		if bestConfigInTesting is not None:
			print("K: {} Default configuration performance {} index {}".format(iK, performanceDefaultOnTesting[iK], indexDefaultOnTesting[iK]))
			print("K: {} Best configuration given by the training on the testing: {}".format(iK, bestConfigInTesting))

			performanceTestingBestOnTraining.append(bestConfigInTesting['bs'])
			indexTestingBestOnTraining.append(bestConfigInTesting['i'])

		iK += 1

	print("avg performance on testing of Default {}: {}".format(np.mean(performanceDefaultOnTesting),
																 performanceDefaultOnTesting))
	print("avg index on testing of best in Default {}: {}".format(np.mean(indexDefaultOnTesting),
																   indexDefaultOnTesting))

	print("avg performance on testing of best in training {}: {}".format(np.mean(performanceTestingBestOnTraining), performanceTestingBestOnTraining))
	print("avg index on testing of best in training {}: {}".format(np.mean(indexTestingBestOnTraining),
																		 indexTestingBestOnTraining))

	print("avg  rp_index: {} {}".format(np.mean(rp_index),rp_index))

	saveList(out, datasetname=datasetname,  algorithm=algorithm, data=rp_index,name="rp_index" )
	print("avg  srho_index: {} {}".format(np.mean(srho_index), srho_index))
	saveList(out, datasetname=datasetname, algorithm=algorithm,  data=srho_index,name="srho_index" )
	print("avg  pmann_index: {} {}".format(np.mean(pmann_index), pmann_index))
	saveList(out, datasetname=datasetname, algorithm=algorithm, data=pmann_index,name="pmann_index" )
	print("avg  pwilcoxon_index: {} {}".format(np.mean(pwilcoxon_index), pwilcoxon_index))
	saveList(out, datasetname=datasetname, algorithm=algorithm, data=pwilcoxon_index,name="pwilcoxon_index" )
	print("avg  rp_performance: {} {}".format(np.mean(rp_performance), rp_performance))
	saveList(out, datasetname=datasetname, algorithm=algorithm, data=rp_performance,name="rp_performance" )
	print("avg  srho_performance: {} {}".format(np.mean(srho_performance), srho_performance))
	saveList(out, datasetname=datasetname, algorithm=algorithm, data=srho_performance,name="srho_performance" )
	print("avg  pmann_performance: {} {}".format(np.mean(pmann_performance), pmann_performance))
	saveList(out, datasetname=datasetname, algorithm=algorithm, data=pmann_performance,name="pmann_performance" )
	print("avg  pwilcoxon_performance: {} {}".format(np.mean(pwilcoxon_performance), pwilcoxon_performance))
	saveList(out, datasetname=datasetname, algorithm=algorithm, data=pwilcoxon_performance,name="pwilcoxon_performance" )

	saveList(out, datasetname=datasetname, algorithm=algorithm, data=performanceTestingBestOnTraining,
			 name="performanceTestingBestOnTraining")

	saveList(out, datasetname=datasetname, algorithm=algorithm, data=indexTestingBestOnTraining,
			 name="indexTestingBestOnTraining")


	saveList(out, datasetname=datasetname, algorithm=algorithm, data=bestOnTestingByFold[defaultId],
			 name="performanceTestingDefaultOnTraining")

	saveList(out, datasetname=datasetname, algorithm=algorithm, data=indexDefaultOnTesting[defaultId],
			 name="indexTestingDefaultOnTraining")


	saveAvgPerformancePerConfig(out=out, data=bestOnTestingByFold,typeset=datasetname, algo=algorithm, name="performance" )


	return performanceTestingBestOnTraining,  bestOnTestingByFold[defaultId] , rp_index,srho_index,pmann_index, pwilcoxon_index, rp_performance,srho_performance,pmann_performance,pwilcoxon_performance

def saveBest(out, data, typeset,k, algo = "",name = "" ):


	filename = "{}/summary_performance_{}_{}_K_{}_{}.csv".format(out, name, typeset, k, algo)
	fout1 = open(filename, 'w')
	for conf in data:
			fout1.write("{},{},{},{}\n".format(conf['c'], conf['av'], conf['bs'], conf['i']))
	fout1.flush()
	fout1.close()
	print("Save results at {}".format(filename))

def saveAvgPerformancePerConfig(out,  typeset, data = {}, algo = "",name = "" ):


	filename = "{}/summary_avg_performance_{}_{}_{}.csv".format(out, name, typeset,  "allAlgorithms" if algo is None else  algo)
	fout1 = open(filename, 'w')
	for conf in data.keys():
			fout1.write("{},{}\n".format(conf, np.mean(data[conf])))
	fout1.flush()
	fout1.close()
	print("Save results at {}".format(filename))

def saveList(out,datasetname, data, algorithm, name):

	filename = "{}/summary_{}_{}_{}.csv".format(out,datasetname, "allAlgorithms" if algorithm is None else  algorithm, name)
	fout1 = open(filename, 'w')
	for conf in data:
			fout1.write("{}\n".format(conf))
	fout1.flush()
	fout1.close()
	print("Save results at {}".format(filename))
'''
df is the  dataframe with all data
X: the list of the diffs to consider (because we may not be interested in analyzing all diffs, specially on the k-fold)
allconfig: the key of all configurations 
'''
def analyzeConfigurationsFromDiffs(df, setofDiffToConsider, allconfig, indexOfColumns):

	# This array stores, per configuration, a list with all the distance values
	valuesPerConfig = [[] for i in allconfig]

	# This array stores, per configuration, the number of diffs analyzed
	presentPerConfig = [0 for i in allconfig]

	countRow = 0
	for rowDiff in df.itertuples():

		countRow+=1
		# in the DataFrame row the first two positions are the tuple id and diff name.
		## example #<class 'tuple'>: (22671, 'nr_98_id_1_010de14013c38b7f82e4755270e88a8249f3a825_SimpleConveyer_GTSPOON.csv', 2.0, 16.0, 80.0, 80.0,192.0, ...
		diff_ID = rowDiff[1]
		if diff_ID in setofDiffToConsider:
			# in the DataFrame row the first two positions are the tuple id and diff name. So, we start in the Shift = 1 position
			shift = 1

			for i in range(0, len(allconfig)):
				currentConfig = allconfig[i]
				indexOfcurrent =  indexOfColumns[currentConfig]
				positionOfConfig = shift + indexOfcurrent
				#if i < 10:
				#	print("{} {} ".format(currentConfig, positionOfConfig))
				distance = rowDiff[positionOfConfig]
				if not np.isnan(distance):
					valuesPerConfig[i].append(distance)
					presentPerConfig[i]+=1


	return valuesPerConfig, presentPerConfig


def compareDefaultWithBest(rankedBestConfigs):

	configs = list(defaultConfigurations.values())

	rankingDefaultConfig = []

	allDefaults = []

	## collect the information about each defauls
	for i in range(0, len(rankedBestConfigs)):
		currentConfig = rankedBestConfigs[i]
		nameConfig = currentConfig['c']
		if nameConfig in configs:
			rankingDefaultConfig.append((i, currentConfig))
			allDefaults.append(currentConfig)

	## Print each default
	print("\nDefaults configs: ")
	print(rankingDefaultConfig)
	for defaultC in rankingDefaultConfig:
		print(defaultC)

	return allDefaults


def computeCorrelation(configsTesting, configsTraining, field = 'i'):
	print("\nField: {}".format(field))
	xbestTraining = [round(x[field],4) for x in configsTraining]
	ybestTest = [round(x[field],4) for x in configsTesting]
	#print("index ({}) left {}".format(len(xbestTraining), ",".join(["%.3f"%x for x in xbestTraining])))
	#print("index ({}) right {}".format(len(ybestTest),",".join(["%.3f"%x for x in ybestTest])))
	#print("index ({}) left {}".format(len(xbestTraining), xbestTraining))
	#print("index ({}) right {}".format(len(ybestTest), ybestTest))

	rp = scipy.stats.pearsonr(xbestTraining, ybestTest)
	print("Pearson's r {} ".format(rp))
	srho = scipy.stats.spearmanr(xbestTraining, ybestTest)
	print("Spearman's rho {} ".format(srho))
	print("Kendall's tau {} ".format(scipy.stats.kendalltau(xbestTraining, ybestTest)))


	stat, pwil = wilcoxon(xbestTraining, ybestTest, alternative='two-sided')

	print('scipy wilcoxon: stat=%.3f, p=%.3f' % (stat, pwil))

#	stats = pg.wilcoxon(xbestTraining, ybestTest, tail='two-sided')
#	print("pingouin wilcoxon:\n {}".format(stats))


	#stat, p = kruskal(xbestTraining, ybestTest)
	#print('scipy kruskal:\ stat=%.3f, p=%.3f' % (stat, p))

	#print("eff size % f" % pg.compute_effsize(xbestTraining, ybestTest))
	# https://pingouin-stats.org/generated/pingouin.mwu.html#pingouin.mwu
#	stats = pg.mwu(xbestTraining, ybestTest, tail='two-sided')
#	print("pingouin MWU:\n {}".format(stats))

	stat, pmann= scipy.stats.mannwhitneyu(xbestTraining, ybestTest, alternative='two-sided')

	print('scipy mannwhitneyu: stat=%.3f, p=%.3f' % (stat, pmann))

	return rp, srho, pmann, pwil

def findBestRanking(X_train, allConfig, df, indexOfColumns):
	valuesPerConfig, presentPerConfig = analyzeConfigurationsFromDiffs(df, X_train, allConfig, indexOfColumns)
	configs, rankedBest = computeBestConfiguration(allConfig, presentPerConfig, valuesPerConfig)
	return configs, rankedBest


def computeBestConfiguration(allConfig, presentPerConfig, valuesPerConfig):

	averages = [0 for x in (allConfig)]
	bestIn = [0 for x in (allConfig)]
	configs= []
	for i in range(0, len(allConfig)):
		averages[i] = np.mean(valuesPerConfig[i])
		zeros = list(filter(lambda x: x == 0, valuesPerConfig[i]))
		if presentPerConfig[i] is not  0:
			bestIn[i] = len(zeros) / presentPerConfig[i]
		else:
			bestIn[i] = 0
		configs.append({'c':allConfig[i], 'av':averages[i], 'bs':bestIn[i]})

	##Sorting according number of best
	bestOrder = sorted(configs, key=lambda x: x['bs'], reverse=True)
	#print("Bests ({}) {}".format(len(bestOrder),bestOrder))

	for i in range(0, len(bestOrder)):
		bestOrder[i]["i"] = i

	return configs, bestOrder



def computeBestAndDefaultByFoldFiles(algoName, fileBest, fileDefault):
	computeBestAndDefaultByFold(algoName, readFileToFloatList(fileBest), readFileToFloatList(fileDefault))

'''Plots the numbers form rq1'''
def computeBestAndDefaultByFold(algoName, allBest = [], allDefault = []):
	print("\nComparing performance of best and default by fold. k={}".format(len(allBest)))
	print("{}: allBest mean {:.2f}\% (st {:.2f})".format(algoName, np.mean(allBest)* 100, np.std(allBest)* 100))
	print("{}: allDefault mean {:.2f}\% st {:.2f})".format(algoName, np.mean(allDefault)* 100, np.std(allDefault)* 100))
	improvements = []
	for i in range(0,len(allBest)):
		improvements.append(allBest[i] - allDefault[i])

	print("{}: improvement mean {:.2f}\% (st {:.2f})".format(algoName, np.mean(improvements) * 100, np.std(improvements)* 100))
	stat, pwil = wilcoxon(allBest, allDefault, alternative='two-sided')

	print('scipy wilcoxon: stat=%.3f, p=%.3f' % (stat, pwil))
'''plots the distribution for RQ1'''
def plotDistributionAvg( fileGT, fileCD, fileXY, defaultGT, defaultCD, defaultXy, model = "JDT"):

	allGT = readCSVToFloatList(fileGT)
	allCD = readCSVToFloatList(fileCD)
	allXY = readCSVToFloatList(fileXY)

	alldefaultGT = readFileToFloatList(defaultGT)
	alldefaultCD= readFileToFloatList(defaultCD)
	alldefaultXy= readFileToFloatList(defaultXy)

	defaultGt= np.mean(alldefaultGT)
	defaultCD= np.mean(alldefaultCD)
	defaultXY = np.mean(alldefaultXy)
	#plt.boxplot(gt)
	#plt.plot(1, 0.54, 'X', alpha=1)
	#plt.show()


	#plt.hist(gt)
	#plt.show()
	algos = [allGT, allCD, allXY]
	ax = sns.violinplot(data = algos,split=True,orient = "v" ,inner="quartile", cut=0, )
	ax.set_xticklabels(['GumTree', 'ChangeDistiller', 'Xy'])
	plt.plot(0, defaultGt, 'X', alpha=1, color='red', markersize=20 )
	plt.plot(1, defaultCD, 'X', alpha=1, color='red', markersize=20 )
	plt.plot(2, defaultXY, 'X', alpha=1, color='red', markersize=20 )
	plt.xticks(fontsize=20)
	plt.yticks(fontsize=20)
	#plt.xlabel("Diff Algorithm", fontproperties=20)
	plt.ylabel("Performance", fontsize=14)
	plt.savefig("distr_avg_performance_{}.pdf".format(model))
	plt.show()

def countHigherValuesFile(path, thr = 0.05):
	print("\nAnalyzing {}".format(path))
	countHigherValues(readFileToFloatList(path), thr)

def countHigherValues(all, thr = 0.05):

	sup = list(filter(lambda x: x>thr, all))

	print("Total {} Sup {} ({:.2f}%)".format(len(all),len(sup), (len(sup)/len(all)*100)))


def runReadResultsCrossValidation(path = "/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/plots/data_monday/summary_performance_performance_{}_K_{}_{}.csv", dataset ="merge_gtJDT_5_CDJDT_4", onlytop = False ):

		corr_thr = 0.90
		dist_thr=0.05


		for algo in ["Gumtree", "ChangeDistiller", "Xy"]:
			person_sup = 0
			mann_sup = 0
			wilcoxon_sup = 0
			total = 0
			slopes =  []
			std_errs = []
			r_values = []
			for i in range(0, 10):
				for j in range(0, 10):
					if i> j:
						total +=1
						print("\nAlgo {} i {} j {}".format(algo, i, j))
						ki = path.format(dataset, i, algo)
						kj = path.format(dataset,j, algo)

						## let'salso check the names column zero to be sure
						li = readCSVToFloatList(ki, indexToKeep=2)
						lj = readCSVToFloatList(kj, indexToKeep=2)

						decimals = 2
						li = ([round(x,decimals) for x in   li])
						lj = ([round(x,decimals) for x in   lj])


						ci = readCSVToStringList(ki, indexToKeep=0)
						cj = readCSVToStringList(kj, indexToKeep=0)
						## Remove outliers
						if onlytop:
							soli = []
							solj = []
							mli = np.mean(li)
							mlj = np.mean(lj)

							sdli = np.std(li)
							sdlj = np.std(lj)

							for l in range(0, len(ci)):
								#if not isOutlier(value=li[l], mean=mli, std=sdli) and not isOutlier(value=lj[l], mean=mlj, std=sdlj):
								if li[l] >= 2 * sdli  and lj[l]>= 2 * sdlj:

									soli.append(li[l])
									solj.append(lj[l])

							print("Size after removing outliers {}".format(len(soli)))

							li= soli
							lj = solj

							print(li)
							print(lj)

						for l in range(0, len(ci)):
							if not ci[l] == cj[l]:

								print("error {}".format(l) )
								return
							#print("{} {} ".format(ci[l],cj[l]))

						print("Size data {}".format(len(li)))
						rp = scipy.stats.pearsonr(li, lj)
						print("Pearson's r {} ".format(rp))

						if rp[0] >corr_thr:
							person_sup+=1

						srho = scipy.stats.spearmanr(li, lj)
						print("Spearman's rho {} ".format(srho))

						#m, b  = np.polyfit(li, lj, 1)
						from scipy import stats
						slope, intercept, r_value, p_value, std_err = stats.linregress(li, lj)
						print("linear regression slope {}, intercept {}, r_value {}, p_value {}, std_err {}".format(slope, intercept, r_value, p_value, std_err ))
						slopes.append(slope)
						std_errs.append(std_err)
						r_values.append(r_value)
						stat, pwil = wilcoxon(li, lj, alternative='two-sided')

						print('scipy wilcoxon: stat=%.3f, p=%.3f' % (stat, pwil))

						if pwil >dist_thr:
							wilcoxon_sup+=1

						stat, pmann = scipy.stats.mannwhitneyu(li, lj, alternative='two-sided')

						print('scipy mannwhitneyu: stat=%.3f, p=%.3f' % (stat, pmann))
						if pmann > dist_thr:
							mann_sup += 1

			print("\nSummary {} total {} pperson {} ({}) , mann {} ({}) , wilcoxon {} ({}) ".format(algo, total, person_sup , 100*(person_sup/total), mann_sup, 100*(mann_sup/total), wilcoxon_sup, 100*(wilcoxon_sup/total)))

			print("avg r-values st slopes st   std err st ")
			print("& {:.5f} & {:.5f} & {:.5f} & {:.5f} & {:.5f} & {:.5f}\\".format(np.mean(r_values), np.std(r_values),np.mean(slopes), np.std(std_errs), np.mean(std_errs),np.std(std_errs)))
		print("\n----End {}".format(algo))

def isOutlier(value, mean,  std, m = 2):
	return abs(value - mean) > m * std



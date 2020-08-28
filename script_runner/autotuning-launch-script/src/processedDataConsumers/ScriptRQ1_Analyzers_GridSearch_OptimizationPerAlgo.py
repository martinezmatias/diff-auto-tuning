import unittest

from src.processedDataConsumers.EngineGridSearchKfoldValidation import *
from src.processedDataConsumers.deprecated.plotPerformance import *
from src.rowDataConsumers.RQ0_Setup_ComputeFitnessDistanceOfConfiguationsFromRowData import *
from src.commons.Datalocation import *
from src.commons.DiffAlgorithmMetadata import *

class TestGrid(unittest.TestCase):


	def _test_A_ComputeFitnessFastPerAlgorithm(self):

		for folderToAnalyze in [NAME_FOLDER_ASTJDT,  NAME_FOLDER_ASTSPOON]:
			for algorithm in ["Gumtree", "ChangeDistiller", "XyMatcher"]:
				computeFitness("{}/{}/".format(RESULTS_ROW_LOCATION, folderToAnalyze), suffix="{}_{}".format(folderToAnalyze, algorithm), key = algorithm)

	def _test_B_ComputeBestKFoldComplete(self):
		for folderToAnalyze in [NAME_FOLDER_ASTJDT,
								NAME_FOLDER_ASTSPOON
								]:
			print("\nanalyzing {}".format(folderToAnalyze))
			kvalue = 3#10
			random_seed_value = 35
			fraction = 0.1

			computeGridSearchKFold("{}/distance_per_diff_{}_Gumtree.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze), kFold=kvalue, algorithm="Gumtree", defaultId=defaultConfigurations["ClassicGumtree"], random_seed=random_seed_value, datasetname=folderToAnalyze, fration=fraction)


			computeGridSearchKFold("{}/distance_per_diff_{}_ChangeDistiller.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze), kFold=kvalue,   algorithm="ChangeDistiller", defaultId=defaultConfigurations["ChangeDistiller"], random_seed=random_seed_value, datasetname=folderToAnalyze, fration=fraction)


			computeGridSearchKFold("{}/distance_per_diff_{}_XyMatcher.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze), kFold=kvalue,	algorithm="XyMatcher", defaultId=defaultConfigurations["XyMatcher"], random_seed=random_seed_value, datasetname=folderToAnalyze, fration=fraction)

	def _test_B_ComputeBestKFoldComplete(self):
		for folderToAnalyze in [NAME_FOLDER_ASTJDT,
								NAME_FOLDER_ASTSPOON
								]:
			print("\nanalyzing {}".format(folderToAnalyze))
			kvalue = 5#10
			random_seed_value = 20
			fraction = 0.01

			computeGridSearchKFold("{}/editscript_size_per_diff_{}_Gumtree.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze),overwrite=True, kFold=kvalue, algorithm="Gumtree", defaultId=defaultConfigurations["ClassicGumtree"], random_seed=random_seed_value, datasetname=folderToAnalyze, fration=fraction)

	def _test_B_ComputeBestKFoldtest(self):
		for folderToAnalyze in [NAME_FOLDER_ASTJDT,
								NAME_FOLDER_ASTSPOON
								]:
			print("\nanalyzing {}".format(folderToAnalyze))
			kvalue = 2#10
			random_seed_value = 1
			fraction = 0.001

			computeGridSearchKFold("{}/editscript_size_per_diff_{}_Gumtree.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze),overwrite=True, kFold=kvalue, algorithm="Gumtree", defaultId=defaultConfigurations["ClassicGumtree"], random_seed=random_seed_value, datasetname=folderToAnalyze, fration=fraction)



	def _test_C_plot(self):
		##TODO: the data must be readen from files
		for folderToAnalyze in [NAME_FOLDER_ASTJDT,
								NAME_FOLDER_ASTSPOON
								]:
			print("\nanalyzing {}".format(folderToAnalyze))
			kvalue = 4#10
			random_seed_value = 1
			fraction = 1
			allOptimized = []
			allDefault = []
			optimizedgt, defaultgt,  rp_index,srho_index,pmann_index, pwilcoxon_index, rp_performance,srho_performance,pmann_performance,pwilcoxon_performance = computeGridSearchKFold("{}/distance_per_diff_{}_Gumtree.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze), kFold=kvalue, algorithm="Gumtree", defaultId=defaultConfigurations["ClassicGumtree"], random_seed=random_seed_value, datasetname=folderToAnalyze, fration=fraction)
			allOptimized.append(optimizedgt)
			allDefault.append(defaultgt)

			optimizedcd, defaultcd,  rp_index,srho_index,pmann_index, pwilcoxon_index, rp_performance,srho_performance,pmann_performance,pwilcoxon_performance =computeGridSearchKFold("{}/distance_per_diff_{}_ChangeDistiller.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze), kFold=kvalue,   algorithm="ChangeDistiller", defaultId=defaultConfigurations["ChangeDistiller"], random_seed=random_seed_value, datasetname=folderToAnalyze, fration=fraction)
			allOptimized.append(optimizedcd)
			allDefault.append(defaultcd)

			optimizedxy, defaultxy,  rp_index,srho_index,pmann_index, pwilcoxon_index, rp_performance,srho_performance,pmann_performance,pwilcoxon_performance =computeGridSearchKFold("{}/distance_per_diff_{}_XyMatcher.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze), kFold=kvalue,	algorithm="XyMatcher", defaultId=defaultConfigurations["XyMatcher"], random_seed=random_seed_value, datasetname=folderToAnalyze, fration=fraction)
			allOptimized.append(optimizedxy)
			allDefault.append(defaultxy)

			print("END: printing summary")

			print("optimizedgt: {}".format(optimizedgt))
			print("defaultgt: {}".format(defaultgt))
			print("optimizedcd: {}".format(optimizedcd))
			print("defaultcd: {}".format(defaultcd))
			print("optimizedxy: {}".format(optimizedxy))
			print("defaultxy: {}".format(defaultxy))

			plotImprovements(improvements=allOptimized, defaults=allDefault, key = folderToAnalyze)
			print("\nanalyzing {}".format(folderToAnalyze))

	def _testAnalyzeGeneralResults(self):

			proportionEvals = 0.1

			searchMethod = "GridSearch"#"TPE"#"random"#"GridSearch"

			fraction = 1
			for modelToAnalyze in [ NAME_FOLDER_ASTSPOON, NAME_FOLDER_ASTJDT]: #
				modelName = "JDT" if "JDT" in modelToAnalyze else "Spoon"
				for algo in ["Gumtree"]:

					sizeSpaceAlgo = sizeSearchSpace[algo]
					evals = int(proportionEvals * sizeSpaceAlgo)

					# if is zero, we put 1
					evals = 1 if evals is 0 else evals
					print("Total evals for {} {}".format(algo, evals))

					evalString = "_evals_{}".format(evals)

					if  searchMethod is "GridSearch":
						evalString = ""

					allPercentageBestAllSeed = []
					allPercentageEqualsAllSeed = []
					allPercentageWorstAllSeed = []

					for iseed in range(0, 10):
							print("Analyzing seed {}".format(iseed))
							try:
								#/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/results/finalResults/finalAll
								location = "{}/finalResults/finalAll/{}/dataset_{}/algorithm_{}/seed_{}/fractionds_{}/".format(RESULTS_PROCESSED_LOCATION,searchMethod,modelToAnalyze, algo, iseed,fraction )
								##Best
								file = "{}/summary_{}_{}_proportionBestOnTesting{}_f_{}.csv".format(location, modelToAnalyze, algo,evalString, fraction )
								print("File {}".format(file))
								res = readFileToFloatList(file)
								print(res)
								allPercentageBestAllSeed.extend(res)

								#Equals
								file = "{}/summary_{}_{}_proportionEqualsOnTesting{}_f_{}.csv".format(location, modelToAnalyze,
																								  algo,evalString, fraction)
								res = readFileToFloatList(file)
								print(res)
								allPercentageEqualsAllSeed.extend(res)

								##Worrst

								file = "{}/summary_{}_{}_proportionDefaultOnTesting{}_f_{}.csv".format(location,
																									modelToAnalyze,
																									algo, evalString, fraction)
								res = readFileToFloatList(file)
								print(res)
								allPercentageWorstAllSeed.extend(res)
							except Exception as ex:
								print(ex)


					avgBest = np.mean(allPercentageBestAllSeed)
					avgEquals = np.mean(allPercentageEqualsAllSeed)
					avgWorst = np.mean(allPercentageWorstAllSeed)

					print(modelName)
					print("& {:.2f}\% & {:.2f}\% & {:.2f}\%  ".format(avgBest * 100, avgEquals * 100, avgWorst * 100))

	def _testAnalyzeCostResults(self):

			proportionEvals = 0.1
			isTPE = True
			searchMethod = "GridSearch"

			evalString = "_evals"
			listBests = []
			for modelToAnalyze in [ NAME_FOLDER_ASTSPOON, NAME_FOLDER_ASTJDT]:
				modelName = "JDT" if "JDT" in modelToAnalyze else "Spoon"
				for algo in ["Gumtree"]:

					avgBestPerRange = []
					stdBestPerRange = []
					avgWorstPerRange = []
					rangeSelected = [0.001, 0.005, 0.01, 0.05, 0.1, 0.5, 1]
					for fraction in rangeSelected:

						print("\nRatio: {}".format(fraction))
						sizeSpaceAlgo = sizeSearchSpace[algo]
						evals = int(proportionEvals * sizeSpaceAlgo)
						# if is zero, we put 1
						evals = 1 if evals is 0 else evals
						print("Total evals for {} {}".format(algo, evals))

						allPercentageBestAllSeed = []
						allPercentageEqualsAllSeed = []
						allPercentageWorstAllSeed = []

						allMetricBestAllSeed = []
						allMetricDefaultAllSeed = []


						for iseed in range(0, 5):
								print("Analyzing seed {}".format(iseed))
								#NAME_FOLDER_ASTJDT,
								location = "{}/lastResults/{}/dataset_{}/algorithm_{}/seed_{}/fractionds_{}/".format(RESULTS_PROCESSED_LOCATION,searchMethod,modelToAnalyze, algo, iseed,fraction )
								##Best
								file = "{}/summary_{}_{}_proportionBestOnTesting_f_{}.csv".format(location, modelToAnalyze, algo,fraction )
								res = readFileToFloatList(file)
								print("Best {}".format(res))
								allPercentageBestAllSeed.extend(res)

								#Equals
								file = "{}/summary_{}_{}_proportionEqualsOnTesting_f_{}.csv".format(location, modelToAnalyze,
																								  algo, fraction)
								res = readFileToFloatList(file)
								print("Equals {}".format(res))
								allPercentageEqualsAllSeed.extend(res)

								##Worrst

								file = "{}/summary_{}_{}_proportionDefaultOnTesting_f_{}.csv".format(location,
																									modelToAnalyze,
																									algo, fraction)
								res = readFileToFloatList(file)
								print("Worst {} ".format(res))
								allPercentageWorstAllSeed.extend(res)


						avgBest = np.mean(allPercentageBestAllSeed)
						sdtBest = np.std(allPercentageBestAllSeed)
						avgEquals = np.mean(allPercentageEqualsAllSeed)
						avgWorst = np.mean(allPercentageWorstAllSeed)

						print(modelName)
						print("& {:.2f}\% & {:.2f}\% & {:.2f}\%  ".format(avgBest * 100, avgEquals * 100, avgWorst * 100))
						print("Best of fraction {}: av {} all {}".format(fraction, avgBest, allPercentageBestAllSeed))
						avgBestPerRange.append(avgBest)
						stdBestPerRange.append(sdtBest)

					listBests.append(avgBestPerRange)
					#listBests.append(stdBestPerRange)
					print("All avg per range: {}".format(avgBestPerRange))

			for l in listBests:
				plt.plot(l, linewidth=3.0)
			#plt.xticks([i for i in range(0, len(rangeSelected))] , ["{} ({}%)".format( rangeSelected[i] * 100000,  rangeSelected[i] * 100).replace(".0", "") for i in range(0, len(rangeSelected))], rotation = 45)
			plt.xticks([i for i in range(0, len(rangeSelected))],
					   ["{} ".format(rangeSelected[i] * 100000).replace(".0", "")
						for i in range(0, len(rangeSelected))], rotation=0)

			plt.ylabel("% pairs Global improves Default",  fontsize=14)
			plt.xlabel("# of file pairs in dataset",  fontsize=14)
			plt.legend(["Spoon", "JDT"], fontsize=14)
			plt.xticks(fontsize=10)
			plt.yticks(fontsize=10)
			plt.savefig("figCostSpoonJDTGridSearch.pdf")
			plt.show()



	def testAnalyzeCostResultsPlotFromPaper(self):

			timeSingleDiff = 469#100 #miliseconds
			allDiff = 100000
			trainingSize = 0.8
			listPerformancePerCong = {}
			listTimesPerCong = {}
			listConfigs = {}
			completeKey = {}
			cperformances= {}
			for searchMethod in ["TPE" , "random", "GridSearch"
								  ]:

				listPerformancePerCong[ searchMethod] = []
				listTimesPerCong [ searchMethod] = []
				listConfigs [ searchMethod] = []

				for modelToAnalyze in [ #NAME_FOLDER_ASTSPOON,
										NAME_FOLDER_ASTJDT]:
					modelName = "JDT" if "JDT" in modelToAnalyze else "Spoon"
					for algo in ["Gumtree"]:

						avgBestPerRange = []
						avgWorstPerRange = []
						#rangeSelected = [0.001, 0.005, 0.01, 0.05, 0.1, 0.5, 1]
						proportionsDataset = [0.001,  0.01,  0.1, 0.5, 1]
						proportionEvalsConsidered = [0.01, 0.1, 0.5]

						for proportionDataset in proportionsDataset:

							print("\nproportionDataset: {}".format(proportionDataset))
							alreadyAnalyzedGrid = False
							for aProportionEval in proportionEvalsConsidered:

								sizeSpaceAlgo = sizeSearchSpace[algo]
								evals = int(aProportionEval * sizeSpaceAlgo)
								# if is zero, we put 1
								evals = 1 if evals is 0 else evals
								print("Total evals for {} {}".format(algo, evals))

								evalString = "_evals_{}".format(evals)

								if searchMethod is "GridSearch":

									if alreadyAnalyzedGrid:
										continue
									alreadyAnalyzedGrid = True

									evalString = ""
									evals = 2025




								allPercentageBestAllSeed = []
								allPercentageEqualsAllSeed = []
								allPercentageWorstAllSeed = []

								for iseed in range(0, 10):
									try:
										print("Analyzing seed {}".format(iseed))
										#NAME_FOLDER_ASTJDT,
										location = "{}/finalResults/finalAll/{}/dataset_{}/algorithm_{}/seed_{}/fractionds_{}/".format(RESULTS_PROCESSED_LOCATION,searchMethod,modelToAnalyze, algo, iseed,proportionDataset )
										##Best
										file = "{}/summary_{}_{}_proportionBestOnTesting{}_f_{}.csv".format(location, modelToAnalyze, algo,evalString,proportionDataset )
										print("File {}".format(file))
										res = readFileToFloatList(file)
										print("Best {}".format(res))
										allPercentageBestAllSeed.extend(res)

										#Equals
										file = "{}/summary_{}_{}_proportionEqualsOnTesting{}_f_{}.csv".format(location, modelToAnalyze,
																										  algo, evalString, proportionDataset)
										res = readFileToFloatList(file)
										print("Equals {}".format(res))
										allPercentageEqualsAllSeed.extend(res)

										##Worrst

										file = "{}/summary_{}_{}_proportionDefaultOnTesting{}_f_{}.csv".format(location,
																											modelToAnalyze,
																											algo, evalString,proportionDataset)
										res = readFileToFloatList(file)
										print("Worst {} ".format(res))
										allPercentageWorstAllSeed.extend(res)
									except Exception as ex:
										print(ex)
								## end seeds

								avgBest = np.mean(allPercentageBestAllSeed)
								avgEquals = np.mean(allPercentageEqualsAllSeed)
								avgWorst = np.mean(allPercentageWorstAllSeed)

								listPerformancePerCong[searchMethod].append(avgBest * 100)
								timesconfig = ((timeSingleDiff* (proportionDataset * allDiff * trainingSize) * evals ) / (1000 * 3600)  )# hrs
								listTimesPerCong[searchMethod].append(timesconfig)
								completeParticularKey = "{} d: {}{}".format(searchMethod, (proportionDataset * allDiff * trainingSize), evalString.replace("_evals_", " e: ")).replace(".0", "").replace("GridSearch","GS").replace("random","RS")
								completeKey[completeParticularKey] = timesconfig
								nameconfig = "{}".format(searchMethod).replace(
									"GridSearch", "GS").replace("random", "RS")
								cperformances[completeParticularKey] = avgBest

								listConfigs[searchMethod].append((nameconfig, (proportionDataset * allDiff  * trainingSize) ) )
								print("Time for {}  {} {}".format(timesconfig, nameconfig,avgBest ))

								print(modelName)
								print("& {:.2f}\% & {:.2f}\% & {:.2f}\%  ".format(avgBest * 100, avgEquals * 100, avgWorst * 100))
								print("Best of fraction {}: av {} all {}".format(proportionDataset, avgBest, allPercentageBestAllSeed))
								avgBestPerRange.append(avgBest)


			#import plotly.express as px

			#fig = plt.figure()
			fig = plt.figure(figsize=(3, 6))

			ax = plt.subplot(111)
			#mStyles = [".", ",", "o", "v", "^", "<", ">", "1", "2", "3", "4", "8", "s", "p", "P", "*", "h", "H", "+",
			#		   "x", "X", "D", "d", "|", "_", 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11
			#		   ]

			#colorsUsed = ["green", "blue", "red"]

			for c in completeKey.keys():
				print("c {} time {:.3f} hs {:.3f} ".format(c, completeKey[c], cperformances[c]*100))

			for method in listPerformancePerCong.keys():

				colour = "black"
				imstyle = "M"

				methodLegend = None
				if "TPE" in method:
					colour = "green"
					imstyle = "o"
					methodLegend = "TPE"
				elif "random" in method:
					colour = "red"
					imstyle = "X"
					methodLegend = "RandomSearch"
				elif "GridSearch" in method:
					colour = "blue"
					imstyle = "D"
					methodLegend = "GridSearch"

				print("{} {} {} ".format(method, colour, imstyle))
				plt.scatter(listTimesPerCong[method], listPerformancePerCong[method], s=120, marker=imstyle, c=colour,
							label = methodLegend, )  # , marker=mStyles[i]


				for i in range(0, len(listPerformancePerCong[method])):

					ax.annotate(str(listConfigs[method][i][1]).replace(".0",""), (listTimesPerCong[method][i], listPerformancePerCong[method][i]),size=18, color="black",
								rotation=23)
					#xytext=(z[0]+0.05, y[0]+0.3)

			#plt.xticks(listTimesPerCong, rotation=0)
			plt.ylabel("% of cases improved",  fontsize=45)
			plt.xlabel("Time (Hours) (log scale)",  fontsize=45)
			#plt.xlim(0, 700)
		#	ax.legend(bbox_to_anchor=(1.6, 1.05))


			ld = list(map(lambda x: str(x[0]).replace(".0","") ,listConfigs))

			#legend =  plt.legend(list(set(ld)), fontsize=12,
			#		   #mode = "expand",
			#		  # bbox_to_anchor=(1, 1.15),
			#		   ncol=7
			#
			plt.legend(loc=2, prop={'size': 25})
			plt.xticks(fontsize=30)
			plt.yticks(fontsize=30)
			plt.xscale("log")
			plt.savefig("figCostPoints.pdf")
			#legend.get_frame().set_facecolor('white')
			plt.show()
			#listTimesPerCong[searchMethod].append(timesconfig)




	'''For Table of  RQ 1'''
	def _test_C_ComparisonBest_TableRQ1(self):
		#GridSearch
		#/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/results/summaryResults/summaryResults/GridSearch/dataset_merge_out4gt_outCD/algorithm_Gumtree/seed_0/fractionds_1

		for astModel in [NAME_FOLDER_ASTJDT, NAME_FOLDER_ASTSPOON]:
			for algorithm in ["Gumtree", "ChangeDistiller", "XyMatcher"]:
				## for proportion 1, we took any seed, all have the same results

				fraction = 1
				locationFileResults = "{}/summaryResults/GridSearch/dataset_{}/algorithm_{}/seed_0/fractionds_{}/".format(RESULTS_PROCESSED_LOCATION,astModel, algorithm, fraction)
				computeBestAndDefaultByFoldFiles(astModel, algorithm,
												 "{}/summary_{}_{}_performanceTestingBestOnTraining_f_{}.csv".format(locationFileResults,astModel, algorithm,fraction)
												 , "{}/summary_{}_{}_performanceTestingDefaultOnTraining_f_{}.csv".format(locationFileResults, astModel, algorithm,fraction))

	'''For Table of  RQ 1'''
	def _test_D_ComparisonBest_PlotRQ1(self):
		for astModel in [NAME_FOLDER_ASTJDT, NAME_FOLDER_ASTSPOON]:
			fraction = 1

			locationFileResultsGumTree = "{}/summaryResults/GridSearch/dataset_{}/algorithm_{}/seed_0/fractionds_{}/".format(
				RESULTS_PROCESSED_LOCATION, astModel, "Gumtree", fraction)

			locationFileResultsChangeDistiller = "{}/summaryResults/GridSearch/dataset_{}/algorithm_{}/seed_0/fractionds_{}/".format(
				RESULTS_PROCESSED_LOCATION, astModel, "ChangeDistiller", fraction)
			locationFileResultsXyMatcher = "{}/summaryResults/GridSearch/dataset_{}/algorithm_{}/seed_0/fractionds_{}/".format(
				RESULTS_PROCESSED_LOCATION, astModel, "XyMatcher", fraction)

			plotDistributionAvg( "{}/avg_performance_performanceOnTraining_{}_GumTree_f_{}.csv".format(locationFileResultsGumTree, astModel, fraction),
								"{}/avg_performance_performanceOnTraining_{}_ChangeDistiller_f_{}.csv".format(locationFileResultsChangeDistiller, astModel, fraction),
								"{}/avg_performance_performanceOnTraining_{}_XyMatcher_f_{}.csv".format(locationFileResultsXyMatcher, astModel, fraction),
								 "{}/summary_{}_Gumtree_performanceTestingDefaultOnTraining_f_{}.csv".format(locationFileResultsGumTree, astModel, fraction),
								 "{}/summary_{}_ChangeDistiller_performanceTestingDefaultOnTraining_f_{}.csv".format(locationFileResultsChangeDistiller, astModel, fraction),
								"{}/summary_{}_XyMatcher_performanceTestingDefaultOnTraining_f_{}.csv".format(locationFileResultsXyMatcher, astModel, fraction),
								 model= "JDT" if "JDT" in astModel else "Spoon", out=RESULTS_PROCESSED_LOCATION)

	'''Deprecated, not used any more, those metrics are computed based on the  summary_avg_performance_performance'''
	def _testCheckTest(self):
		dataset = NAME_FOLDER_ASTSPOON
		for algo in ["Gumtree", "ChangeDistiller", "Xy"]:
			for t in ["pmann", "pwilcoxon"]:
				for p in ["performance", "index"]:
					countHigherValuesFile(
						"{}/summary_{}_{}_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION, dataset, algo, t,p), thr=0.05)
			print("-----")

	'''Deprecated, not used any more, those metrics are computed based on the  summary_avg_performance_performance'''
	def _testCorrelation(self):
		dataset = NAME_FOLDER_ASTSPOON
		for algo in ["Gumtree", "ChangeDistiller", "Xy"]:
			for t in ["rp", "srho"]:
				for p in ["performance", "index"]:
					countHigherValuesFile(
						"{}/summary_{}_{}_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION,dataset, algo, t,p), thr=0.90)
			print("-----")

if __name__ == '__main__':
	unittest.main()

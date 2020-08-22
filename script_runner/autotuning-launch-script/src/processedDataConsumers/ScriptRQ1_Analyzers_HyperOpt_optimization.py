import unittest
from src.processedDataConsumers.EngineHyperOptDAT import *
from src.commons.Datalocation import *
from src.rowDataConsumers.RQ0_Setup_ComputeEDSizeOfConfiguationsFromRowData import *
from src.processedDataConsumers.CostParameters import *
class TestHyperOp(unittest.TestCase):

	def _test_A_ComputeDistanceFastPerAlgorithm(self):

		for folderToAnalyze in [NAME_FOLDER_ASTJDT,  NAME_FOLDER_ASTSPOON]:
			for algorithm in ["Gumtree", "ChangeDistiller", "XyMatcher"]:
				computeEditScriptSize("{}/{}/".format(RESULTS_ROW_LOCATION, folderToAnalyze), suffix="{}_{}".format(folderToAnalyze, algorithm), key = algorithm)


	def _test_CompteHyperOpt_single_by_algo(self):
			''''only 1000 '''''
			kfold = 10
			maxeval = 1000
			franction = 0.005
			seed=20
			for folderToAnalyze in [NAME_FOLDER_ASTSPOON, NAME_FOLDER_ASTJDT]:
				for algorithm in ["Gumtree", "ChangeDistiller",
								  "XyMatcher"]:
					print("\nanalyzing {}".format(folderToAnalyze))
					computeHyperOpt(pathResults="{}/editscript_size_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze, algorithm), kFold=kfold, max_evals=maxeval,fractiondata= franction,  dataset = folderToAnalyze, algorithm = algorithm, out=RESULTS_PROCESSED_LOCATION, random_seed=seed )

	def _test_CompteHyperOpt_single_by_algo_GumTree(self):
			''''only 1000 '''''
			kfold = 5
			maxeval = 200
			franction = 1
			seed=10
			TPE = True
			measure  = [True ,False]
			for folderToAnalyze in [NAME_FOLDER_ASTSPOON, NAME_FOLDER_ASTJDT]:
				for algorithm in ["Gumtree"]:
					print("\nanalyzing {}".format(folderToAnalyze))
					for useAvg in measure:
						computeHyperOpt(pathResults="{}/editscript_size_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze, algorithm),overwrite=False,useAverage=useAvg, runTpe=TPE,  kFold=kfold, max_evals=maxeval,fractiondata= franction,  dataset = folderToAnalyze, algorithm = algorithm, out=RESULTS_PROCESSED_LOCATION, random_seed=seed )

	def _test_CompteHyperOpt_single_by_algo_ChangeDistiller(self):
			''''only 1000 '''''
			kfold = 2
			maxeval = 10
			franction = 0.01
			seed=10
			TPE = True
			measure  = [True]
			for folderToAnalyze in [NAME_FOLDER_ASTSPOON, NAME_FOLDER_ASTJDT]:
				for algorithm in ["ChangeDistiller"]:
					print("\nanalyzing {}".format(folderToAnalyze))
					for useAvg in measure:
						computeHyperOpt(pathResults="{}/editscript_size_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze, algorithm),overwrite=True,useAverage=useAvg, runTpe=TPE,  kFold=kfold, max_evals=maxeval,fractiondata= franction,  dataset = folderToAnalyze, algorithm = algorithm, out=RESULTS_PROCESSED_LOCATION, random_seed=seed )

	def test_CompteHyperOpt_single_by_algo_Xy(self):
			''''only 1000 '''''
			kfold = 2
			maxeval = 10
			franction = 0.01
			seed=10
			TPE = True
			measure  = [True]
			for folderToAnalyze in [NAME_FOLDER_ASTSPOON, NAME_FOLDER_ASTJDT]:
				for algorithm in ["XyMatcher"]:
					print("\nanalyzing {}".format(folderToAnalyze))
					for useAvg in measure:
						computeHyperOpt(pathResults="{}/editscript_size_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze, algorithm),overwrite=True,useAverage=useAvg, runTpe=TPE,  kFold=kfold, max_evals=maxeval,fractiondata= franction,  dataset = folderToAnalyze, algorithm = algorithm, out=RESULTS_PROCESSED_LOCATION, random_seed=seed )



	def _test_CompteHyperOpt_single_by_algo_GumTree_useAverage(self):

			kfold = 2
			maxeval = 100
			franction = 1
			seed=20
			model = NAME_FOLDER_ASTJDT
			for algorithm in ["Gumtree"]:
				print("\nanalyzing {}".format(model))
				computeHyperOpt(pathResults="{}/editscript_size_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION,model, algorithm),
								useAverage=False,
								kFold=kfold, max_evals=maxeval,fractiondata= franction,
								dataset = model, algorithm = algorithm, out="{}/test/".format(RESULTS_PROCESSED_LOCATION), random_seed=seed )





	def _test_debug_CompteHyperOpt_single_by_algo(self):
			kfold = 2
			maxeval = 10
			franction = 0.1
			seed=20
			TPE = False
			for folderToAnalyze in [NAME_FOLDER_ASTJDT]:
				for algorithm in ["Gumtree", "ChangeDistiller",
								  "XyMatcher"]:
					print("\nanalyzing {}".format(folderToAnalyze))
					computeHyperOpt(pathResults="{}/editscript_size_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION,folderToAnalyze, algorithm),overwrite=True, runTpe=TPE, kFold=kfold, max_evals=maxeval,fractiondata= franction,  dataset = folderToAnalyze, algorithm = algorithm, out=RESULTS_PROCESSED_LOCATION, random_seed=seed )
					break

	def _test_CompteHyperOpt_allAlgos(self):
			''''only 1000 '''''
			kfold = 10
			for folderToAnalyze in [NAME_FOLDER_ASTJDT, NAME_FOLDER_ASTSPOON]:

					print("\nanalyzing {}".format(folderToAnalyze))
					computeHyperOpt(pathResults="{}/editscript_size_per_diff_{}.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze), kFold=kfold, max_evals=1000,fractiondata= 1,  dataset = folderToAnalyze, algorithm = None, out=RESULTS_PROCESSED_LOCATION )



	def _testAnalyzeHyperopResults(self):
		evals = 1000
		fraction = 1
		parentMethodFolder = None
		childMethodFolder = None

		isTPE = False

		if isTPE:
			parentMethodFolder = "TPE"
			childMethodFolder = "hyper"
		else:
			parentMethodFolder = "random"
			childMethodFolder = "random"

		for modelToAnalyze in [NAME_FOLDER_ASTJDT, NAME_FOLDER_ASTSPOON]:
			for algo in ["Gumtree", "ChangeDistiller", "XyMatcher"]:

				location = "{}/summaryResults/{}/hyper_op/dataset_{}/algorithm_{}/seed_0/fractionds_{}/".format(RESULTS_PROCESSED_LOCATION,parentMethodFolder, modelToAnalyze, algo,fraction)
				#/TPE/hyper_op/dataset_merge_out4gt_outCD/algorithm_Gumtree/seed_3/fractionds_1/hyper_op_merge_out4gt_outCD_defaultConfigsPerformance_Gumtree_evals_1000_f_1.csv

				print("\nBest: ")
				pathBest = "{}/{}_op_{}_bestConfigsPerformance_{}_evals_{}_f_{}.csv".format(location,childMethodFolder,
																									 modelToAnalyze,
																									 algo, evals,
																									 fraction)
				pathDefault = "{}/{}_op_{}_defaultConfigsPerformance_{}_evals_{}_f_{}.csv".format(location,childMethodFolder,
																									 modelToAnalyze,
																									 algo, evals,
																									 fraction)
				analyzeResultsHyperop2(
					pathDistances="{}/distance_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION, modelToAnalyze,
																		  algo),
					pathSize="{}/editscript_size_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION, modelToAnalyze,
																			algo),
					pathBest=pathBest,pathDefault=pathDefault, algo=algo, model="JDT" if "JDT" in modelToAnalyze else "Spoon"
				)

				print("----")

if __name__ == '__main__':
	unittest.main()

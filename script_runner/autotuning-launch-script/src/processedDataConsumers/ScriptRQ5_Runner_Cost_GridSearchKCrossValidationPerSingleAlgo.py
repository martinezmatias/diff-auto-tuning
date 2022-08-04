from src.processedDataConsumers.EngineHyperOptDAT import *
import subprocess
from src.execution.Config import  *
from src.commons.Datalocation import *


def main(onlyTest = False):
	for folderToAnalyze in [NAME_FOLDER_ASTJDT,
							NAME_FOLDER_ASTSPOON
							]:
		print("\nAnalyzing {}".format(folderToAnalyze))
		kvalue = KFOLD_VALUE
		for palgorithm in ["Gumtree"#, "ChangeDistiller", "XyMatcher"
						   ]:

			for iseed in range(0, 5):

				runGridSearchK("{}/editscript_size_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze, palgorithm),
											   kFold=kvalue, algorithm=palgorithm,
											   random_seed=iseed,
								   dataset=folderToAnalyze)

		if onlyTest:
				print("Only test, break. End")
				return


def runGridSearchK(pathResults ="../../../../plots/data/distance_per_diff.csv", kFold=5, dataset ="alldata", algorithm = None, out ="../../plots/data/",  random_seed = 0):

	at_pythom_cmd =  "python3 -m src.processedDataConsumers.RQ5_GridSearchLauncher {} {} {} {} {} ".format(pathResults, algorithm, kFold, dataset, random_seed)

	cmd = ""
	try:
		if is_grid5k():
			print("Running on grid")

			cmd = "oarsub -l nodes=1,walltime=%s -O %s -E %s \"%s\"" % (
				GRID5K_TIME_OUT,
				"./logs/out_{}_{}_{}_{}_seed_{}.txt".format( "gridsearch"  ,"allAlgo" if algorithm is None else algorithm, kFold,dataset, random_seed),
				"./logs/error_{}_{}_{}_{}_seed_{}.txt".format("gridsearch" , "allAlgo"  if algorithm is None else algorithm, kFold,  dataset, random_seed),at_pythom_cmd)

		else:
			cmd = at_pythom_cmd


		print(" Running command {} ".format(cmd))
		devnull = open('/dev/null', 'w')
		cmd_output = subprocess.check_output(cmd, shell=True, stdin=None, stderr=devnull)

		print("Finish Return command {}".format(cmd_output))


	except Exception as ex:
		print("Error with serialization execution {}".format(""))
		print(ex)



def is_grid5k():
    return os.path.exists("/usr/bin/oarsub")

main(onlyTest=False)
print("END")

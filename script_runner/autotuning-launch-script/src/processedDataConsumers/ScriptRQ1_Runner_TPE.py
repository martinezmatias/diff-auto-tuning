from src.processedDataConsumers.EngineHyperOptDAT import *
import subprocess
from src.execution.Config import  *
from src.commons.Datalocation import *


def main(runTPE = True, onlyTest = False):

	for folderToAnalyze in [NAME_FOLDER_ASTSPOON,
							NAME_FOLDER_ASTJDT
							]:
		for algorithm in ["Gumtree", "ChangeDistiller",
						  "XyMatcher"]:

			for iseed in range(0, SEEDS_TO_EXECUTE):
					print("\nanalyzing {}".format(folderToAnalyze))
					runHyperOpts(pathResults="{}/distance_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze, algorithm), dataset = folderToAnalyze, runTpe=runTPE, algo=algorithm, seed=iseed)

					if onlyTest:
						print("Only test mode, we stop The Execution")
						return


def runHyperOpts(pathResults ="../../../../plots/data/distance_per_diff.csv", runTpe = True,  dataset ="alldata", algo = None,  out ="../../plots/data/", seed = 0):

	at_pythom_cmd =  "python3 -m src.processedDataConsumers.RQ1_TPELauncher {} {} {} {} {}".format(pathResults,  dataset, runTpe, algo, seed)

	cmd = ""
	try:
		if is_grid5k():
			print("Running on grid")

			cmd = "oarsub -l nodes=1,walltime=%s -O %s -E %s \"%s\"" % (
				GRID5K_TIME_OUT,
				"./logs/out_{}_{}_{}_seed_{}.txt".format( "hyperop" if runTpe else "random" , dataset, algo, seed),
				"./logs/error_{}_{}_{}__seed_{}.txt".format("hyperop" if runTpe else "random", dataset, algo, seed),
				at_pythom_cmd)

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

main(runTPE = False, onlyTest=False)
main(runTPE = True, onlyTest=False)
print("END")
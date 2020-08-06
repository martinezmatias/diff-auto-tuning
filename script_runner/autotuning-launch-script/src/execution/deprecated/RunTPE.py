from src.processedDataConsumers.EngineHyperOptDAT import *
import subprocess
from src.execution.Config import  *
from src.commons.Datalocation import *


def main(onlyTest = False):
	evals_range = [10, 20, 50, 100, 200, 500]
	ratio = [0.01, 0.025, 0.05, 0.1, 0.2, 0.5]
	runTPE = True
	for i_eval in evals_range:
		kfold = 10
		for i_ratio in ratio:
			for folderToAnalyze in [NAME_FOLDER_ASTSPOON, NAME_FOLDER_ASTJDT]:
				for algorithm in ["Gumtree", "ChangeDistiller",
								  "XyMatcher"]:

					print("\nanalyzing {}".format(folderToAnalyze))
					runHyperOpts(pathResults="{}/distance_per_diff_{}_{}.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze, algorithm), kFold=kfold, max_evals=i_eval, fractiondata= i_ratio, dataset = folderToAnalyze, algorithm = algorithm, runTpe=runTPE)

					if onlyTest:
						print("Only test mode, we stop The Execution")
						return

#folderToAnalyze = sys.argv[1]

#algorithm = sys.argv[2]

#kfold = int(sys.argv[3])

#i_eval = float(sys.argv[4])

#i_ratio = float(sys.argv[5])

#datasetname = sys.argv[6]

def runHyperOpts(pathResults ="../../../../plots/data/distance_per_diff.csv", kFold=5, runTpe = True, max_evals=1000, random_seed = 0, fractiondata= 0.1, dataset ="alldata", algorithm = None, out ="../../plots/data/"):

	at_pythom_cmd =  "python3 -m src.execution.TPELauncher {} {} {} {} {} {} {}".format(pathResults, algorithm, kFold, max_evals, fractiondata, dataset, runTpe)

	cmd = ""
	try:
		if is_grid5k():
			print("Running on grid")

			cmd = "oarsub -l nodes=1,walltime=%s -O %s -E %s \"%s\"" % (
				GRID5K_TIME_OUT,
				"./logs/out_{}_{}_{}_{}_{}_{}.txt".format( "hyperop" if runTpe else "random" ,algorithm, kFold, max_evals, fractiondata, dataset),
				"./logs/error{}_{}_{}_{}_{}_{}.txt".format("hyperop" if runTpe else "random", algorithm, kFold, max_evals, fractiondata, dataset),
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

main(onlyTest=False)
print("END")
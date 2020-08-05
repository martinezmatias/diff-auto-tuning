from src.processedDataConsumers.EngineHyperOptDAT import *
import subprocess
from src.execution.Config import  *
from src.commons.Datalocation import *


def main(onlyTest = False):
	for folderToAnalyze in [NAME_FOLDER_ASTJDT,
							NAME_FOLDER_ASTSPOON
							]:
		print("\nAnalyzing {}".format(folderToAnalyze))
		kvalue = 10
		random_seed_value = 0

		runGridSearchK("{}/distance_per_diff_{}.csv".format(RESULTS_PROCESSED_LOCATION, folderToAnalyze),
								   kFold=kvalue, fractiondata = 1, algorithm=None,
						#		   random_seed=random_seed_value,
					   dataset=folderToAnalyze)


#distancespath = sys.argv[1]

#algorithm = sys.argv[2]

#kfold = int(sys.argv[3])

#datasetname = sys.argv[4]

#fraction = sys.argv[5]

def runGridSearchK(pathResults ="../../../../plots/data/distance_per_diff.csv", kFold=5,  fractiondata= 0.1, dataset ="alldata", algorithm = None, out ="../../plots/data/"):

	at_pythom_cmd =  "python3 -m src.execution.GridLauncher {} {} {} {} {}".format(pathResults, algorithm, kFold, dataset,fractiondata)

	cmd = ""
	try:
		if is_grid5k():
			print("Running on grid")

			cmd = "oarsub -l nodes=1,walltime=%s -O %s -E %s \"%s\"" % (
				GRID5K_TIME_OUT,
				"./logs/out_{}_{}_{}_{}_{}.txt".format( "gridsearch"  ,"allAlgo" if algorithm is None else algorithm, kFold, fractiondata, dataset),
				"./logs/error{}_{}_{}_{}_{}.txt".format(",fractiondata" , "allAlgo"  if algorithm is None else algorithm, kFold, fractiondata, dataset),at_pythom_cmd)

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
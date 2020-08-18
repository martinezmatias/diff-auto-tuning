from src.processedDataConsumers.EngineGridSearchKfoldValidation import *
from src.processedDataConsumers.deprecated.plotPerformance import *
from src.rowDataConsumers.RQ0_Setup_ComputeFitnessDistanceOfConfiguationsFromRowData import *
from src.commons.Datalocation import *
from src.execution.Config import  *
from src.commons.Datalocation import *
import os, subprocess


def main(onlyTest = False):

	for folderToAnalyze in [NAME_FOLDER_ASTJDT, NAME_FOLDER_ASTSPOON]:
		for algorithm in ["Gumtree", "ChangeDistiller", "XyMatcher"]:
			runComputeED("{}/{}/".format(RESULTS_ROW_LOCATION, folderToAnalyze),
						   suffix="{}_{}".format(folderToAnalyze, algorithm), key=algorithm)

			if onlyTest:
				print("Only test mode, we stop The Execution")
				return


def runComputeED(pathResults ,  suffix ="", key = None):

	at_pythom_cmd =  "python3 -m src.rowDataConsumers.RQ0_EDSizeLauncher {} {} {}".format(pathResults,  suffix, key)

	cmd = ""
	try:
		if is_grid5k():
			print("Running on grid")

			cmd = "oarsub -l nodes=1,walltime=%s -O %s -E %s \"%s\"" % (
				GRID5K_TIME_OUT,
				"./logs/out_edsize_{}.txt".format( key),
				"./logs/error_edsize_{}.txt".format(key),
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
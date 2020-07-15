import  os
from src.execution.Config import  *
import subprocess


def is_grid5k():
    return os.path.exists("/usr/bin/oarsub")


def runProject(out, path, subset, begin=0, stop = 10000000, astmodel="GTSPOON", parallel=True, matchers = ""):

	at_java_cmd = javahome + "/java  -cp {}  fr.gumtree.autotuning.Main -out={} -path={} -subset={} -begin={} -stop={} -astmodel={} -parallel={} {} ".format(liblocation, out, path, subset, begin, stop, astmodel, parallel, matchers)

	cmd = ""
	try:
		if is_grid5k():
			print("Running on grid")

			cmd = "oarsub -l nodes=1,walltime=%s -O %s -E %s \"%s\"" % (
				GRID5K_TIME_OUT,
				"./logs/out_{}_{}_{}_{}.txt".format(subset,begin,stop, astmodel),
				"./logs/error_{}_{}_{}_{}.txt".format(subset,begin,stop, astmodel),
				at_java_cmd)

			#cmd = at_java_cmd
			print("command to run: {}".format(cmd))

		else:
			cmd = at_java_cmd


		print(" at java command {} ".format(cmd))
		devnull = open('/dev/null', 'w')
		cmd_output = subprocess.check_output(cmd, shell=True, stdin=None, stderr=devnull)
		## cmd_output = subprocess.check_call(cmd, shell=True, stdout=STDOUT, stdin=None, stderr=devnull)

		print("Return grid node execution {}".format(cmd_output))


	except Exception as ex:
		print("Error with serialization execution {}".format(""))
		print(ex)




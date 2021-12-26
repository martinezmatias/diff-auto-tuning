from src.execution.TaskLauncher import *
from src.execution.Config import *
from src.rowDataConsumers.StoreIndex import *
import sys

if len(sys.argv) < 4:
	print("wrongs args")
	exit(0)

begin = sys.argv[1]

end = sys.argv[2]

model = sys.argv[3]

if len(sys.argv) > 4:
	outDir = sys.argv[4]
else:
	outDir = outResult

matchers = ""
if len(sys.argv) > 5:
	matP = sys.argv[5]
	mall = matP.split("_")
	for m in mall:
		matchers+=" -matchers="+m

if len(sys.argv) > 6:
	paralelltype = sys.argv[6]
else:
	paralelltype = "PROPERTY_LEVEL"



#for i in range(1, 41):

for aFileFromFolder in os.listdir(megadiffpath):

	abspath = os.path.join(megadiffpath, aFileFromFolder)
	
	if not str(aFileFromFolder).lower().startswith("git-"):
		print("Discarding {}: is not a dir".format(abspath))
		continue

	groupId, maxid = getMaxAnalyzed(outDir, str(aFileFromFolder), model)
	print("\n groupId {} , maxid {} ".format(groupId, maxid))
	if maxid is None:
		maxid = begin
	else:
		maxid = maxid + 1
	runProject(out = outDir, path = megadiffpath, subset=aFileFromFolder, begin=maxid, stop = end, astmodel=model, parallel=paralelltype, matchers=matchers)

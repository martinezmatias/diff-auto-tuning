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

if len(sys.argv) > 7:
	targetproject = sys.argv[7]
else:
	print("Missing project last parameter")
	exit()

groupId, maxid = getMaxAnalyzed(outDir, str(targetproject), model)
print(" groupId {} , maxid {} ".format(groupId, maxid))
if maxid is None:
		maxid = begin
else:
		maxid = maxid + 1
runProject(out = outDir, path = megadiffpath, subset=targetproject, begin=maxid, stop = end, astmodel=model, parallel=paralelltype, matchers=matchers)

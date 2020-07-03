from TaskLauncher import *
from Config import *
from StoreIndex import *
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

for i in range(1, 41):

	groupId, maxid = getMaxAnalyzed(outDir, str(i), model)
	print(" groupId {} , maxid {} ".format(groupId, maxid))
	if maxid is None:
		maxid = begin
	else:
		maxid = maxid + 1
	runProject(out = outDir, path = megadiffpath, subset=i, begin=maxid, stop = end, astmodel=model, parallel=True, matchers=matchers)

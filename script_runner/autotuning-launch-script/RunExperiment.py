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

for i in range(1, 41):

	groupId, maxid = getMaxAnalyzed(outResult, str(i), model)
	print(" groupId {} , maxid {} ".format(groupId, maxid))
	if maxid is None:
		maxid = begin
	else:
		maxid = maxid + 1
	runProject(out = outResult, path = megadiffpath, subset=i, begin=maxid, stop = end, astmodel=model, parallel=False)

from TaskLauncher import *
from Config import *
import sys

if len(sys.argv) < 4:
	print("wrongs args")
	exit(0)

begin = sys.argv[1]

end = sys.argv[2]

model = sys.argv[3]

for i in range(1, 41):
	runProject(out = outResult, path = megadiffpath, subset=i, begin=begin, stop = end, astmodel=model, parallel=True)

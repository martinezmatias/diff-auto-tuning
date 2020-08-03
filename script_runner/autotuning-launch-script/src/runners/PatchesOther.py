import os

cwd = "/Users/matias/develop/overfitting-research/drr/Patches"

bugs = {}

for dir_path, dir_names, file_names in os.walk(cwd):
	for f in file_names:
	   if f.endswith(".patch"):
		   s = f.split("-")
		   patchid = s[0]
		   project = s[1]
		   id = s[2]
		   tool = s[3]
		   bugkey = "{}_{}".format(project,id)
		   patchkey="{}_{}".format(project,tool)
		   if bugkey not in bugs:
			   bugs[bugkey] = []

		   bugs[bugkey].append(patchkey)

from collections import Counter
print("Summary")
allsizes = []
for bugkey in bugs.keys():
	nr = len(bugs[bugkey])
	allsizes.append(nr)
	print("{}: {}".format(bugkey, nr))

print("total {}".format(len(bugs.keys())))
occurrences = Counter(allsizes)
print(occurrences)
import matplotlib.pyplot as plt

x = allsizes
plt.hist(x, bins = 20)
plt.show()
plt.savefig("distributionnrbugs.pdf")



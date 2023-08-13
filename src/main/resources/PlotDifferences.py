
import matplotlib.pyplot as plt
import numpy as np

def computeDiff(vDefault, vBest):

	valuesDefault = []
	valuesDBest = []

	file1 = open(vDefault, 'r')
	Lines = file1.readlines()
	count = 0
	for line in Lines:
		count += 1
		valuesDefault.append(float(line.strip()))
	print("Default",count)

	file1 = open(vBest, 'r')
	Lines = file1.readlines()
	count = 0
	for line in Lines:
		count += 1
		valuesDBest.append(float(line.strip()))
	print("Best", count)

	diff = []
	outlier = 0
	for i in range(0, len(valuesDBest)):
		if not (valuesDBest[i] > 100000 or valuesDefault[i] > 100000):

			currentDiff = valuesDBest[i] - valuesDefault[i]
			limits = 25
			if currentDiff > -limits and currentDiff < limits:
				diff.append(currentDiff)
			else:
				outlier+=1

	print(len(diff))
	print(outlier)

	return diff


def copyD(old, new):
	valuesDefault = []
	valuesDBest = []

	file1 = open(old, 'r')

	fileOut = open(new, 'w')
	Lines = file1.readlines()
	count = 0
	outliers=0
	for line in Lines:
		count += 1

		value = float(line.strip())
		if value > 100000:
			fileOut.write(value)
			fileOut.write("\n")
		else:
			outliers+=1

	fileOut.close()
	file1.close()
	print("end copying, outliers ", outliers)

def computeDiffOfImproved(vDefault, vBest):
	valuesDefault = []
	valuesDBest = []

	file1 = open(vDefault, 'r')
	Lines = file1.readlines()
	count = 0
	for line in Lines:
		count += 1
		valuesDefault.append(float(line.strip()))
	print("Default",count)

	file1 = open(vBest, 'r')
	Lines = file1.readlines()
	count = 0
	for line in Lines:
		count += 1
		valuesDBest.append(float(line.strip()))
	print("Best", count)

	diff = []
	outlier = 0

	idems = 0
	improve = 0
	decr = 0
	total= 0
	for i in range(0, len(valuesDBest)):
		if not (valuesDBest[i] > 100000 or valuesDefault[i] > 100000):

			if valuesDefault[i] == 0:
				continue
			total+=1
			if valuesDefault[i] > valuesDBest[i]:
				improve+=1
			elif valuesDefault[i] < valuesDBest[i]:
				decr+=1
			else:
				idems+=1
				continue


			if valuesDefault[i] >=  valuesDBest[i]:
				currentDiff =  ((valuesDefault[i] -  valuesDBest[i]) / valuesDefault[i] ) * 100
			else:
				currentDiff = ((valuesDefault[i] - valuesDBest[i]) / valuesDBest[i]) * 100
			if  True:#currentDiff > 0:
					diff.append(currentDiff)
			else:
					outlier+=1

	print("considered", len(diff))
	print("outliers",outlier)

	print("impro", improve/total)
	print("dec", decr/total)
	print("eq", idems/total)
	return diff


# Create a figure instance
fig = plt.figure()



vDefaultJDT = "/Users/matias/develop/gt-tuning/git-code-gpgt/out/cross_validation_global_both_new_1683630367925_median/ExaJDT_1000__values_ClassicGumtree-bu_minsim-0.5-bu_minsize-1000-st_minprio-1-st_priocalc-height.txt" #"/Users/matias/develop/gt-tuning/git-dat-results/summary_results/RQ1/JDT/values_default.txt"
vBestJDT = "/Users/matias/develop/gt-tuning/git-code-gpgt/out/cross_validation_global_both_new_1683630367925_median/ExaJDT_1000__values_best.txt"#"/Users/matias/develop/gt-tuning/git-dat-results/summary_results/RQ1/JDT/values_best.txt"

vDefaultSpoon = "/Users/matias/develop/gt-tuning/git-code-gpgt/out/cross_validation_global_both_new_1683630367925_median/ExaSpoon_1000__values_ClassicGumtree-bu_minsim-0.5-bu_minsize-1000-st_minprio-1-st_priocalc-height.txt" #"/Users/matias/develop/gt-tuning/git-dat-results/summary_results/RQ1/Spoon/values_default.txt"
vBestSpoon = "/Users/matias/develop/gt-tuning/git-code-gpgt/out/cross_validation_global_both_new_1683630367925_median/ExaSpoon_1000__values_best.txt"#"/Users/matias/develop/gt-tuning/git-dat-results/summary_results/RQ1/Spoon/values_best.txt"


diffJDT = computeDiffOfImproved(vDefaultJDT,vBestJDT)
diffSpoon = computeDiffOfImproved(vDefaultSpoon,vBestSpoon)



fig, ax = plt.subplots()
ax.violinplot([diffJDT, diffSpoon], #showmeans=True,
              showmedians=True,)
# add median value as a point
plt.axhline(y = 0, color = 'r', linestyle = '--')


#plt.ylabel("size tuned - size default", fontsize=20)
plt.ylabel("% length reduction", fontsize=20)
plt.yticks(fontsize=18)
ax.set_xticks([1, 2])
ax.set_xticklabels(["JDT","Spoon"], fontsize=20)
plt.show()
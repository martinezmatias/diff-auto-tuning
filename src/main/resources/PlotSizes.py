
import matplotlib.pyplot as plt
import numpy as np

def getValues(vDefault):

	values = []
	out = 0
	file1 = open(vDefault, 'r')
	Lines = file1.readlines()
	count = 0
	for line in Lines:
		count += 1
		if (int(line.strip()) >5000):
			out+=1
		else:
			values.append(int(line.strip()))
	print(out)
	return values

valuesJDT = getValues("/Users/matias/develop/gt-tuning/git-code-gpgt/out/RQtimes_TPE_HYPEROPT__jdt_MEDIAN_sizeds_9996_attemps_10_nrseeds_1_1684837677661/eval_sizes.csv")
valuesSpoon = getValues("/Users/matias/develop/gt-tuning/git-code-gpgt/out/RQtimes_TPE_HYPEROPT__Spoon_MEDIAN_sizeds_9996_attemps_10_nrseeds_1_1684837743110/eval_sizes.csv")

print("JDT")
print(np.mean(valuesJDT))
print(np.median(valuesJDT))

print("valuesSpoon")
print(np.mean(valuesSpoon))
print(np.median(valuesSpoon))


# https://stackoverflow.com/questions/26291479/changing-the-color-of-matplotlibs-violin-plots

fig, ax = plt.subplots()
violin_parts = ax.violinplot([valuesJDT, valuesSpoon], #showmeans=True,
              showmedians=True)

for pc in violin_parts['bodies']:
    pc.set_facecolor('green')
    pc.set_edgecolor('black')

for partname in ('cbars','cmins','cmaxes','cmedians'):
    vp = violin_parts[partname]
    vp.set_edgecolor("black")
    vp.set_linewidth(1)

#plt.axhline(y = 0, color = 'r', linestyle = '--')

plt.ylabel("AST size", fontsize=20)
plt.yticks(fontsize=18)
ax.set_xticks([1, 2])
ax.set_xticklabels(["JDT","Spoon"], fontsize=20)
plt.show()
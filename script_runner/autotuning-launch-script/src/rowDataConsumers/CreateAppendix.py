import pandas
import os
import shutil
def createAppendix(pathResults= "/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/results/editscript_size_per_diff_merge_out4gtJDT_outCDJDT_Gumtree.csv"):
	dfcomplete = pandas.read_csv(pathResults, sep=",")

	diffs = dfcomplete['diff']
	nrexist = 0
	i = 1
	out = "/Users/matias/develop/dat/dat_experiment/dataset/"
	#for i in range(1, 11):
	#	os.makedirs("{}/{}/".format(out, i * 10000) )

	print(len(diffs))
	for diff in diffs:
		#print(diff)
		namedsplited = diff.split("_")


		folder = namedsplited[3]
		id = namedsplited[4]
		#print("{} {} ".format(folder, id))
		megadiffpath = "/Users/matias/develop/newAstorexecution/megadiff-last-zip/"

		fileloc = os.path.join(megadiffpath, folder, "{}.diff.xz".format(id))

		if i * 10000 == nrexist:
			i+=1


		if os.path.exists(fileloc):
			nrexist+=1
			shutil.copy(fileloc, "{}/{}/diff_{}.diff.xz".format(out,i * 10000,nrexist))
		if nrexist == 100000:
			break



	print(nrexist)
##
createAppendix()
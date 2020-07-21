import os
import shutil

def merge(location1, location2, destination, ignoreFromSource = ["XyMatcher"]):
		files = (os.listdir(location1))
		files = list(filter(lambda x: os.path.isdir(os.path.join(location1, x)), files))
		totalDiffAnalyzed = 0
		exists = 0
		notexist = 0
		## Navigate group ids
		for groupId in sorted(files, key=lambda x: int(x)):

			if groupId == ".DS_Store":
				continue

			filesGroup1 = os.path.join(location1, groupId)
			filesGroup2 = os.path.join(location2, groupId)
			destination =  os.path.join(destination, groupId)

			if not os.path.isdir(filesGroup1):
				continue

			if not os.path.exists(destination):
				os.makedirs(destination)

			##Navigates diff
			listdir = os.listdir(filesGroup1)
			for diff in listdir:
				if not diff.endswith(".csv") or diff.startswith("metaInfo"):
					continue

				csvFile1 = os.path.join(filesGroup1, diff)

				csvFile2 = os.path.join(filesGroup2, diff)

				csvout = os.path.join(destination, diff)

				if not os.path.exists(csvFile2):
					#print("not exist {} ".format(csvout))
					notexist +=1

					shutil.copyfile(csvFile1, csvout)
					#
				else:
					exists +=1
					#print("exist exist {} ".format(csvout))

					f1 = open(csvFile1, 'r')
					f2 = open(csvFile2, 'r')
					fout = open(csvout, 'w')

					line = f1.readline()

					# copy one
					while line:

						if not line.startswith("Xy"):
							fout.write(line)
						line = f1.readline()
					#
					line = f2.readline()
					while line:
						# ignore the first one, so we call read again
						line = f2.readline()
						fout.write(line)

					f1.close()
					f2.close()
					fout.close()



		print("exists {} not exists {} ".format(exists, notexist))
merge(location1="/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/results/out10bis5_4gt",
	  location2="/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/results/outCD_2",
	  destination="/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/results/testmerge")
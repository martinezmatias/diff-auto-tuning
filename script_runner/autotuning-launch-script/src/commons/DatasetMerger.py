import os
import shutil

def merge(location1, location2, destination, ignoreFromSource = ["XyMatcher"], merge = True):
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
			destinationGroup =  os.path.join(destination, groupId)

			if not os.path.isdir(filesGroup1):
				continue

			if not os.path.exists(destinationGroup):
				os.makedirs(destinationGroup)

			##Navigates diff
			listdir = os.listdir(filesGroup1)
			for diff in listdir:
				if not diff.endswith(".csv") or diff.startswith("metaInfo"):
					continue

				csvFile1 = os.path.join(filesGroup1, diff)

				csvFile2 = os.path.join(filesGroup2, diff)

				csvout = os.path.join(destinationGroup, diff)

				if not os.path.exists(csvFile2):
					#print("not exist {} ".format(csvout))
					notexist +=1

					shutil.copyfile(csvFile1, csvout)
					#
				else:

					if not merge:
						continue

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


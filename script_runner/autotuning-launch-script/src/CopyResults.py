
import os
import shutil
import sys
def navigateAndCopy(source, destination):
	print("")

	if not os.path.exists(destination):
		print("creating destinatio {}".format(destination))

	for project in os.listdir(source):

		pathSourceProject = os.path.join(source, project)

		if  os.path.isfile(pathSourceProject):
			continue

		for diffname in os.listdir(pathSourceProject):

			pathSourceDiff = os.path.join(pathSourceProject, diffname)

			if os.path.isfile(pathSourceDiff):
				continue

			for fileInDiff in os.listdir(pathSourceDiff):
				pathSourceFileDiff = os.path.join(pathSourceDiff, fileInDiff)
				if str(fileInDiff).startswith("result"):

					pathDestDiff = os.path.join(destination, project, diffname)

					if not os.path.exists(pathDestDiff):
						os.makedirs(pathDestDiff)

					desfilepath = os.path.join(pathDestDiff, fileInDiff)

					print("coping file from {} to {} ".format(pathSourceFileDiff, desfilepath))

					shutil.copy(pathSourceFileDiff,desfilepath)

navigateAndCopy(sys.argv[1], sys.argv[2])
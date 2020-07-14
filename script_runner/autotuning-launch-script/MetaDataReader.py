import csv

'''get the informattion of the Tree given a diff id'''
def getTreeMetricsFromFile(location, diffId):

	with open('{}/metaInfo_{}'.format(location, diffId), mode='r') as csv_file:
		csv_reader = csv.DictReader(csv_file)
		for row in csv_reader:
			size = row["L_SIZE"]
			height = row["L_HEIGHT"]
			return int(size), int(height)
	return None, None
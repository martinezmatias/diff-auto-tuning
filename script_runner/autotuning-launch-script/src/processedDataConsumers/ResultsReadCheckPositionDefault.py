
import csv
from src.commons.DiffAlgorithmMetadata import *

'''Return the list of the default configurations together with the index in the list of configuration ordered by a given criterion e.g. number of best'''
def getPositionDefalt(fileLocation = "./plots/data/best_configurations_summary.csv"):
	configs = list(defaultConfigurations.values())
	indexesConfigurations = []

	with open(fileLocation, mode='r') as csv_file:
		csv_reader = csv.DictReader(csv_file)
		line_count = 0
		for row in csv_reader:
			configuration = row["configuration"]
			if configuration in configs:
				print("{} at {}".format(configuration, line_count))
				indexesConfigurations.append({"configuration":configuration, "index":line_count})

			line_count += 1

	print("\n---Results: ")
	print("found {}".format(indexesConfigurations))
	notfound = list(filter(lambda x: x not in indexesConfigurations, configs))
	print("not found {}".format(notfound))

	## we add the not found configuration.
	for other in notfound:
		indexesConfigurations.append({"configuration": other, "index": None})

	return indexesConfigurations
##

#print(getPositionDefalt())

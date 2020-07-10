
import csv
from DiffAlgorithmMetadata import *

def getPositionDefalt(fileLocation = "./plots/data/best_configurations_summary.csv"):
	configs = list(defaultConfigurations.values())
	print(configs)
	found = []

	with open(fileLocation, mode='r') as csv_file:
		csv_reader = csv.DictReader(csv_file)
		line_count = 0
		for row in csv_reader:
			configuration = row["configuration"]
			if configuration in configs:
				print("{} at {}".format(configuration, line_count))
				found.append(configuration)
			#print(configuration)
			line_count += 1

	print("\n---Results: ")
	print("found {}".format(found))
	notfound = list(filter(lambda x: x not in found, configs))
	print("not found {}".format(notfound))

##

	print("End")
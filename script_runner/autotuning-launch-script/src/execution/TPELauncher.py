
import sys
from src.processedDataConsumers.ResultsHyperOptDAT import *


folderToAnalyze = sys.argv[1]

algorithm = sys.argv[2]

kfold = int(sys.argv[3])

i_eval = float(sys.argv[4])

i_ratio = float(sys.argv[5])

datasetname = sys.argv[6]

computeHyperOpt(folderToAnalyze, kFold=kfold, max_evals=i_eval ,fractiondata= i_ratio,  dataset = datasetname, algorithm = algorithm)
